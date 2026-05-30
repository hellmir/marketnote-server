package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.PaymentMethod;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
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

        Set<Long> targetPricePolicyIds = resolveTargetPricePolicyIds(order.getOrderProducts(), command.returnPricePolicyIds());
        boolean isFullReturn = targetPricePolicyIds.size() >= order.getOrderProducts().size();

        long totalProductAmount = calculateTotalProductAmount(order.getOrderProducts(), targetPricePolicyIds);
        long returnShippingFee = calculateReturnShippingFee(command);
        String refundMethod = resolveRefundMethod(order);
        long estimatedRefundCash = calculateEstimatedRefundCash(order, totalProductAmount, isFullReturn);
        long estimatedRefundAmount = calculateEstimatedRefundAmount(totalProductAmount, returnShippingFee, estimatedRefundCash);

        return GetReturnRefundInfoResult.builder()
                .totalProductAmount(totalProductAmount)
                .returnShippingFee(returnShippingFee)
                .refundMethod(refundMethod)
                .estimatedRefundAmount(estimatedRefundAmount)
                .estimatedRefundCash(estimatedRefundCash)
                .build();
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
            throw new InvalidReasonCategoryException(reasonCategory);
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
                .mapToLong(product -> Math.multiplyExact(product.getUnitAmount(), product.getQuantity().longValue()))
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
        CalculateReturnShippingFeeCommand shippingFeeCommand = CalculateReturnShippingFeeCommand.builder()
                .orderId(command.orderId())
                .reasonCategory(command.reasonCategory())
                .returnPricePolicyIds(command.returnPricePolicyIds())
                .build();

        CalculateReturnShippingFeeResult result = calculateReturnShippingFeeUseCase.calculateReturnShippingFee(shippingFeeCommand);
        return result.returnShippingFee();
    }

    private String resolveRefundMethod(Order order) {
        String orderKey = order.getOrderKey().toString();
        PspPaymentEvent paymentEvent = findPspPaymentEventPort.findByOrderKey(orderKey)
                .orElseThrow(() -> new PaymentEventNotFoundException(orderKey));

        return PaymentMethod.fromMobileCode(paymentEvent.getMethod()).getDescription();
    }

    private long calculateEstimatedRefundCash(Order order, long totalProductAmount, boolean isFullReturn) {
        Long pointAmount = order.getAmount().getPointAmount();
        if (FormatValidator.hasNoValue(pointAmount) || pointAmount <= 0) {
            return 0L;
        }

        if (isFullReturn) {
            return pointAmount;
        }

        Long totalAmount = order.getAmount().getTotalAmount();
        if (FormatValidator.hasNoValue(totalAmount) || totalAmount <= 0) {
            return 0L;
        }

        return Math.multiplyExact(pointAmount, totalProductAmount) / totalAmount;
    }

    private long calculateEstimatedRefundAmount(long totalProductAmount, long returnShippingFee, long estimatedRefundCash) {
        long amount = Math.subtractExact(Math.subtractExact(totalProductAmount, returnShippingFee), estimatedRefundCash);
        return Math.max(amount, 0L);
    }
}
