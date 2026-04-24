package com.personal.marketnote.commerce.port.in.usecase.payment;

import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;

public interface RefundPaymentUseCase {
    void refund(RefundPaymentCommand command);
}
