package com.personal.marketnote.commerce.domain.payment;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Payment {
    private Long id;
    private Long orderId;
    private UUID orderKey;
    private String pgPaymentKey;
    private Long paymentAmount;
    private Boolean successYn;
    private Boolean refundedYn;
    private Long refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Payment from(PaymentCreateState state) {
        return Payment.builder()
                .orderId(state.getOrderId())
                .orderKey(state.getOrderKey())
                .paymentAmount(state.getPaymentAmount())
                .successYn(null)
                .refundedYn(false)
                .refundAmount(0L)
                .build();
    }

    public static Payment from(PaymentSnapshotState state) {
        return Payment.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .orderKey(state.getOrderKey())
                .pgPaymentKey(state.getPgPaymentKey())
                .paymentAmount(state.getPaymentAmount())
                .successYn(state.getSuccessYn())
                .refundedYn(state.getRefundedYn())
                .refundAmount(FormatValidator.hasValue(state.getRefundAmount()) ? state.getRefundAmount() : 0L)
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void markAsSuccess(String pgPaymentKey) {
        this.pgPaymentKey = pgPaymentKey;
        successYn = true;
    }

    public void markAsFailed() {
        successYn = false;
    }

    public void markAsRefunded() {
        refundedYn = true;
        refundAmount = paymentAmount;
    }

    public boolean isAlreadyRefunded() {
        return refundedYn;
    }

    public void markAsPartiallyRefunded(Long amount) {
        long newRefundAmount = refundAmount + amount;
        if (newRefundAmount > paymentAmount) {
            throw new InvalidRefundAmountException(
                    "누적 환불 금액이 결제 금액을 초과합니다. paymentAmount=" + paymentAmount
                            + ", 현재 환불액=" + refundAmount + ", 요청 환불액=" + amount);
        }
        refundAmount = newRefundAmount;
        if (FormatValidator.equals(refundAmount, paymentAmount)) {
            refundedYn = true;
        }
    }
}
