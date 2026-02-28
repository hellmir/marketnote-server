package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.PaymentAllocationJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository.PaymentAllocationJpaRepository;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class PaymentAllocationPersistenceAdapter implements SavePaymentAllocationPort {
    private final PaymentAllocationJpaRepository paymentAllocationJpaRepository;

    @Override
    public void saveAll(List<PaymentAllocation> allocations) {
        List<PaymentAllocationJpaEntity> entities = allocations.stream()
                .map(PaymentAllocationJpaEntity::from)
                .toList();
        paymentAllocationJpaRepository.saveAll(entities);
    }
}
