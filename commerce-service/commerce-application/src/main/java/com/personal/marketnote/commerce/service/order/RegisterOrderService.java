package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderCreateState;
import com.personal.marketnote.commerce.domain.order.OrderProductCreateState;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationCreateState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.SaveOrderPort;
import com.personal.marketnote.commerce.port.out.payment.SavePaymentPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
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
    private final FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;
    private final SaveOrderPort saveOrderPort;
    private final SavePaymentPort savePaymentPort;
    private final SavePaymentAllocationPort savePaymentAllocationPort;
    private final ModifyUserPointPort modifyUserPointPort;

    @Override
    public RegisterOrderResult registerOrder(RegisterOrderCommand command) {
        validateTotalAmountConsistency(command);
        validateDiscountAmounts(command);
        validatePointBalance(command);
        validateUnitAmountsAgainstActualPrices(command);
        validateShippingFee(command);

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
                                .amount(command.amount())
                                .shippingAddress(command.shippingAddress())
                                .orderProductStates(orderProductStates)
                                .build()
                )
        );

        long couponAmount = resolveAmount(command.amount().getCouponAmount());
        long pointAmount = resolveAmount(command.amount().getPointAmount());
        long shippingFee = resolveAmount(command.amount().getShippingFee());
        long payableAmount = Math.addExact(command.amount().getTotalAmount(), shippingFee);
        long totalDiscount = Math.addExact(couponAmount, pointAmount);
        long paymentAmount = Math.subtractExact(payableAmount, totalDiscount);

        if (paymentAmount < 0) {
            log.error("결제 금액 음수 발생 - totalAmount: {}, shippingFee: {}, coupon: {}, point: {}",
                    command.amount().getTotalAmount(), shippingFee, couponAmount, pointAmount);
            throw new ExcessiveDiscountException(command.amount().getTotalAmount(), shippingFee, couponAmount, pointAmount);
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

        createPaymentAllocations(savedOrder.getId(), command.orderProducts());

        return RegisterOrderResult.from(savedOrder);
    }

    private void createPaymentAllocations(Long orderId, List<OrderProductItemCommand> orderProducts) {
        Map<Long, Long> sellerGrossAmounts = orderProducts.stream()
                .collect(Collectors.groupingBy(
                        OrderProductItemCommand::sellerId,
                        Collectors.summingLong(item ->
                                Math.multiplyExact(item.unitAmount(), (long) item.quantity()))
                ));

        List<PaymentAllocation> allocations = sellerGrossAmounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> PaymentAllocation.from(
                        PaymentAllocationCreateState.builder()
                                .orderId(orderId)
                                .sellerId(entry.getKey())
                                .allocatedAmount(entry.getValue())
                                .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                                .targetType(PaymentAllocationTargetType.ORDER)
                                .idempotencyKey("ORDER_ALLOCATION:" + orderId + ":" + entry.getKey())
                                .build()
                ))
                .toList();

        if (!allocations.isEmpty()) {
            savePaymentAllocationPort.saveAll(allocations);
        }
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
            log.warn("주문 금액 오버플로우 발생 - 전송된 총액: {}", command.amount().getTotalAmount());
            throw new OrderAmountMismatchException(command.amount().getTotalAmount(), -1L);
        }

        if (!command.amount().getTotalAmount().equals(calculatedTotal)) {
            log.warn("주문 금액 불일치 - 전송된 총액: {}, 계산된 합계: {}", command.amount().getTotalAmount(), calculatedTotal);
            throw new OrderAmountMismatchException(command.amount().getTotalAmount(), calculatedTotal);
        }
    }

    private void validateDiscountAmounts(RegisterOrderCommand command) {
        long couponAmount = resolveAmount(command.amount().getCouponAmount());
        long pointAmount = resolveAmount(command.amount().getPointAmount());
        long shippingFee = resolveAmount(command.amount().getShippingFee());

        long totalDiscount;
        long payableAmount;
        try {
            totalDiscount = Math.addExact(couponAmount, pointAmount);
            payableAmount = Math.addExact(command.amount().getTotalAmount(), shippingFee);
        } catch (ArithmeticException e) {
            log.warn("할인/배송비 금액 오버플로우 - 쿠폰: {}, 포인트: {}, 배송비: {}", couponAmount, pointAmount, shippingFee);
            throw new ExcessiveDiscountException(command.amount().getTotalAmount(), shippingFee, couponAmount, pointAmount);
        }

        if (totalDiscount > payableAmount) {
            log.warn("할인 금액 초과 - 주문 총액: {}, 배송비: {}, 쿠폰: {}, 포인트: {}", command.amount().getTotalAmount(), shippingFee, couponAmount, pointAmount);
            throw new ExcessiveDiscountException(command.amount().getTotalAmount(), shippingFee, couponAmount, pointAmount);
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

    private void validatePointBalance(RegisterOrderCommand command) {
        long pointAmount = resolveAmount(command.amount().getPointAmount());
        if (pointAmount <= 0) {
            return;
        }

        Long availablePoints = modifyUserPointPort.getAvailablePoints(command.buyerId());
        if (availablePoints < pointAmount) {
            log.warn("포인트 잔액 부족 - buyerId: {}, 요청: {}, 보유: {}",
                    command.buyerId(), pointAmount, availablePoints);
            throw new InsufficientPointException(pointAmount, availablePoints);
        }
    }

    private void validateShippingFee(RegisterOrderCommand command) {
        long requestedShippingFee = resolveAmount(command.amount().getShippingFee());

        List<Long> sellerIds = command.orderProducts().stream()
                .map(OrderProductItemCommand::sellerId)
                .distinct()
                .toList();

        if (sellerIds.isEmpty()) {
            return;
        }

        Map<Long, ShippingPolicyInfoResult> shippingPolicies =
                findShippingPolicyBySellerIdsPort.findBySellerIds(sellerIds);

        if (shippingPolicies.isEmpty()) {
            log.warn("[SHIPPING_VALIDATION_SKIPPED] product-service 응답 없음 - 배송비 검증 생략. sellerIds: {}", sellerIds);
            return;
        }

        long calculatedShippingFee = calculateExpectedShippingFee(command.orderProducts(), shippingPolicies);

        if (requestedShippingFee != calculatedShippingFee) {
            log.warn("배송비 불일치 - 전송된 배송비: {}, 계산된 배송비: {}", requestedShippingFee, calculatedShippingFee);
            throw new ShippingFeeMismatchException(requestedShippingFee, calculatedShippingFee);
        }
    }

    private long calculateExpectedShippingFee(
            List<OrderProductItemCommand> orderProducts,
            Map<Long, ShippingPolicyInfoResult> shippingPolicies
    ) {
        Map<Long, Long> sellerAmounts = orderProducts.stream()
                .collect(Collectors.groupingBy(
                        OrderProductItemCommand::sellerId,
                        Collectors.summingLong(item ->
                                Math.multiplyExact(item.unitAmount(), (long) item.quantity()))
                ));

        long totalShippingFee = 0L;
        for (Map.Entry<Long, Long> entry : sellerAmounts.entrySet()) {
            Long sellerId = entry.getKey();
            Long sellerAmount = entry.getValue();

            ShippingPolicyInfoResult policy = shippingPolicies.get(sellerId);
            if (FormatValidator.hasNoValue(policy)) {
                continue;
            }

            if (sellerAmount < policy.freeShippingThreshold()) {
                totalShippingFee = Math.addExact(totalShippingFee, policy.shippingFee());
            }
        }

        return totalShippingFee;
    }

    private long resolveAmount(Long amount) {
        return FormatValidator.hasValue(amount) ? amount : 0L;
    }
}
