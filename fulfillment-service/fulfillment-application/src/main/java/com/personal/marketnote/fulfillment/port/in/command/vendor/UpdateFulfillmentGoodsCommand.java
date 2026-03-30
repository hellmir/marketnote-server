package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record UpdateFulfillmentGoodsCommand(
        String customerCode,
        String accessToken,
        List<UpdateFulfillmentGoodsItemCommand> goods
) {
    public static UpdateFulfillmentGoodsCommand of(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentGoodsItemCommand> goods
    ) {
        return new UpdateFulfillmentGoodsCommand(customerCode, accessToken, goods);
    }
}
