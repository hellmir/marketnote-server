package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.stock.FulfillmentStockDetailQuery;
import com.personal.marketnote.fulfillment.domain.vendor.stock.FulfillmentStockQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;

public class FulfillmentStockCommandToRequestMapper {
    public static FulfillmentStockQuery mapToQuery(GetFulfillmentStocksCommand command) {
        return FulfillmentStockQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.outOfStockYn(),
                command.whCd()
        );
    }

    public static FulfillmentStockDetailQuery mapToDetailQuery(GetFulfillmentStockDetailCommand command) {
        return FulfillmentStockDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.cstGodCd(),
                command.outOfStockYn()
        );
    }
}
