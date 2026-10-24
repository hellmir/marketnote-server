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
import com.personal.marketnote.commerce.exception.InvalidCancelAmountException;
import com.personal.marketnote.commerce.exception.InvalidCancelProductException;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishPaymentEventPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePaymentPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * 결제 취소 트랜잭션 헬퍼
 *
 * <p>결제 취소 플로우를 독립 트랜잭션(REQUIRES_NEW) 3단계로 분리하여
 * PG 취소 성공 후 DB 업데이트 실패 시에도 수동 복구 근거(PSP Event)를 확보한다.
 *
 * <ol>
 *   <li>TX-1 {@link #prepareCancel}: 검증만 수행, DB 쓰기 없이 Context 반환 (readOnly)</li>
 *   <li>TX-2A {@link #commitPspEventCancel}: PSP Event만 cancel/partialRefund 상태로 커밋
 *       (수동 복구 근거 확보 — payment/refund 변경 없음)</li>
 *   <li>TX-2B {@link #commitPaymentAndPostProcess}: payment markAsRefunded/markAsPartiallyRefunded +
 *       refund 저장 + 재고 복구 + 포인트 환불 + Outbox 이벤트 + afterCommit 훅</li>
 *   <li>TX-2 {@link #commitCancelSkipPg}: 배송비 차감으로 환불 금액이 0원인 경로 통합 처리</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelTransactionHelper {
    private static final String PSP_FULL_CANCEL_TYPE_CODE = "STSC";
    private static final String PSP_PARTIAL_CANCEL_TYPE_CODE = "STPC";

    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final RestoreProductInventoryUseCase restoreProductInventoryUseCase;
    private final ModifyUserPointPort modifyUserPointPort;
    private final SaveRefundPort saveRefundPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final PublishPaymentEventPort publishPaymentEventPort;
    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;

    /**
     * TX-1: 조회/검증 + Context 반환 (DB 쓰기 없음)
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED, readOnly = true)
    public PaymentCancelContext prepareCancel(CancelPaymentCommand command) {
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

        Long alreadyRefunded = payment.getRefundAmount().getValue();
        Long refundableAmount = Math.subtractExact(payment.getPaymentAmount().getValue(), alreadyRefunded);

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
        String cancelId = isFullCancel ? null : UUID.randomUUID().toString();

        return PaymentCancelContext.of(
                payment, event, order, cancelAmount, adjustedCancelAmount, remainAmount,
                alreadyRefunded, shippingFeeDeduction, isFullCancel,
                command.cancelReason(), adjustedCancelReason, cancelId,
                transactionNumber, modType, command
        );
    }

    /**
     * TX-2A: PSP Event만 cancel/partialRefund 상태로 커밋 (수동 복구 근거 확보)
     *
     * <p>TX-2B 실패 시에도 PSP Event에는 PG 취소 완료 상태/rawResponse가 남아
     * 시스템 운영자가 불일치를 식별하고 수동으로 payment/refund 보정을 수행할 수 있다.</p>
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitPspEventCancel(PaymentCancelContext context, PaymentCancelVendorResult vendorResult) {
        PspPaymentEvent event = context.event();
        String rawResponse = FormatValidator.hasValue(vendorResult) ? vendorResult.rawResponse() : null;

        if (context.isFullCancel()) {
            event.cancel(rawResponse);
            updatePspPaymentEventPort.update(event);
            return;
        }

        event.partialRefund(rawResponse);
        updatePspPaymentEventPort.update(event);
    }

    /**
     * TX-2B: payment 환불 처리 + refund 저장 + 재고 복구 + 포인트 환불 + Outbox 이벤트 + afterCommit 훅
     *
     * <p>동시 부분취소 race 방지: TX-1 스냅샷이 아니라 TX-2B 진입 시점의 최신 Payment를 재조회하여
     * refundAmount 누적. @Version 충돌 시 {@link Retryable}로 최대 3회 재시도.</p>
     */
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50)
    )
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitPaymentAndPostProcess(PaymentCancelContext context, PaymentCancelVendorResult vendorResult) {
        Payment payment = findPaymentPort.findByOrderKey(context.payment().getOrderKey())
                .orElseThrow(() -> new PaymentNotFoundException(context.originalCommand().orderKey()));
        Order order = context.order();
        CancelPaymentCommand command = context.originalCommand();
        boolean isFullCancel = context.isFullCancel();

        if (isFullCancel) {
            payment.markAsRefunded();
        }
        if (!isFullCancel) {
            payment.markAsPartiallyRefunded(Money.of(context.adjustedCancelAmount()));
        }
        updatePaymentPort.update(payment);

        saveRefundRecord(payment, isFullCancel, context.adjustedCancelAmount(), context.adjustedCancelReason(), vendorResult);

        Long partialProductPendingDeduction = null;
        if (isFullCancel) {
            restoreInventory(order);
            refundPoints(order);
        }
        if (!isFullCancel) {
            restorePartialCancelInventory(order, command);
            partialProductPendingDeduction = resolveDeductionPoint(
                    order.getOrderProducts(), command, payment.getPaymentAmount().getValue(), context.cancelAmount());
            Long buyerId = order.getBuyerId();
            Long orderId = order.getId();
            Long precomputedDeduction = partialProductPendingDeduction;
            runAfterCommit(() -> reducePartialPendingProductAccumulationPoints(buyerId, orderId, precomputedDeduction));
            runAfterCommit(() -> reducePartialPendingSharedPurchasePoints(
                    order, payment.getPaymentAmount().getValue(), context.cancelAmount()));
        }

        List<OrderProduct> cancelTargetProducts = resolveCancelProducts(isFullCancel, command);

        // [#929][#1034] 결제 취소 역분개는 Kafka Consumer(PaymentCancelledLedgerConsumer)로 전환 완료
        publishPaymentCancelledEvent(
                order.getId(), command.orderKey(), order.getBuyerId(), context.cancelAmount(),
                payment.getPaymentAmount().getValue(), order.getAmount().getPointAmount().getValue(),
                isFullCancel, context.alreadyRefunded(),
                context.cancelId(), order.getOrderProducts(), cancelTargetProducts,
                partialProductPendingDeduction
        );
    }

    /**
     * TX-2: 배송비 차감으로 환불 금액이 0원인 경로 통합 처리
     *
     * <p>PG 호출을 생략하는 경로이므로 PSP Event/payment를 함께 동일 트랜잭션에서 갱신해도
     * 외부 상태와의 불일치 위험이 없다.</p>
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitCancelSkipPg(PaymentCancelContext context) {
        Payment payment = context.payment();
        PspPaymentEvent event = context.event();
        Order order = context.order();
        CancelPaymentCommand command = context.originalCommand();

        log.info("부분 취소 환불 금액이 배송비 차감으로 0원 - orderId: {}, PG 환불 생략", order.getId());

        payment.markAsPartiallyRefunded(Money.of(0L));
        event.partialRefund(null);
        updatePaymentPort.update(payment);
        updatePspPaymentEventPort.update(event);

        saveRefundRecordWithoutPg(payment, context.cancelAmount(), context.shippingFeeDeduction(), context.adjustedCancelReason());
        restorePartialCancelInventory(order, command);

        Long pgSkipDeduction = resolveDeductionPoint(
                order.getOrderProducts(), command, payment.getPaymentAmount().getValue(), context.cancelAmount());
        Long pgSkipBuyerId = order.getBuyerId();
        Long pgSkipOrderId = order.getId();
        runAfterCommit(() -> reducePartialPendingProductAccumulationPoints(
                pgSkipBuyerId, pgSkipOrderId, pgSkipDeduction));
        runAfterCommit(() -> reducePartialPendingSharedPurchasePoints(
                order, payment.getPaymentAmount().getValue(), context.cancelAmount()));

        List<OrderProduct> pgSkipCancelProducts = resolveCancelProducts(context.isFullCancel(), command);
        publishPaymentCancelledEvent(
                order.getId(), command.orderKey(), order.getBuyerId(), 0L,
                payment.getPaymentAmount().getValue(), order.getAmount().getPointAmount().getValue(),
                context.isFullCancel(), context.alreadyRefunded(),
                UUID.randomUUID().toString(), order.getOrderProducts(), pgSkipCancelProducts, pgSkipDeduction
        );
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
                            allocation -> allocation.getShippingFee().getValue(),
                            (existing, replacement) -> existing
                    ));

            Map<Long, Long> sellerTotalAmounts = order.getOrderProducts().stream()
                    .collect(Collectors.groupingBy(
                            OrderProduct::getSellerId,
                            Collectors.summingLong(
                                    op -> op.getUnitAmount().multiply(op.getQuantity().getValue()).getValue()
                            )
                    ));

            Map<Long, Long> sellerCancelAmounts = command.cancelProducts().stream()
                    .collect(Collectors.groupingBy(
                            item -> resolveSellerIdForCancelProduct(order, item.pricePolicyId()),
                            Collectors.summingLong(item -> {
                                OrderProduct orderProduct = findOrderProductByPricePolicyId(
                                        order, item.pricePolicyId()
                                );
                                return orderProduct.getUnitAmount().multiply(item.quantity().longValue()).getValue();
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
                        Money.of(remainingAmount), Money.of(policy.shippingFee()),
                        Money.of(policy.freeShippingThreshold())
                );
                long baseFee = ShippingFeeCalculator.calculateBaseFee(context).getValue();
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
        return findOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
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
                                    .unitAmount(0L)
                                    .accumulatedPoint(0L)
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
                .collect(Collectors.toMap(
                        OrderProduct::getPricePolicyId, op -> op.getQuantity().getValue(), Integer::sum
                ));

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
                total = Math.addExact(total,
                        orderProduct.getAccumulatedPoint().multiply(orderProduct.getQuantity().getValue()).getValue()
                );
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
            total = Math.addExact(total,
                    Math.multiplyExact(productInfo.accumulatedPoint(), (long) orderProduct.getQuantity().getValue())
            );
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
                                .unitAmount(0L)
                                .accumulatedPoint(0L)
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

    private Long calculateSnapshotDeductionPoint(List<OrderProduct> orderProducts, CancelPaymentCommand command) {
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
            total = Math.addExact(total, orderProduct.getAccumulatedPoint().multiply(cancelItem.quantity()).getValue());
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

    private void publishPaymentCancelledEvent(
            Long orderId, String orderKey, Long buyerId,
            Long cancelAmount, Long paymentAmount, Long pointAmount,
            boolean isFullCancel, Long alreadyRefunded,
            String cancelId,
            List<OrderProduct> orderProducts,
            List<OrderProduct> cancelProducts,
            Long partialProductPendingDeduction
    ) {
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
        Long pointAmount = order.getAmount().getPointAmount().getValue();
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
