package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record UpdateFasstoDeliveryCarCommand(
        String customerCode,
        String accessToken,
        List<UpdateFasstoDeliveryCarItemCommand> deliveryRequests
) {
    public static UpdateFasstoDeliveryCarCommand of(
            String customerCode,
            String accessToken,
            List<UpdateFasstoDeliveryCarItemCommand> deliveryRequests
    ) {
        return new UpdateFasstoDeliveryCarCommand(customerCode, accessToken, deliveryRequests);
    }
}
