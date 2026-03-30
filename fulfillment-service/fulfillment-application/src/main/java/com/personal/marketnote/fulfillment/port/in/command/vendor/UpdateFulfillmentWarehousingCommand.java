package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record UpdateFulfillmentWarehousingCommand(
        String customerCode,
        String accessToken,
        List<UpdateFulfillmentWarehousingItemCommand> warehousingRequests
) {
    public static UpdateFulfillmentWarehousingCommand of(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentWarehousingItemCommand> warehousingRequests
    ) {
        return new UpdateFulfillmentWarehousingCommand(customerCode, accessToken, warehousingRequests);
    }
}
