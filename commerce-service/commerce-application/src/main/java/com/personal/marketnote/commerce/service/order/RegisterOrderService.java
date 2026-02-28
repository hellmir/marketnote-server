package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderCreateState;
import com.personal.marketnote.commerce.domain.order.OrderProductCreateState;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.exception.ExcessiveDiscountException;
import com.personal.marketnote.commerce.exception.OrderAmountMismatchException;
import com.personal.marketnote.commerce.exception.PriceMismatchException;
import com.personal.marketnote.commerce.exception.SellerMismatchException;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.SaveOrderPort;
import com.personal.marketnote.commerce.port.out.payment.SavePaymentPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class RegisterOrderService implements RegisterOrderUseCase {
    private final GetInventoryUseCase getInventoryUseCase;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final SaveOrderPort saveOrderPort;
    private final SavePaymentPort savePaymentPort;

    @Override
    public RegisterOrderResult registerOrder(RegisterOrderCommand command) {
        validateTotalAmountConsistency(command);
        validateDiscountAmounts(command);
        validateUnitAmountsAgainstActualPrices(command);

        Map<Long, Long> productIdsByPricePolicyId = command.orderProducts().stream()
                .collect(Collectors.toMap(
                        OrderProductItemCommand::pricePolicyId,
                        OrderProductItemCommand::productId,
                        (existing, replacement) -> existing
                ));

        Set<Inventory> inventories = getInventoryUseCase.getOrCreateInventories(productIdsByPricePolicyId);
        inventories.forEach(inventory -> {
            int orderQuantity = command.orderProducts().stream()
                    .filter(item -> item.pricePolicyId().equals(inventory.getPricePolicyId()))
                    .map(OrderProductItemCommand::quantity)
                    .reduce(0, Integer::sum);

            inventory.validateIsSufficient(orderQuantity);
        });

        List<OrderProductCreateState> orderProductStates = command.orderProducts().stream()
                .map(item -> OrderProductCreateState.builder()
                        .sellerId(item.sellerId())
                        .pricePolicyId(item.pricePolicyId())
                        .sharerId(item.sharerId())
                        .quantity(item.quantity())
                        .unitAmount(item.unitAmount())
                        .imageUrl(item.imageUrl())
                        .build())
                .toList();

        Order savedOrder = saveOrderPort.save(
                Order.from(
                        OrderCreateState.builder()
                                .buyerId(command.buyerId())
                                .totalAmount(command.totalAmount())
                                .couponAmount(command.couponAmount())
                                .pointAmount(command.pointAmount())
                                .orderProductStates(orderProductStates)
                                .build()
                )
        );

        long couponAmount = resolveAmount(command.couponAmount());
        long pointAmount = resolveAmount(command.pointAmount());
        long paymentAmount = command.totalAmount() - couponAmount - pointAmount;

        if (paymentAmount < 0) {
            log.error("결제 금액 음수 발생 - totalAmount: {}, coupon: {}, point: {}",
                    command.totalAmount(), couponAmount, pointAmount);
            throw new ExcessiveDiscountException(command.totalAmount(), couponAmount, pointAmount);
        }

        savePaymentPort.save(
                Payment.from(
                        PaymentCreateState.builder()
                                .orderId(savedOrder.getId())
                                .orderKey(savedOrder.getOrderKey())
                                .paymentAmount(paymentAmount)
                                .build()
                )
        );

        return RegisterOrderResult.from(savedOrder);
    }

    private void validateTotalAmountConsistency(RegisterOrderCommand command) {
        long calculatedTotal;
        try {
            calculatedTotal = 0L;
            for (OrderProductItemCommand item : command.orderProducts()) {
                long itemTotal = Math.multiplyExact(item.unitAmount(), item.quantity());
                calculatedTotal = Math.addExact(calculatedTotal, itemTotal);
            }
        } catch (ArithmeticException e) {
            log.warn("주문 금액 오버플로우 발생 - 전송된 총액: {}", command.totalAmount());
            throw new OrderAmountMismatchException(command.totalAmount(), -1L);
        }

        if (!command.totalAmount().equals(calculatedTotal)) {
            log.warn("주문 금액 불일치 - 전송된 총액: {}, 계산된 합계: {}", command.totalAmount(), calculatedTotal);
            throw new OrderAmountMismatchException(command.totalAmount(), calculatedTotal);
        }
    }

    private void validateDiscountAmounts(RegisterOrderCommand command) {
        long couponAmount = resolveAmount(command.couponAmount());
        long pointAmount = resolveAmount(command.pointAmount());

        long totalDiscount;
        try {
            totalDiscount = Math.addExact(couponAmount, pointAmount);
        } catch (ArithmeticException e) {
            log.warn("할인 금액 오버플로우 - 쿠폰: {}, 포인트: {}", couponAmount, pointAmount);
            throw new ExcessiveDiscountException(command.totalAmount(), couponAmount, pointAmount);
        }

        if (totalDiscount > command.totalAmount()) {
            log.warn("할인 금액 초과 - 주문 총액: {}, 쿠폰: {}, 포인트: {}", command.totalAmount(), couponAmount, pointAmount);
            throw new ExcessiveDiscountException(command.totalAmount(), couponAmount, pointAmount);
        }
    }

    private void validateUnitAmountsAgainstActualPrices(RegisterOrderCommand command) {
        List<Long> pricePolicyIds = command.orderProducts().stream()
                .map(OrderProductItemCommand::pricePolicyId)
                .distinct()
                .toList();

        if (pricePolicyIds.isEmpty()) {
            return;
        }

        Map<Long, ProductInfoResult> productInfoMap = findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds);

        if (productInfoMap.isEmpty()) {
            log.warn("[PRODUCT_VALIDATION_SKIPPED] product-service 응답 없음 - 가격/판매자 검증 생략. pricePolicyIds: {}", pricePolicyIds);
            return;
        }

        for (OrderProductItemCommand item : command.orderProducts()) {
            ProductInfoResult productInfo = productInfoMap.get(item.pricePolicyId());
            if (FormatValidator.hasNoValue(productInfo)) {
                log.warn("[PRODUCT_VALIDATION_SKIPPED] 상품 정보 누락 - pricePolicyId: {}, 전송된 단가: {}",
                        item.pricePolicyId(), item.unitAmount());
                continue;
            }

            Long actualSellerId = productInfo.sellerId();
            if (FormatValidator.hasValue(actualSellerId) && !actualSellerId.equals(item.sellerId())) {
                log.warn("판매자 불일치 - pricePolicyId: {}, 전송된 sellerId: {} (실제 판매자와 불일치)",
                        item.pricePolicyId(), item.sellerId());
                throw new SellerMismatchException(item.pricePolicyId(), item.sellerId(), actualSellerId);
            }

            Long actualPrice = productInfo.getSellingPrice();
            if (FormatValidator.hasValue(actualPrice) && !actualPrice.equals(item.unitAmount())) {
                log.warn("단가 불일치 - pricePolicyId: {}, 전송된 단가: {}, 실제 가격: {}",
                        item.pricePolicyId(), item.unitAmount(), actualPrice);
                throw new PriceMismatchException(item.pricePolicyId(), item.unitAmount(), actualPrice);
            }
        }
    }

    private long resolveAmount(Long amount) {
        return FormatValidator.hasValue(amount) ? amount : 0L;
    }
}
