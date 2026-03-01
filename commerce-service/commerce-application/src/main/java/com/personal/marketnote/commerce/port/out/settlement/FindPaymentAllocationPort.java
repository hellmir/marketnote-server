package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;

import java.util.List;

public interface FindPaymentAllocationPort {
    List<PaymentAllocation> findUnsettledAllocations(Integer year, Integer month);

    List<PaymentAllocation> findBySettlementId(Long settlementId);
}
