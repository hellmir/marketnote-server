package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record GetFulfillmentDeliveriesCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String status,
        String releaseType,
        String orderNumber
) {
}
