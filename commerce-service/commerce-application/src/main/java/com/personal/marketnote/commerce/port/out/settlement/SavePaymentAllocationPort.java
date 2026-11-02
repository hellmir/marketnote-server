package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;

import java.util.List;

public interface SavePaymentAllocationPort {
    void saveAll(List<PaymentAllocation> allocations);
}
