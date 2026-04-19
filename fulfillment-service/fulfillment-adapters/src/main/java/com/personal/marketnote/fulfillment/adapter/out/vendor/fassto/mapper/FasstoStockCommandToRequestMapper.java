package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.stock.FulfillmentStockDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.stock.FulfillmentStockQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;

public class FasstoStockCommandToRequestMapper {
    public static FulfillmentStockQuery mapToQuery(GetFulfillmentStocksCommand command) {
        return FulfillmentStockQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.outOfStockYn(),
                command.warehouseCode()
        );
    }

    public static FulfillmentStockDetailQuery mapToDetailQuery(GetFulfillmentStockDetailCommand command) {
        return FulfillmentStockDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.customerProductCode(),
                command.outOfStockYn()
        );
    }
}
