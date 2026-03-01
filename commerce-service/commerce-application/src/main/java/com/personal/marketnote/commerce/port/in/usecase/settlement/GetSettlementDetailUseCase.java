package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementDetailResult;

import java.util.List;

public interface GetSettlementDetailUseCase {
    List<GetSettlementDetailResult> getSettlementAllocations(Long settlementId);
}
