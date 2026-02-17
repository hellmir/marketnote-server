package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record UpdateFasstoDeliveryCommand(
        String customerCode,
        String accessToken,
        List<UpdateFasstoDeliveryItemCommand> deliveryRequests
) {
    public static UpdateFasstoDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<UpdateFasstoDeliveryItemCommand> deliveryRequests
    ) {
        return new UpdateFasstoDeliveryCommand(customerCode, accessToken, deliveryRequests);
    }
}
