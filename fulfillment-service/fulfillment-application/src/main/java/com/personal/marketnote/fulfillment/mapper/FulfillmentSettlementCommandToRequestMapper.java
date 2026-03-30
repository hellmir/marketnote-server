package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.settlement.FulfillmentSettlementDailyCostQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;

public class FulfillmentSettlementCommandToRequestMapper {
    public static FulfillmentSettlementDailyCostQuery mapToQuery(GetFulfillmentSettlementDailyCostsCommand command) {
        return FulfillmentSettlementDailyCostQuery.of(
                command.yearMonth(),
                command.whCd(),
                command.customerCode(),
                command.accessToken()
        );
    }
}
