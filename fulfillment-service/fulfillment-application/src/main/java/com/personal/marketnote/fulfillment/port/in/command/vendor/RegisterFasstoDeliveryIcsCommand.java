package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFasstoDeliveryIcsCommand(
        String customerCode,
        String accessToken,
        List<RegisterFasstoDeliveryIcsItemCommand> deliveryRequests
) {
    public static RegisterFasstoDeliveryIcsCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFasstoDeliveryIcsItemCommand> deliveryRequests
    ) {
        return new RegisterFasstoDeliveryIcsCommand(customerCode, accessToken, deliveryRequests);
    }
}
