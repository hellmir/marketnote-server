package com.personal.marketnote.commerce.port.out.settlement;

import java.util.List;

public interface UpdatePaymentAllocationPort {
    void assignSettlement(List<Long> allocationIds, Long settlementId);
}
