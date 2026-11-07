package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record GetFulfillmentDeliveryGoodDetailCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String orderNumber
) {
}
