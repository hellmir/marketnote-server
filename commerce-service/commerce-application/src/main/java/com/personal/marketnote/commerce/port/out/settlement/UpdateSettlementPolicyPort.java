package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;

public interface UpdateSettlementPolicyPort {
    SettlementPolicy update(SettlementPolicy settlementPolicy);
}
