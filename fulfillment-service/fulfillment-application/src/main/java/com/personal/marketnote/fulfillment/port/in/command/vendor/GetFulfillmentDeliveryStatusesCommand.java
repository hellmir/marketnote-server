package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record GetFulfillmentDeliveryStatusesCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String releaseType
) {
}
