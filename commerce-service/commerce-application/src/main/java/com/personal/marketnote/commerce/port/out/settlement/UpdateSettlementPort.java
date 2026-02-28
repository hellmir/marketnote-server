package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;

public interface UpdateSettlementPort {
    Settlement update(Settlement settlement);
}
