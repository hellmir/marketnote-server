package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.payment.PaymentMethod;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.PaymentEventNotFoundException;
import com.personal.marketnote.commerce.exception.ReasonCategoryNoValueException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.command.order.GetReturnRefundInfoCommand;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;
import com.personal.marketnote.commerce.port.in.result.order.GetReturnRefundInfoResult;
import com.personal.marketnote.commerce.port.in.usecase.order.CalculateReturnShippingFeeUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetReturnRefundInfoUseCase;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
@Slf4j
public class GetReturnRefundInfoService implements GetReturnRefundInfoUseCase {

    private static final OrderStatus TARGET_STATUS = OrderStatus.RETURN_REQUESTED;

    private final GetOrderUseCase getOrderUseCase;
    private final CalculateReturnShippingFeeUseCase calculateReturnShippingFeeUseCase;
    private final FindPspPaymentEventPort findPspPaymentEventPort;

    @Override
    public GetReturnRefundInfoResult getReturnRefundInfo(GetReturnRefundInfoCommand command) {
        Order order = getOrderUseCase.getOrder(command.orderId());

        validateBuyerOwnership(order, command.buyerId());
        validateReasonCategory(command.reasonCategory());
        validateReturnableStatus(order);

        Set<Long> targetPricePolicyIds = resolveTargetPricePolicyIds(
                order.getOrderProducts(), command.returnPricePolicyIds()
        );
        boolean isFullReturn = targetPricePolicyIds.size() >= order.getOrderProducts().size();

        long totalProductAmount = calculateTotalProductAmount(order.getOrderProducts(), targetPricePolicyIds);
        long returnShippingFee = calculateReturnShippingFee(command);
        String refundMethod = resolveRefundMethod(order);
        long estimatedRefundCash = calculateEstimatedRefundCash(order, totalProductAmount, isFullReturn);
        long estimatedRefundAmount = calculateEstimatedRefundAmount(
                totalProductAmount, returnShippingFee, estimatedRefundCash
        );

        return GetReturnRefundInfoResult.of(
                totalProductAmount, returnShippingFee, refundMethod, estimatedRefundAmount, estimatedRefundCash
        );
    }

    private void validateBuyerOwnership(Order order, Long buyerId) {
        if (!order.isBuyer(buyerId)) {
            log.warn("주문 소유자 불일치 - orderId: {}, 요청자: {}", order.getId(), buyerId);
            throw new UnauthorizedOrderAccessException();
        }
    }

    private void validateReasonCategory(OrderStatusReasonCategory reasonCategory) {
        if (FormatValidator.hasNoValue(reasonCategory)) {
            throw new ReasonCategoryNoValueException();
        }
        if (!reasonCategory.isReturnReason()) {
            log.warn("주문 상태 변경에 적합하지 않은 사유 카테고리입니다. reasonCategory={}", reasonCategory.name());
            throw new InvalidReasonCategoryException();
        }
    }

    private void validateReturnableStatus(Order order) {
        if (!order.getOrderStatus().canTransitionTo(TARGET_STATUS)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), TARGET_STATUS);
        }
    }

    private long calculateTotalProductAmount(List<OrderProduct> orderProducts, Set<Long> targetPricePolicyIds) {
        return orderProducts.stream()
                .filter(product -> targetPricePolicyIds.contains(product.getPricePolicyId()))
                .mapToLong(product -> product.getUnitAmount().multiply(product.getQuantity().getValue()).getValue())
                .reduce(0L, Math::addExact);
    }

    private Set<Long> resolveTargetPricePolicyIds(List<OrderProduct> orderProducts, List<Long> returnPricePolicyIds) {
        Set<Long> allPricePolicyIds = orderProducts.stream()
                .map(OrderProduct::getPricePolicyId)
                .collect(Collectors.toSet());

        if (FormatValidator.hasNoValue(returnPricePolicyIds)) {
            return allPricePolicyIds;
        }

        Set<Long> requestedIds = new HashSet<>(returnPricePolicyIds);
        requestedIds.retainAll(allPricePolicyIds);
        return requestedIds;
    }

    private long calculateReturnShippingFee(GetReturnRefundInfoCommand command) {
        CalculateReturnShippingFeeResult result = calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                CalculateReturnShippingFeeCommand.from(command)
        );
        return result.returnShippingFee();
    }

    private String resolveRefundMethod(Order order) {
        String orderKey = order.getOrderKey().toString();
        PspPaymentEvent paymentEvent = findPspPaymentEventPort.findByOrderKey(orderKey)
                .orElseThrow(() -> {
                    log.warn("거래 등록 정보를 찾을 수 없습니다. orderKey={}", orderKey);
                    return new PaymentEventNotFoundException();
                });

        return PaymentMethod.fromMobileCode(paymentEvent.getMethod()).getDescription();
    }

    private long calculateEstimatedRefundCash(Order order, long totalProductAmount, boolean isFullReturn) {
        long pointAmount = order.getAmount().getPointAmount().getValue();
        if (pointAmount <= 0) {
            return 0L;
        }

        if (isFullReturn) {
            return pointAmount;
        }

        long totalAmount = order.getAmount().getTotalAmount().getValue();
        if (totalAmount <= 0) {
            return 0L;
        }

        return Math.multiplyExact(pointAmount, totalProductAmount) / totalAmount;
    }

    private long calculateEstimatedRefundAmount(
            long totalProductAmount, long returnShippingFee, long estimatedRefundCash
    ) {
        long amount = Math.subtractExact(
                Math.subtractExact(totalProductAmount, returnShippingFee), estimatedRefundCash
        );
        return Math.max(amount, 0L);
    }
}
