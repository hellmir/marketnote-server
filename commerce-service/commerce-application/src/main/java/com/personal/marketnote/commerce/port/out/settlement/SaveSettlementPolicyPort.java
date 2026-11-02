package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;

public interface SaveSettlementPolicyPort {
    SettlementPolicy save(SettlementPolicy settlementPolicy);
}
