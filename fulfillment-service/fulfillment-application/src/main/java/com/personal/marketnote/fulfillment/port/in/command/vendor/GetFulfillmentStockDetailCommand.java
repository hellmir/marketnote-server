package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentStockDetailCommand(
        String customerCode,
        String accessToken,
        String cstGodCd,
        String outOfStockYn
) {
    public static GetFulfillmentStockDetailCommand of(
            String customerCode,
            String accessToken,
            String cstGodCd,
            String outOfStockYn
    ) {
        return new GetFulfillmentStockDetailCommand(customerCode, accessToken, cstGodCd, outOfStockYn);
    }
}
