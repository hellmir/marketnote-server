package com.personal.marketnote.commerce.adapter.in.web.settlement.response;

import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;

import java.util.List;

public record GetSellerSettlementsResponse(
        List<GetSettlementResponse> settlements
) {
    public static GetSellerSettlementsResponse from(GetSettlementsResult result) {
        List<GetSettlementResponse> responses = result.settlements().stream()
                .map(GetSettlementResponse::from)
                .toList();
        return new GetSellerSettlementsResponse(responses);
    }
}
