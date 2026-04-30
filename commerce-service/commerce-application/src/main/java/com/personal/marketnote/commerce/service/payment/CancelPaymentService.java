package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundCreateState;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.shipping.ShippingFeeCalculator;
import com.personal.marketnote.commerce.domain.shipping.ShippingFeeContext;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.CancelPaymentUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishPaymentEventPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class CancelPaymentService implements CancelPaymentUseCase {
    private static final String PSP_FULL_CANCEL_TYPE_CODE = "STSC";
    private static final String PSP_PARTIAL_CANCEL_TYPE_CODE = "STPC";

    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final PaymentVendorPort paymentVendorPort;
    private final RestoreProductInventoryUseCase restoreProductInventoryUseCase;
    private final ModifyUserPointPort modifyUserPointPort;
    private final SaveRefundPort saveRefundPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final PublishPaymentEventPort publishPaymentEventPort;
    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;

    @Override
    public void cancel(CancelPaymentCommand command) {
        UUID orderKeyUuid = UUID.fromString(command.orderKey());
        Payment payment = findPaymentPort.findByOrderKey(orderKeyUuid)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        Order order = findVerifiedOrder(payment.getOrderId(), command.buyerId());

        PspPaymentEvent event = findPspPaymentEventPort.findByOrderKey(command.orderKey())
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        String transactionNumber = event.getPgPaymentKey();
        boolean isFullCancel = command.isFullCancel();
        String modType = isFullCancel
                ? PSP_FULL_CANCEL_TYPE_CODE
                : PSP_PARTIAL_CANCEL_TYPE_CODE;

        Long alreadyRefunded = FormatValidator.hasValue(payment.getRefundAmount()) ? payment.getRefundAmount() : 0L;
        Long refundableAmount = Math.subtractExact(payment.getPaymentAmount(), alreadyRefunded);

        Long cancelAmount = computeCancelAmount(isFullCancel, command.cancelAmount(), refundableAmount);

        if (!isFullCancel && command.hasCancelProducts()) {
            validateCancelProducts(order, command.cancelProducts());
        }

        long shippingFeeDeduction = 0L;
        if (!isFullCancel && command.hasCancelProducts()) {
            shippingFeeDeduction = calculateShippingFeeDeductionForPartialCancel(order, command);
        }

        Long adjustedCancelAmount = cancelAmount;
        String adjustedCancelReason = command.cancelReason();
        if (shippingFeeDeduction > 0L) {
            adjustedCancelAmount = Math.subtractExact(cancelAmount, shippingFeeDeduction);
            if (adjustedCancelAmount <= 0L) {
                adjustedCancelAmount = 0L;
            }
            adjustedCancelReason = command.cancelReason()
                    + " (무료 배송 기준 미달로 인한 배송비 차감 " + shippingFeeDeduction + "원)";
            log.info("부분 취소 배송비 재계산 - orderId: {}, 원래 환불액: {}, 배송비 차감: {}, 조정 환불액: {}",
                    order.getId(), cancelAmount, shippingFeeDeduction, adjustedCancelAmount);
        }

        Long remainAmount = Math.subtractExact(refundableAmount, adjustedCancelAmount);

        if (adjustedCancelAmount <= 0L) {
            log.info("부분 취소 환불 금액이 배송비 차감으로 0원 - orderId: {}, PG 환불 생략", order.getId());
            updateDomain(isFullCancel, payment, event, null, 0L);
            updatePaymentPort.update(payment);
            updatePspPaymentEventPort.update(event);
            saveRefundRecordWithoutPg(payment, cancelAmount, shippingFeeDeduction, adjustedCancelReason);
            restorePartialCancelInventory(order, command);
            Long pgSkipDeduction = resolveDeductionPoint(
                    order.getOrderProducts(), command, payment.getPaymentAmount(), cancelAmount);
            Long pgSkipBuyerId = order.getBuyerId();
            Long pgSkipOrderId = order.getId();
            runAfterCommit(() -> reducePartialPendingProductAccumulationPoints(
                    pgSkipBuyerId, pgSkipOrderId, pgSkipDeduction));
            runAfterCommit(() -> reducePartialPendingSharedPurchasePoints(
                    order, payment.getPaymentAmount(), cancelAmount));
            List<OrderProduct> pgSkipCancelProducts = resolveCancelProducts(isFullCancel, command);
            publishPaymentCancelledEvent(
                    order.getId(), command.orderKey(), order.getBuyerId(), 0L,
                    payment.getPaymentAmount(), order.getAmount().getPointAmount(), isFullCancel, alreadyRefunded,
                    UUID.randomUUID().toString(), order.getOrderProducts(), pgSkipCancelProducts, pgSkipDeduction);
            return;
        }

        PaymentCancelVendorResult vendorResult = requestPaymentCancellationToPsp(
                transactionNumber, modType, adjustedCancelAmount, remainAmount, adjustedCancelReason
        );

        if (!vendorResult.success()) {
            throw new PaymentCancelException(
                    "결제 취소 실패 [" + vendorResult.resultCode() + "]: " + vendorResult.resultMessage()
            );
        }

        updateDomain(isFullCancel, payment, event, vendorResult, adjustedCancelAmount);
        updatePaymentPort.update(payment);
        updatePspPaymentEventPort.update(event);

        saveRefundRecord(payment, isFullCancel, adjustedCancelAmount, adjustedCancelReason, vendorResult);

        Long partialProductPendingDeduction = null;
        if (isFullCancel) {
            restoreInventory(order);
            refundPoints(order);
        } else {
            restorePartialCancelInventory(order, command);
            partialProductPendingDeduction = resolveDeductionPoint(
                    order.getOrderProducts(), command, payment.getPaymentAmount(), cancelAmount);
            Long buyerId = order.getBuyerId();
            Long orderId = order.getId();
            Long precomputedDeduction = partialProductPendingDeduction;
            runAfterCommit(() -> reducePartialPendingProductAccumulationPoints(
                    buyerId, orderId, precomputedDeduction));
            runAfterCommit(() -> reducePartialPendingSharedPurchasePoints(
                    order, payment.getPaymentAmount(), cancelAmount));
        }

        // [#929][#1034] 결제 취소 역분개는 Kafka Consumer(PaymentCancelledLedgerConsumer)로 전환 완료
        String cancelId = isFullCancel ? null : UUID.randomUUID().toString();

        List<OrderProduct> cancelTargetProducts = resolveCancelProducts(isFullCancel, command);

        // Outbox 이벤트 저장 (트랜잭션 내)
        publishPaymentCancelledEvent(
                order.getId(), command.orderKey(), order.getBuyerId(), cancelAmount,
                payment.getPaymentAmount(), order.getAmount().getPointAmount(), isFullCancel, alreadyRefunded,
                cancelId, order.getOrderProducts(), cancelTargetProducts,
                partialProductPendingDeduction);
    }

    private Long computeCancelAmount(boolean isFullCancel, Long partialCancelAmount, Long refundableAmount) {
        if (isFullCancel) {
            return refundableAmount;
        }

        if (FormatValidator.hasNoValue(partialCancelAmount) || partialCancelAmount <= 0L) {
            throw new InvalidCancelAmountException("부분취소 금액은 0보다 커야 합니다");
        }

        if (partialCancelAmount > refundableAmount) {
            throw new InvalidCancelAmountException(
                    "취소 금액(" + partialCancelAmount + ")이 환불 가능 금액(" + refundableAmount + ")을 초과합니다"
            );
        }

        return partialCancelAmount;
    }

    private PaymentCancelVendorResult requestPaymentCancellationToPsp(
            String transactionNumber, String cancelType, Long cancelAmount, Long remainAmount, String cancelReason
    ) {
        PaymentCancelVendorCommand vendorCommand = PaymentCancelVendorCommand.builder()
                .transactionId(transactionNumber)
                .cancelType(cancelType)
                .cancelAmount(cancelAmount)
                .remainAmount(remainAmount)
                .cancelReason(cancelReason)
                .build();

        return paymentVendorPort.cancelPayment(vendorCommand);
    }

    private void updateDomain(
            boolean isFullCancel, Payment payment, PspPaymentEvent event,
            PaymentCancelVendorResult vendorResult, Long cancelAmount
    ) {
        if (isFullCancel) {
            payment.markAsRefunded();
            String rawResponse = FormatValidator.hasValue(vendorResult) ? vendorResult.rawResponse() : null;
            event.cancel(rawResponse);
            return;
        }

        payment.markAsPartiallyRefunded(cancelAmount);
        String rawResponse = FormatValidator.hasValue(vendorResult) ? vendorResult.rawResponse() : null;
        event.partialRefund(rawResponse);
    }

    private void saveRefundRecord(
            Payment payment, boolean isFullCancel, Long cancelAmount,
            String cancelReason, PaymentCancelVendorResult vendorResult
    ) {
        try {
            RefundType refundType = isFullCancel ? RefundType.FULL_REFUND : RefundType.PARTIAL_REFUND;
            RefundCreateState createState = RefundCreateState.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .refundType(refundType)
                    .refundAmount(cancelAmount)
                    .cancelReason(cancelReason)
                    .processedBy("SYSTEM")
                    .pgRefundKey(payment.getPgPaymentKey())
                    .pgRawResponse(vendorResult.rawResponse())
                    .build();
            Refund refund = Refund.from(createState);
            saveRefundPort.save(refund);
        } catch (Exception e) {
            log.error("환불 상세 기록 저장 실패 - orderId: {}, error: {}",
                    payment.getOrderId(), e.getMessage(), e);
        }
    }

    private long calculateShippingFeeDeductionForPartialCancel(Order order, CancelPaymentCommand command) {
        try {
            List<PaymentAllocation> allocations = findPaymentAllocationPort.findByOrderId(order.getId());
            if (allocations.isEmpty()) {
                return 0L;
            }

            Map<Long, Long> initialShippingFeeBySellerIdMap = allocations.stream()
                    .collect(Collectors.toMap(
                            PaymentAllocation::getSellerId,
                            PaymentAllocation::getShippingFee,
                            (existing, replacement) -> existing
                    ));

            Map<Long, Long> sellerTotalAmounts = order.getOrderProducts().stream()
                    .collect(Collectors.groupingBy(
                            OrderProduct::getSellerId,
                            Collectors.summingLong(op -> Math.multiplyExact(op.getUnitAmount(), op.getQuantity().longValue()))
                    ));

            Map<Long, Long> sellerCancelAmounts = command.cancelProducts().stream()
                    .collect(Collectors.groupingBy(
                            item -> resolveSellerIdForCancelProduct(order, item.pricePolicyId()),
                            Collectors.summingLong(item -> {
                                OrderProduct orderProduct = findOrderProductByPricePolicyId(order, item.pricePolicyId());
                                return Math.multiplyExact(orderProduct.getUnitAmount(), item.quantity().longValue());
                            })
                    ));

            List<Long> targetSellerIds = initialShippingFeeBySellerIdMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == 0L)
                    .map(Map.Entry::getKey)
                    .filter(sellerCancelAmounts::containsKey)
                    .toList();

            if (targetSellerIds.isEmpty()) {
                return 0L;
            }

            Map<Long, ShippingPolicyInfoResult> shippingPolicies =
                    findShippingPolicyBySellerIdsPort.findBySellerIds(targetSellerIds);

            long totalDeduction = 0L;
            for (Long sellerId : targetSellerIds) {
                ShippingPolicyInfoResult policy = shippingPolicies.get(sellerId);
                if (FormatValidator.hasNoValue(policy)) {
                    continue;
                }

                long sellerTotal = sellerTotalAmounts.getOrDefault(sellerId, 0L);
                long sellerCancel = sellerCancelAmounts.getOrDefault(sellerId, 0L);
                long remainingAmount = Math.subtractExact(sellerTotal, sellerCancel);

                ShippingFeeContext context = ShippingFeeContext.of(
                        remainingAmount, policy.shippingFee(), policy.freeShippingThreshold()
                );
                long baseFee = ShippingFeeCalculator.calculateBaseFee(context);
                totalDeduction = Math.addExact(totalDeduction, baseFee);
            }
            return totalDeduction;
        } catch (Exception e) {
            log.error("부분 취소 배송비 재계산 실패 - orderId: {}, error: {}", order.getId(), e.getMessage(), e);
            return 0L;
        }
    }

    private Long resolveSellerIdForCancelProduct(Order order, Long pricePolicyId) {
        return order.getOrderProducts().stream()
                .filter(op -> op.getPricePolicyId().equals(pricePolicyId))
                .map(OrderProduct::getSellerId)
                .findFirst()
                .orElse(0L);
    }

    private OrderProduct findOrderProductByPricePolicyId(Order order, Long pricePolicyId) {
        return order.getOrderProducts().stream()
                .filter(op -> op.getPricePolicyId().equals(pricePolicyId))
                .findFirst()
                .orElseThrow(() -> new InvalidCancelProductException(
                        "주문에 존재하지 않는 상품입니다. pricePolicyId=" + pricePolicyId));
    }

    private void saveRefundRecordWithoutPg(
            Payment payment, Long originalCancelAmount, long shippingFeeDeduction, String cancelReason
    ) {
        try {
            RefundCreateState createState = RefundCreateState.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .refundType(RefundType.PARTIAL_REFUND)
                    .refundAmount(0L)
                    .cancelReason(cancelReason)
                    .processedBy("SYSTEM")
                    .build();
            Refund refund = Refund.from(createState);
            saveRefundPort.save(refund);
        } catch (Exception e) {
            log.error("배송비 차감 환불 기록 저장 실패 - orderId: {}, error: {}",
                    payment.getOrderId(), e.getMessage(), e);
        }
    }

    private Order findVerifiedOrder(Long orderId, Long buyerId) {
        Order order = findOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getBuyerId().equals(buyerId)) {
            log.warn("결제 취소 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}",
                    orderId, order.getBuyerId(), buyerId);
            throw new UnauthorizedOrderAccessException();
        }
        return order;
    }

    private void restoreInventory(Order order) {
        try {
            restoreProductInventoryUseCase.restore(
                    order.getOrderProducts(), order.getId(), "주문 전액 취소에 의한 재고 복구"
            );
        } catch (Exception e) {
            log.error("재고 복구 실패 - orderId: {}, error: {}",
                    order.getId(), e.getMessage(), e);
        }
    }

    private void restorePartialCancelInventory(Order order, CancelPaymentCommand command) {
        if (!command.hasCancelProducts()) {
            return;
        }

        try {
            List<OrderProduct> cancelTargetProducts = command.cancelProducts().stream()
                    .map(item -> OrderProduct.from(
                            OrderProductSnapshotState.builder()
                                    .pricePolicyId(item.pricePolicyId())
                                    .quantity(item.quantity())
                                    .build()
                    ))
                    .toList();
            restoreProductInventoryUseCase.restore(cancelTargetProducts, order.getId(), "주문 부분 취소에 의한 재고 복구");
        } catch (Exception e) {
            log.error("부분 취소 재고 복구 실패 - orderId: {}, error: {}",
                    order.getId(), e.getMessage(), e);
        }
    }

    private void validateCancelProducts(Order order, List<CancelPaymentCommand.CancelProductItem> cancelProducts) {
        long distinctCount = cancelProducts.stream()
                .map(CancelPaymentCommand.CancelProductItem::pricePolicyId)
                .distinct()
                .count();
        if (distinctCount != cancelProducts.size()) {
            throw new InvalidCancelProductException("중복된 상품이 포함되어 있습니다.");
        }

        Map<Long, Integer> orderQuantityByPricePolicyId = order.getOrderProducts().stream()
                .collect(Collectors.toMap(OrderProduct::getPricePolicyId, OrderProduct::getQuantity, Integer::sum));

        for (CancelPaymentCommand.CancelProductItem item : cancelProducts) {
            if (FormatValidator.hasNoValue(item.quantity()) || item.quantity() <= 0) {
                throw new InvalidCancelProductException(
                        "취소 수량은 1 이상이어야 합니다. pricePolicyId=" + item.pricePolicyId());
            }
            Integer orderQuantity = orderQuantityByPricePolicyId.get(item.pricePolicyId());
            if (FormatValidator.hasNoValue(orderQuantity)) {
                throw new InvalidCancelProductException(
                        "주문에 존재하지 않는 상품입니다. pricePolicyId=" + item.pricePolicyId());
            }
            if (item.quantity() > orderQuantity) {
                throw new InvalidCancelProductException(
                        "취소 수량이 주문 수량을 초과합니다. pricePolicyId=" + item.pricePolicyId()
                                + ", 주문수량=" + orderQuantity + ", 취소수량=" + item.quantity());
            }
        }
    }

    private List<UUID> extractSharerKeys(Order order) {
        return order.getOrderProducts().stream()
                .map(OrderProduct::getSharerKey)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void reducePartialPendingSharedPurchasePoints(Order order, Long paymentAmount, Long cancelAmount) {
        List<UUID> sharerKeys = extractSharerKeys(order);
        if (sharerKeys.isEmpty()) {
            return;
        }

        try {
            modifyUserPointPort.reducePartialPendingSharedPurchasePoints(
                    sharerKeys, paymentAmount, cancelAmount, order.getId());
        } catch (Exception e) {
            log.error("부분 취소 공유 적립 예정 포인트 차감 실패 - orderId: {}, sharerKeys: {}, error: {}",
                    order.getId(), sharerKeys, e.getMessage(), e);
        }
    }

    private void reducePartialPendingProductAccumulationPoints(
            Long buyerId, Long orderId, Long precomputedDeduction
    ) {
        try {
            if (FormatValidator.hasNoValue(precomputedDeduction) || precomputedDeduction <= 0) {
                return;
            }

            modifyUserPointPort.reducePartialPendingPoints(buyerId, precomputedDeduction, orderId);
        } catch (Exception e) {
            log.error("부분 취소 상품 적립 예정 포인트 차감 실패 - orderId: {}, buyerId: {}, error: {}",
                    orderId, buyerId, e.getMessage(), e);
        }
    }

    private Long calculateTotalAccumulatedPoint(List<OrderProduct> orderProducts) {
        boolean allHaveSnapshot = orderProducts.stream()
                .allMatch(op -> FormatValidator.hasValue(op.getAccumulatedPoint()));
        if (allHaveSnapshot) {
            long total = 0L;
            for (OrderProduct orderProduct : orderProducts) {
                total = Math.addExact(total, Math.multiplyExact(orderProduct.getAccumulatedPoint(), orderProduct.getQuantity()));
            }
            return total;
        }

        List<Long> pricePolicyIds = orderProducts.stream()
                .map(OrderProduct::getPricePolicyId)
                .toList();
        Map<Long, ProductInfoResult> productInfoMap = findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds);

        long total = 0L;
        for (OrderProduct orderProduct : orderProducts) {
            ProductInfoResult productInfo = productInfoMap.get(orderProduct.getPricePolicyId());
            if (FormatValidator.hasNoValue(productInfo)) {
                log.warn("상품 정보 조회 누락 - pricePolicyId: {}", orderProduct.getPricePolicyId());
                continue;
            }
            if (FormatValidator.hasNoValue(productInfo.accumulatedPoint())) {
                continue;
            }
            total = Math.addExact(total, Math.multiplyExact(productInfo.accumulatedPoint(), orderProduct.getQuantity()));
        }
        return total;
    }

    private Long calculateProportionalPoint(Long cancelAmount, Long paymentAmount, Long totalPoint) {
        if (FormatValidator.hasNoValue(paymentAmount) || paymentAmount <= 0) {
            return 0L;
        }
        if (FormatValidator.hasNoValue(cancelAmount) || FormatValidator.hasNoValue(totalPoint)) {
            return 0L;
        }
        long numerator = Math.multiplyExact(cancelAmount, totalPoint);
        return (numerator + paymentAmount / 2) / paymentAmount;
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }

        action.run();
    }

    private List<OrderProduct> resolveCancelProducts(boolean isFullCancel, CancelPaymentCommand command) {
        if (isFullCancel || !command.hasCancelProducts()) {
            return null;
        }

        return command.cancelProducts().stream()
                .map(item -> OrderProduct.from(
                        OrderProductSnapshotState.builder()
                                .pricePolicyId(item.pricePolicyId())
                                .quantity(item.quantity())
                                .build()
                ))
                .toList();
    }

    private Long resolveDeductionPoint(
            List<OrderProduct> orderProducts, CancelPaymentCommand command,
            Long paymentAmount, Long cancelAmount
    ) {
        try {
            Long snapshotDeduction = calculateSnapshotDeductionPoint(orderProducts, command);
            if (FormatValidator.hasValue(snapshotDeduction)) {
                return snapshotDeduction;
            }

            return resolveProportionalDeductionPoint(orderProducts, paymentAmount, cancelAmount);
        } catch (Exception e) {
            log.error("부분 취소 포인트 사전 계산 실패 - error: {}", e.getMessage(), e);
            return null;
        }
    }

    private Long calculateSnapshotDeductionPoint(
            List<OrderProduct> orderProducts, CancelPaymentCommand command
    ) {
        if (!command.hasCancelProducts()) {
            return null;
        }

        Map<Long, OrderProduct> orderProductMap = orderProducts.stream()
                .collect(Collectors.toMap(OrderProduct::getPricePolicyId, op -> op, (a, b) -> a));

        long total = 0L;
        for (CancelPaymentCommand.CancelProductItem cancelItem : command.cancelProducts()) {
            OrderProduct orderProduct = orderProductMap.get(cancelItem.pricePolicyId());
            if (FormatValidator.hasNoValue(orderProduct)
                    || FormatValidator.hasNoValue(orderProduct.getAccumulatedPoint())) {
                return null;
            }
            total = Math.addExact(total, Math.multiplyExact(orderProduct.getAccumulatedPoint(), cancelItem.quantity()));
        }

        return total;
    }

    private Long resolveProportionalDeductionPoint(
            List<OrderProduct> orderProducts, Long paymentAmount, Long cancelAmount
    ) {
        Long totalAccumulatedPoint = calculateTotalAccumulatedPoint(orderProducts);
        if (FormatValidator.hasNoValue(totalAccumulatedPoint) || totalAccumulatedPoint <= 0) {
            return null;
        }

        Long proportionalPoint = calculateProportionalPoint(cancelAmount, paymentAmount, totalAccumulatedPoint);
        if (proportionalPoint <= 0) {
            return null;
        }

        return proportionalPoint;
    }

    private void publishPaymentCancelledEvent(Long orderId, String orderKey, Long buyerId,
                                              Long cancelAmount, Long paymentAmount, Long pointAmount,
                                              boolean isFullCancel, Long alreadyRefunded,
                                              String cancelId,
                                              List<OrderProduct> orderProducts,
                                              List<OrderProduct> cancelProducts,
                                              Long partialProductPendingDeduction) {
        try {
            publishPaymentEventPort.publishPaymentCancelledEvent(
                    orderId, orderKey, buyerId, cancelAmount, paymentAmount,
                    pointAmount, isFullCancel, alreadyRefunded, cancelId, orderProducts, cancelProducts,
                    partialProductPendingDeduction);
        } catch (Exception e) {
            log.error("결제 취소 이벤트 발행 실패 - orderId: {}, orderKey: {}, error: {}",
                    orderId, orderKey, e.getMessage(), e);
        }
    }

    private void refundPoints(Order order) {
        Long pointAmount = order.getAmount().getPointAmount();
        if (FormatValidator.hasNoValue(pointAmount) || pointAmount <= 0) {
            return;
        }

        try {
            modifyUserPointPort.refundOrderPoints(order.getBuyerId(), pointAmount, order.getId());
        } catch (Exception e) {
            log.error("포인트 환불 실패 - orderId: {}, buyerId: {}, pointAmount: {}, error: {}",
                    order.getId(), order.getBuyerId(), pointAmount, e.getMessage(), e);
        }
    }
}
