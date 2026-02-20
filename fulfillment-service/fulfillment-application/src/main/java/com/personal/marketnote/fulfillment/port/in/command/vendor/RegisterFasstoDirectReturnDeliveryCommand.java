package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFasstoDirectReturnDeliveryCommand(
        String customerCode,
        String accessToken,
        List<RegisterFasstoDirectReturnDeliveryItemCommand> directReturnDeliveryRequests
) {
    public static RegisterFasstoDirectReturnDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFasstoDirectReturnDeliveryItemCommand> directReturnDeliveryRequests
    ) {
        return new RegisterFasstoDirectReturnDeliveryCommand(customerCode, accessToken, directReturnDeliveryRequests);
    }
}
