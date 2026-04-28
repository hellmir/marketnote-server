package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderAmount;
import com.personal.marketnote.commerce.domain.order.OrderProductCreateState;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.mapper.OrderCommandToStateMapper;
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
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingRegionType;
import com.personal.marketnote.commerce.port.out.result.user.ShippingAddressInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import com.personal.marketnote.commerce.port.out.user.FindUserShippingAddressPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class RegisterOrderService implements RegisterOrderUseCase {
    private static final Executor VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final GetInventoryUseCase getInventoryUseCase;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;
    private final SaveOrderPort saveOrderPort;
    private final SavePaymentPort savePaymentPort;
    private final SavePaymentAllocationPort savePaymentAllocationPort;
    private final ModifyUserPointPort modifyUserPointPort;
    private final FindUserShippingAddressPort findUserShippingAddressPort;

    @Override
    public RegisterOrderResult registerOrder(RegisterOrderCommand command) {
        validateTotalAmountConsistency(command);
        validateDiscountAmounts(command);
        validatePointBalance(command);

        List<Long> pricePolicyIds = command.orderProducts().stream()
                .map(OrderProductItemCommand::pricePolicyId)
                .distinct()
                .toList();

        List<Long> sellerIds = command.orderProducts().stream()
                .map(OrderProductItemCommand::sellerId)
                .distinct()
                .toList();

        // 상품 정보는 HTTP, 배송비 정책은 로컬 Read Model DB 조회. Virtual Thread 병렬 조회 유지.
        CompletableFuture<Map<Long, ProductInfoResult>> productInfoFuture =
                CompletableFuture.supplyAsync(() -> fetchProductInfoByPricePolicyIds(pricePolicyIds), VIRTUAL_EXECUTOR);
        CompletableFuture<Map<Long, ShippingPolicyInfoResult>> shippingPolicyFuture =
                CompletableFuture.supplyAsync(() -> fetchShippingPoliciesBySellerIds(sellerIds), VIRTUAL_EXECUTOR);

        Map<Long, ProductInfoResult> productInfoMap = joinFuture(productInfoFuture);
        Map<Long, ShippingPolicyInfoResult> shippingPolicies = joinFuture(shippingPolicyFuture);

        validateUnitAmountsAgainstActualPrices(command, productInfoMap);

        ShippingAddressInfoResult addressInfo = findUserShippingAddressPort.findByIdAndUserId(
                command.shippingAddressId(), command.buyerId()
        );
        ShippingRegionType regionType = ShippingRegionType.from(addressInfo.regionType());

        validateShippingFee(command, shippingPolicies, regionType);

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

        List<OrderProductCreateState> orderProductStates = OrderCommandToStateMapper.mapToOrderProductStates(command.orderProducts(), productInfoMap);

        OrderAmount orderAmount = OrderAmount.from(
                OrderCommandToStateMapper.mapToOrderAmountState(command.amount())
        );

        ShippingAddress shippingAddress = resolveShippingAddress(command, addressInfo);

        Order savedOrder = saveOrderPort.save(
                Order.from(
                        OrderCommandToStateMapper.mapToOrderState(
                                command.buyerId(), orderAmount, shippingAddress, orderProductStates)
                )
        );

        long couponAmount = resolveAmount(command.amount().couponAmount());
        long pointAmount = resolveAmount(command.amount().pointAmount());
        long shippingFee = resolveAmount(command.amount().shippingFee());
        long payableAmount = Math.addExact(command.amount().totalAmount(), shippingFee);
        long totalDiscount = Math.addExact(couponAmount, pointAmount);
        long paymentAmount = Math.subtractExact(payableAmount, totalDiscount);

        if (paymentAmount < 0) {
            log.error("결제 금액 음수 발생 - totalAmount: {}, shippingFee: {}, coupon: {}, point: {}",
                    command.amount().totalAmount(), shippingFee, couponAmount, pointAmount);
            throw new ExcessiveDiscountException(command.amount().totalAmount(), shippingFee, couponAmount, pointAmount);
        }

        savePaymentPort.save(
                Payment.from(
                        OrderCommandToStateMapper.mapToPaymentState(
                                savedOrder.getId(), savedOrder.getOrderKey(), paymentAmount)
                )
        );

        createPaymentAllocations(savedOrder.getId(), command.orderProducts(), shippingPolicies, regionType);

        return RegisterOrderResult.from(savedOrder);
    }

    private void createPaymentAllocations(Long orderId, List<OrderProductItemCommand> orderProducts,
                                          Map<Long, ShippingPolicyInfoResult> shippingPolicies,
                                          ShippingRegionType regionType) {
        Map<Long, Long> sellerGrossAmounts = orderProducts.stream()
                .collect(Collectors.groupingBy(
                        OrderProductItemCommand::sellerId,
                        Collectors.summingLong(item ->
                                Math.multiplyExact(item.unitAmount(), (long) item.quantity()))
                ));

        Map<Long, Long> sellerShippingFees = calculateSellerShippingFees(sellerGrossAmounts, shippingPolicies, regionType);

        List<PaymentAllocation> allocations = sellerGrossAmounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> {
                    Long sellerId = entry.getKey();
                    Long sellerShippingFee = sellerShippingFees.getOrDefault(sellerId, 0L);
                    return PaymentAllocation.from(
                            OrderCommandToStateMapper.mapToPaymentAllocationState(
                                    orderId, sellerId, entry.getValue(), sellerShippingFee)
                    );
                })
                .toList();

        if (!allocations.isEmpty()) {
            savePaymentAllocationPort.saveAll(allocations);
        }
    }

    private Map<Long, Long> calculateSellerShippingFees(
            Map<Long, Long> sellerAmounts,
            Map<Long, ShippingPolicyInfoResult> shippingPolicies,
            ShippingRegionType regionType
    ) {
        Map<Long, Long> sellerShippingFees = new java.util.HashMap<>();
        for (Map.Entry<Long, Long> entry : sellerAmounts.entrySet()) {
            Long sellerId = entry.getKey();
            Long sellerAmount = entry.getValue();

            ShippingPolicyInfoResult policy = shippingPolicies.get(sellerId);
            if (FormatValidator.hasNoValue(policy)) {
                sellerShippingFees.put(sellerId, 0L);
                continue;
            }

            long baseFee = sellerAmount < policy.freeShippingThreshold() ? policy.shippingFee() : 0L;
            long surcharge = resolveSurcharge(policy, regionType);
            sellerShippingFees.put(sellerId, Math.addExact(baseFee, surcharge));
        }
        return sellerShippingFees;
    }

    private long resolveSurcharge(ShippingPolicyInfoResult policy, ShippingRegionType regionType) {
        if (regionType.isJeju()) {
            return resolveAmount(policy.jejuSurcharge());
        }
        if (regionType.isIsland()) {
            return resolveAmount(policy.islandSurcharge());
        }
        return 0L;
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
            log.warn("주문 금액 오버플로우 발생 - 전송된 총액: {}", command.amount().totalAmount());
            throw new OrderAmountMismatchException(command.amount().totalAmount(), -1L);
        }

        if (!command.amount().totalAmount().equals(calculatedTotal)) {
            log.warn("주문 금액 불일치 - 전송된 총액: {}, 계산된 합계: {}", command.amount().totalAmount(), calculatedTotal);
            throw new OrderAmountMismatchException(command.amount().totalAmount(), calculatedTotal);
        }
    }

    private void validateDiscountAmounts(RegisterOrderCommand command) {
        long couponAmount = resolveAmount(command.amount().couponAmount());
        long pointAmount = resolveAmount(command.amount().pointAmount());
        long shippingFee = resolveAmount(command.amount().shippingFee());

        long totalDiscount;
        long payableAmount;
        try {
            totalDiscount = Math.addExact(couponAmount, pointAmount);
            payableAmount = Math.addExact(command.amount().totalAmount(), shippingFee);
        } catch (ArithmeticException e) {
            log.warn("할인/배송비 금액 오버플로우 - 쿠폰: {}, 포인트: {}, 배송비: {}", couponAmount, pointAmount, shippingFee);
            throw new ExcessiveDiscountException(command.amount().totalAmount(), shippingFee, couponAmount, pointAmount);
        }

        if (totalDiscount > payableAmount) {
            log.warn("할인 금액 초과 - 주문 총액: {}, 배송비: {}, 쿠폰: {}, 포인트: {}", command.amount().totalAmount(), shippingFee, couponAmount, pointAmount);
            throw new ExcessiveDiscountException(command.amount().totalAmount(), shippingFee, couponAmount, pointAmount);
        }
    }

    private Map<Long, ProductInfoResult> fetchProductInfoByPricePolicyIds(List<Long> pricePolicyIds) {
        if (pricePolicyIds.isEmpty()) {
            return Map.of();
        }

        return findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds);
    }

    private Map<Long, ShippingPolicyInfoResult> fetchShippingPoliciesBySellerIds(List<Long> sellerIds) {
        if (sellerIds.isEmpty()) {
            return Map.of();
        }

        return findShippingPolicyBySellerIdsPort.findBySellerIds(sellerIds);
    }

    private <T> T joinFuture(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw ce;
        }
    }

    private void validateUnitAmountsAgainstActualPrices(RegisterOrderCommand command,
                                                        Map<Long, ProductInfoResult> productInfoMap) {
        if (productInfoMap.isEmpty()) {
            log.warn("[PRODUCT_VALIDATION_SKIPPED] product-service 응답 없음 - 가격/판매자 검증 생략. pricePolicyIds: {}",
                    command.orderProducts().stream().map(OrderProductItemCommand::pricePolicyId).distinct().toList());
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
        long pointAmount = resolveAmount(command.amount().pointAmount());
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

    private void validateShippingFee(RegisterOrderCommand command,
                                     Map<Long, ShippingPolicyInfoResult> shippingPolicies,
                                     ShippingRegionType regionType) {
        long requestedShippingFee = resolveAmount(command.amount().shippingFee());

        if (shippingPolicies.isEmpty()) {
            log.warn("[SHIPPING_VALIDATION_SKIPPED] product-service 응답 없음 - 배송비 검증 생략. sellerIds: {}",
                    command.orderProducts().stream().map(OrderProductItemCommand::sellerId).distinct().toList());
            return;
        }

        long calculatedShippingFee = calculateExpectedShippingFee(command.orderProducts(), shippingPolicies, regionType);

        if (requestedShippingFee != calculatedShippingFee) {
            log.warn("배송비 불일치 - 전송된 배송비: {}, 계산된 배송비: {}", requestedShippingFee, calculatedShippingFee);
            throw new ShippingFeeMismatchException(requestedShippingFee, calculatedShippingFee);
        }
    }

    private long calculateExpectedShippingFee(
            List<OrderProductItemCommand> orderProducts,
            Map<Long, ShippingPolicyInfoResult> shippingPolicies,
            ShippingRegionType regionType
    ) {
        Map<Long, Long> sellerAmounts = orderProducts.stream()
                .collect(Collectors.groupingBy(
                        OrderProductItemCommand::sellerId,
                        Collectors.summingLong(item ->
                                Math.multiplyExact(item.unitAmount(), (long) item.quantity()))
                ));

        Map<Long, Long> sellerShippingFees = calculateSellerShippingFees(sellerAmounts, shippingPolicies, regionType);

        long totalShippingFee = 0L;
        for (Long fee : sellerShippingFees.values()) {
            totalShippingFee = Math.addExact(totalShippingFee, fee);
        }
        return totalShippingFee;
    }

    private ShippingAddress resolveShippingAddress(RegisterOrderCommand command,
                                                   ShippingAddressInfoResult addressInfo) {
        return ShippingAddress.of(
                addressInfo.recipientName(),
                addressInfo.recipientPhoneNumber(),
                null,
                addressInfo.address(),
                addressInfo.addressDetail(),
                command.deliveryRequestType(),
                command.deliveryRequestMessage()
        );
    }

    private long resolveAmount(Long amount) {
        return FormatValidator.hasValue(amount) ? amount : 0L;
    }
}
