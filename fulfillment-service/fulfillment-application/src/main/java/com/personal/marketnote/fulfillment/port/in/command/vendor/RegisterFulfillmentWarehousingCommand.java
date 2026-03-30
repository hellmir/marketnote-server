package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFulfillmentWarehousingCommand(
        String customerCode,
        String accessToken,
        List<RegisterFulfillmentWarehousingItemCommand> warehousingRequests
) {
    public static RegisterFulfillmentWarehousingCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentWarehousingItemCommand> warehousingRequests
    ) {
        return new RegisterFulfillmentWarehousingCommand(customerCode, accessToken, warehousingRequests);
    }
}
