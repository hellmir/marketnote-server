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
        this.successYn = true;
    }

    public void markAsFailed() {
        this.successYn = false;
    }

    public void markAsRefunded() {
        this.refundedYn = true;
        this.refundAmount = this.paymentAmount;
    }

    public void markAsPartiallyRefunded(Long amount) {
        this.refundAmount = this.refundAmount + amount;
        if (this.refundAmount.equals(this.paymentAmount)) {
            this.refundedYn = true;
        }
    }
}
