package com.personal.marketnote.commerce.port.out.payment.vendor;

import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import lombok.Builder;

@Builder
public record PaymentCancelVendorCommand(
        String transactionId,
        String cancelType,
        Long cancelAmount,
        Long remainAmount,
        String cancelReason
) {
    public static PaymentCancelVendorCommand from(PspPaymentEvent event, CancelDirective directive) {
        return PaymentCancelVendorCommand.builder()
                .transactionId(event.getPgPaymentKey())
                .cancelType(directive.cancelType())
                .cancelAmount(directive.cancelAmount())
                .remainAmount(directive.remainAmount())
                .cancelReason(directive.cancelReason())
                .build();
    }

    public record CancelDirective(
            String cancelType,
            Long cancelAmount,
            Long remainAmount,
            String cancelReason
    ) {
        public static CancelDirective of(
                String cancelType,
                Long cancelAmount,
                Long remainAmount,
                String cancelReason
        ) {
            return new CancelDirective(cancelType, cancelAmount, remainAmount, cancelReason);
        }
    }
}
