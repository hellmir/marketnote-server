package com.personal.marketnote.commerce.port.in.result.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;

import java.util.List;

public record GetSettlementsResult(
        List<GetSettlementResult> settlements
) {
    public static GetSettlementsResult from(List<Settlement> settlements) {
        List<GetSettlementResult> results = settlements.stream()
                .map(GetSettlementResult::from)
                .toList();
        return new GetSettlementsResult(results);
    }
}
