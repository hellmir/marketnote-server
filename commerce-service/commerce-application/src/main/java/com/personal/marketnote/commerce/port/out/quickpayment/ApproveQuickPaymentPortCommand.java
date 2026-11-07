package com.personal.marketnote.commerce.port.out.quickpayment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.port.in.command.quickpayment.ApproveQuickPaymentCommand;
import lombok.Builder;

@Builder
public record ApproveQuickPaymentPortCommand(
        String batchKey,
        String groupId,
        String orderKey,
        String amount,
        String goodName
) {
    public static ApproveQuickPaymentPortCommand of(
            ApproveQuickPaymentCommand command, QuickPaymentCard card, Payment payment
    ) {
        return new ApproveQuickPaymentPortCommand(
                card.getBatchKey(),
                card.getGroupId(),
                command.orderKey(),
                String.valueOf(payment.getPaymentAmount().getValue()),
                command.goodName()
        );
    }
}
