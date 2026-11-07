package com.personal.marketnote.commerce.port.out.payment.vendor;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import lombok.Builder;

@Builder
public record PaymentApprovalVendorCommand(
        String encData,
        String encInfo,
        String orderAmount,
        String orderNumber,
        String payType
) {
    public static PaymentApprovalVendorCommand of(ApprovePaymentCommand command, Payment payment) {
        return new PaymentApprovalVendorCommand(
                command.encData(),
                command.encInfo(),
                String.valueOf(payment.getPaymentAmount().getValue()),
                command.orderKey(),
                command.payType()
        );
    }
}
