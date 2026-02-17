package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFasstoDeliveryCarCommand(
        String customerCode,
        String accessToken,
        List<RegisterFasstoDeliveryCarItemCommand> deliveryRequests
) {
    public static RegisterFasstoDeliveryCarCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFasstoDeliveryCarItemCommand> deliveryRequests
    ) {
        return new RegisterFasstoDeliveryCarCommand(customerCode, accessToken, deliveryRequests);
    }
}
