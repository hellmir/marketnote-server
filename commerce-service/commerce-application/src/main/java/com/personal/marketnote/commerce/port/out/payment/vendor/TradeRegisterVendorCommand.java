package com.personal.marketnote.commerce.port.out.payment.vendor;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import lombok.Builder;

@Builder
public record TradeRegisterVendorCommand(
        String orderKey,
        String orderAmount,
        String payMethod,
        String goodName
) {
    public static TradeRegisterVendorCommand of(Payment payment, ReadyPaymentCommand command) {
        return new TradeRegisterVendorCommand(
                payment.getOrderKey().toString(),
                String.valueOf(payment.getPaymentAmount().getValue()),
                command.payMethod(),
                command.goodName()
        );
    }
}
