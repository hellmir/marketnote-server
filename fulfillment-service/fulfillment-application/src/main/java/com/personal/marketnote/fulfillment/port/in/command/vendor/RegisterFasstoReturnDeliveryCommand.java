package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFasstoReturnDeliveryCommand(
        String customerCode,
        String accessToken,
        List<RegisterFasstoReturnDeliveryItemCommand> returnDeliveryRequests
) {
    public static RegisterFasstoReturnDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFasstoReturnDeliveryItemCommand> returnDeliveryRequests
    ) {
        return new RegisterFasstoReturnDeliveryCommand(customerCode, accessToken, returnDeliveryRequests);
    }
}
