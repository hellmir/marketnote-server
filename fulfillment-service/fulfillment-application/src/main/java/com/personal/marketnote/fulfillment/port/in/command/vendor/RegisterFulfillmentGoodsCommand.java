package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFulfillmentGoodsCommand(
        String customerCode,
        String accessToken,
        List<RegisterFulfillmentGoodsItemCommand> goods
) {
    public static RegisterFulfillmentGoodsCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentGoodsItemCommand> goods
    ) {
        return new RegisterFulfillmentGoodsCommand(customerCode, accessToken, goods);
    }
}
