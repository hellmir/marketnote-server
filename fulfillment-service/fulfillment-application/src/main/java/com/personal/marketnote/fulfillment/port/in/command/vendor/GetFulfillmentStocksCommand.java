package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentStocksCommand(
        String customerCode,
        String accessToken,
        String outOfStockYn,
        String whCd
) {
    public static GetFulfillmentStocksCommand of(
            String customerCode,
            String accessToken
    ) {
        return new GetFulfillmentStocksCommand(customerCode, accessToken, null, null);
    }

    public static GetFulfillmentStocksCommand of(
            String customerCode,
            String accessToken,
            String outOfStockYn
    ) {
        return new GetFulfillmentStocksCommand(customerCode, accessToken, outOfStockYn, null);
    }

    public static GetFulfillmentStocksCommand of(
            String customerCode,
            String accessToken,
            String outOfStockYn,
            String whCd
    ) {
        return new GetFulfillmentStocksCommand(customerCode, accessToken, outOfStockYn, whCd);
    }
}
