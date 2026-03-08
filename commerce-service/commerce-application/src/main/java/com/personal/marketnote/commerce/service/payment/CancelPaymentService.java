package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundCreateState;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.CancelPaymentUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;
    private final RestoreProductInventoryUseCase restoreProductInventoryUseCase;
    private final ModifyUserPointPort modifyUserPointPort;
    private final SaveRefundPort saveRefundPort;

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
        Long refundableAmount = payment.getPaymentAmount() - alreadyRefunded;

        Long cancelAmount = computeCancelAmount(isFullCancel, command.cancelAmount(), refundableAmount);
        Long remainAmount = refundableAmount - cancelAmount;

        PaymentCancelVendorResult vendorResult = requestPaymentCancellationToPsp(
                transactionNumber, modType, cancelAmount, remainAmount, command.cancelReason()
        );

        if (!vendorResult.isSuccess()) {
            throw new PaymentCancelException(
                    "KCP 결제 취소 실패 [" + vendorResult.resCd() + "]: " + vendorResult.resMsg()
            );
        }

        updateDomain(isFullCancel, payment, event, vendorResult, cancelAmount);
        updatePaymentPort.update(payment);
        updatePspPaymentEventPort.update(event);

        saveRefundRecord(payment, isFullCancel, cancelAmount, command.cancelReason(), vendorResult);

        if (isFullCancel) {
            changeOrderStatusUseCase.changeOrderStatus(
                    ChangeOrderStatusCommand.builder()
                            .id(payment.getOrderId())
                            .orderStatus(OrderStatus.CANCEL_REQUESTED)
                            .build()
            );
            restoreInventory(order);
            refundPoints(order);
            revokePendingProductAccumulationPoints(order);
            revokePendingSharedPurchasePoints(order);
        }

        recordLedgerEntryForCancellation(payment, isFullCancel, cancelAmount, alreadyRefunded);
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
            String transactionNumber, String modType, Long cancelAmount, Long remainAmount, String cancelReason
    ) {
        PaymentCancelVendorCommand vendorCommand = PaymentCancelVendorCommand.builder()
                .tno(transactionNumber)
                .modType(modType)
                .modMny(cancelAmount)
                .remMny(remainAmount)
                .modDesc(cancelReason)
                .build();

        return paymentVendorPort.cancelPayment(vendorCommand);
    }

    private void updateDomain(
            boolean isFullCancel, Payment payment, PspPaymentEvent event,
            PaymentCancelVendorResult vendorResult, Long cancelAmount
    ) {
        if (isFullCancel) {
            payment.markAsRefunded();
            event.cancel(vendorResult.rawResponse());
            return;
        }

        payment.markAsPartiallyRefunded(cancelAmount);
        event.partialRefund(vendorResult.rawResponse());
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

    private void recordLedgerEntryForCancellation(
            Payment payment, boolean isFullCancel, Long cancelAmount, Long alreadyRefunded
    ) {
        try {
            String idempotencyKey;
            if (isFullCancel) {
                idempotencyKey = "PAYMENT_CANCELLATION:" + payment.getOrderId();
            } else {
                idempotencyKey = "PAYMENT_PARTIAL_REFUND:" + payment.getOrderId()
                        + ":" + cancelAmount + ":" + alreadyRefunded;
            }
            recordLedgerEntryUseCase.recordPaymentCancellation(
                    payment.getOrderId(), cancelAmount, idempotencyKey
            );
        } catch (Exception e) {
            log.error("결제 취소 역분개 기록 실패 - orderId: {}, error: {}",
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
                    order.getOrderProducts(), "주문 전액 취소에 의한 재고 복구"
            );
        } catch (Exception e) {
            log.error("재고 복구 실패 - orderId: {}, error: {}",
                    order.getId(), e.getMessage(), e);
        }
    }

    private void revokePendingProductAccumulationPoints(Order order) {
        try {
            modifyUserPointPort.revokePendingPoints(order.getBuyerId(), order.getId());
        } catch (Exception e) {
            log.error("상품 적립 예정 포인트 회수 실패 - orderId: {}, buyerId: {}, error: {}",
                    order.getId(), order.getBuyerId(), e.getMessage(), e);
        }
    }

    private void revokePendingSharedPurchasePoints(Order order) {
        List<Long> sharerIds = extractSharerIds(order);
        if (sharerIds.isEmpty()) {
            return;
        }

        try {
            modifyUserPointPort.revokePendingSharedPurchasePoints(sharerIds, order.getId());
        } catch (Exception e) {
            log.error("공유 적립 예정 포인트 회수 실패 - orderId: {}, sharerIds: {}, error: {}",
                    order.getId(), sharerIds, e.getMessage(), e);
        }
    }

    private List<Long> extractSharerIds(Order order) {
        return order.getOrderProducts().stream()
                .map(OrderProduct::getSharerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void refundPoints(Order order) {
        Long pointAmount = order.getPointAmount();
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
