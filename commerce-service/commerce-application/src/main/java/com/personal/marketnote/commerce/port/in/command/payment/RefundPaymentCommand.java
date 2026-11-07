package com.personal.marketnote.commerce.port.in.command.payment;

import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;
import lombok.Builder;

@Builder
public record RefundPaymentCommand(
        String orderKey,
        Long orderId,
        Long cancelAmount,
        Long paymentAmount,
        boolean isFullCancel,
        Long alreadyRefunded,
        Long returnShippingFee
) {
    public static RefundPaymentCommand from(CompleteReturnRefundCommand command) {
        return RefundPaymentCommand.builder()
                .orderKey(command.orderKey())
                .orderId(command.orderId())
                .cancelAmount(command.returnAmount())
                .paymentAmount(command.paymentAmount())
                .isFullCancel(command.isFullReturn())
                .returnShippingFee(command.returnShippingFee())
                .build();
    }
}
