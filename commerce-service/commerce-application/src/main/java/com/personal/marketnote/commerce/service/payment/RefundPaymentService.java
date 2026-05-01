package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundCreateState;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.RefundPaymentUseCase;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class RefundPaymentService implements RefundPaymentUseCase {
    private static final String PSP_FULL_CANCEL_TYPE_CODE = "STSC";
    private static final String PSP_PARTIAL_CANCEL_TYPE_CODE = "STPC";

    private final FindPaymentPort findPaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final PaymentVendorPort paymentVendorPort;
    private final SaveRefundPort saveRefundPort;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void refund(RefundPaymentCommand command) {
        Payment payment = findPayment(command.orderKey());
        validateNotAlreadyRefunded(payment, command.orderKey());

        PspPaymentEvent event = findPspPaymentEvent(command.orderKey());
        validateRefundable(event, command.orderKey());

        Long alreadyRefunded = FormatValidator.hasValue(command.alreadyRefunded()) ? command.alreadyRefunded() : 0L;
        Long refundableAmount = command.paymentAmount() - alreadyRefunded;
        Long cancelAmount = resolveCancelAmount(command.isFullCancel(), command.cancelAmount(), refundableAmount);

        Long adjustedCancelAmount = cancelAmount;
        if (!command.isFullCancel()) {
            adjustedCancelAmount = deductReturnShippingFee(cancelAmount, command.returnShippingFee(), command.orderId());
        }

        if (adjustedCancelAmount <= 0L) {
            log.info("반품 배송비 차감으로 환불 금액 0원 - orderId: {}, PG 환불 생략", command.orderId());
            persistRefundResultWithoutPg(command, payment, event);
            return;
        }

        Long remainAmount = refundableAmount - adjustedCancelAmount;
        String cancelType = command.isFullCancel() ? PSP_FULL_CANCEL_TYPE_CODE : PSP_PARTIAL_CANCEL_TYPE_CODE;

        PaymentCancelVendorResult vendorResult = requestPgCancellation(
                event.getPgPaymentKey(), cancelType, adjustedCancelAmount, remainAmount
        );

        persistRefundResult(command, payment, event, vendorResult, adjustedCancelAmount);
    }

    private Payment findPayment(String orderKey) {
        UUID orderKeyUuid = UUID.fromString(orderKey);
        return findPaymentPort.findByOrderKey(orderKeyUuid)
                .orElseThrow(() -> new PaymentNotFoundException(orderKey));
    }

    private void validateNotAlreadyRefunded(Payment payment, String orderKey) {
        if (payment.isAlreadyRefunded()) {
            throw new PaymentAlreadyRefundedException(orderKey);
        }
    }

    private PspPaymentEvent findPspPaymentEvent(String orderKey) {
        return findPspPaymentEventPort.findByOrderKey(orderKey)
                .orElseThrow(() -> new PaymentNotFoundException(orderKey));
    }

    private void validateRefundable(PspPaymentEvent event, String orderKey) {
        if (!event.getPoStatus().isRefundable()) {
            throw new PaymentAlreadyRefundedException(orderKey);
        }
    }

    private Long resolveCancelAmount(boolean isFullCancel, Long cancelAmount, Long refundableAmount) {
        if (isFullCancel) {
            return refundableAmount;
        }
        return cancelAmount;
    }

    private Long deductReturnShippingFee(Long cancelAmount, Long returnShippingFee, Long orderId) {
        if (FormatValidator.hasNoValue(returnShippingFee) || returnShippingFee <= 0L) {
            return cancelAmount;
        }

        Long adjusted = Math.subtractExact(cancelAmount, returnShippingFee);
        if (adjusted <= 0L) {
            adjusted = 0L;
        }

        log.info("반품 배송비 차감 - orderId: {}, 원래 환불액: {}, 반품 배송비: {}, 조정 환불액: {}",
                orderId, cancelAmount, returnShippingFee, adjusted);
        return adjusted;
    }

    private void persistRefundResultWithoutPg(
            RefundPaymentCommand command, Payment payment, PspPaymentEvent event
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> {
            updateDomain(command.isFullCancel(), payment, event, null, 0L);
            updatePaymentPort.update(payment);
            updatePspPaymentEventPort.update(event);
            saveRefundRecordWithoutPg(payment, command.cancelAmount(), command.returnShippingFee());
        });
    }

    private void saveRefundRecordWithoutPg(Payment payment, Long cancelAmount, Long returnShippingFee) {
        try {
            String cancelReason = "반품 환불 (반품 배송비 " + returnShippingFee + "원 차감, PG 환불 생략)";
            RefundCreateState createState = RefundCreateState.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .refundType(RefundType.PARTIAL_REFUND)
                    .refundAmount(cancelAmount)
                    .cancelReason(cancelReason)
                    .processedBy("SYSTEM")
                    .build();
            Refund refund = Refund.from(createState);
            saveRefundPort.save(refund);
        } catch (Exception e) {
            log.error("반품 배송비 차감 환불 기록 저장 실패 - orderId: {}, error: {}",
                    payment.getOrderId(), e.getMessage(), e);
        }
    }

    private PaymentCancelVendorResult requestPgCancellation(
            String transactionNumber, String cancelType, Long cancelAmount, Long remainAmount
    ) {
        PaymentCancelVendorCommand vendorCommand = PaymentCancelVendorCommand.builder()
                .transactionId(transactionNumber)
                .cancelType(cancelType)
                .cancelAmount(cancelAmount)
                .remainAmount(remainAmount)
                .cancelReason("주문 취소")
                .build();

        PaymentCancelVendorResult vendorResult = paymentVendorPort.cancelPayment(vendorCommand);

        if (!vendorResult.success()) {
            throw new PaymentCancelException(
                    "결제 취소 실패 [" + vendorResult.resultCode() + "]: " + vendorResult.resultMessage()
            );
        }

        return vendorResult;
    }

    private void persistRefundResult(
            RefundPaymentCommand command, Payment payment, PspPaymentEvent event,
            PaymentCancelVendorResult vendorResult, Long cancelAmount
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> {
            updateDomain(command.isFullCancel(), payment, event, vendorResult, cancelAmount);
            updatePaymentPort.update(payment);
            updatePspPaymentEventPort.update(event);
            saveRefundRecord(payment, command.isFullCancel(), cancelAmount, vendorResult);
        });
    }

    private void updateDomain(
            boolean isFullCancel, Payment payment, PspPaymentEvent event,
            PaymentCancelVendorResult vendorResult, Long cancelAmount
    ) {
        String rawResponse = FormatValidator.hasValue(vendorResult) ? vendorResult.rawResponse() : null;

        if (isFullCancel) {
            payment.markAsRefunded();
            event.cancel(rawResponse);
            return;
        }

        payment.markAsPartiallyRefunded(cancelAmount);
        event.partialRefund(rawResponse);
    }

    private void saveRefundRecord(
            Payment payment, boolean isFullCancel, Long cancelAmount,
            PaymentCancelVendorResult vendorResult
    ) {
        try {
            RefundType refundType = isFullCancel ? RefundType.FULL_REFUND : RefundType.PARTIAL_REFUND;
            RefundCreateState createState = RefundCreateState.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .refundType(refundType)
                    .refundAmount(cancelAmount)
                    .cancelReason("주문 취소")
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
}
