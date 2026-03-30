package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentAllStockCommand;

public class FulfillmentStockRequestToCommandMapper {
    public static GetFulfillmentStocksCommand mapToStocksCommand(
            String customerCode,
            String accessToken,
            String outOfStockYn,
            String whCd
    ) {
        return GetFulfillmentStocksCommand.of(customerCode, accessToken, outOfStockYn, whCd);
    }

    public static GetFulfillmentStockDetailCommand mapToStockDetailCommand(
            String customerCode,
            String accessToken,
            String cstGodCd,
            String outOfStockYn
    ) {
        return GetFulfillmentStockDetailCommand.of(customerCode, accessToken, cstGodCd, outOfStockYn);
    }

    public static SyncFulfillmentAllStockCommand mapToSyncAllCommand(
            String customerCode,
            String whCd
    ) {
        return SyncFulfillmentAllStockCommand.of(customerCode, whCd);
    }
}
