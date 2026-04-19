package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.settlement.FulfillmentSettlementDailyCostQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;

public class FasstoSettlementCommandToRequestMapper {
    public static FulfillmentSettlementDailyCostQuery mapToQuery(GetFulfillmentSettlementDailyCostsCommand command) {
        return FulfillmentSettlementDailyCostQuery.of(
                command.yearMonth(),
                command.warehouseCode(),
                command.customerCode(),
                command.accessToken()
        );
    }
}
