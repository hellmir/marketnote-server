package com.personal.marketnote.commerce.adapter.in.web.settlement.mapper;

import com.personal.marketnote.commerce.port.in.command.settlement.GetSellerSettlementsQuery;

public class SellerSettlementRequestToQueryMapper {
    private SellerSettlementRequestToQueryMapper() {
    }

    public static GetSellerSettlementsQuery mapToQuery(Long sellerId, Integer year, Integer month) {
        return GetSellerSettlementsQuery.of(sellerId, year, month);
    }
}
