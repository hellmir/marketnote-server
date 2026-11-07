package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;

public class PollShippingStatusCommandFactory {
    private PollShippingStatusCommandFactory() {
    }

    public static GetFulfillmentDeliveryStatusesCommand toDeliveryStatusesCommand(
            PollShippingStatusCommand command,
            String accessToken,
            String startDate,
            String endDate,
            String releaseType
    ) {
        return GetFulfillmentDeliveryStatusesCommand.builder()
                .customerCode(command.customerCode())
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .releaseType(releaseType)
                .build();
    }
}
