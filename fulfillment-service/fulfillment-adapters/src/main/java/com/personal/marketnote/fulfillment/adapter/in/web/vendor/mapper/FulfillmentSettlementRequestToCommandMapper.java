package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;

public class FulfillmentSettlementRequestToCommandMapper {
    public static GetFulfillmentSettlementDailyCostsCommand mapToDailyCostsCommand(
            String yearMonth,
            String whCd,
            String customerCode,
            String accessToken
    ) {
        return GetFulfillmentSettlementDailyCostsCommand.of(yearMonth, whCd, customerCode, accessToken);
    }
}
