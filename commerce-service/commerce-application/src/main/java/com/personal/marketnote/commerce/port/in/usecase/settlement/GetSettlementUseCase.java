package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.command.settlement.GetSettlementsQuery;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;

public interface GetSettlementUseCase {
    GetSettlementResult getSettlement(Long id);

    GetSettlementsResult getSettlements(GetSettlementsQuery query);
}
