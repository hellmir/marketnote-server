package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record CompleteFasstoDeliveryIcsCommand(
        String customerCode,
        String accessToken,
        List<CompleteFasstoDeliveryIcsItemCommand> completionRequests
) {
    public static CompleteFasstoDeliveryIcsCommand of(
            String customerCode,
            String accessToken,
            List<CompleteFasstoDeliveryIcsItemCommand> completionRequests
    ) {
        return new CompleteFasstoDeliveryIcsCommand(customerCode, accessToken, completionRequests);
    }
}
