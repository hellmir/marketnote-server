package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.PaymentAllocationJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.settlement.mapper.PaymentAllocationEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository.PaymentAllocationJpaRepository;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdatePaymentAllocationPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class PaymentAllocationPersistenceAdapter
        implements SavePaymentAllocationPort, FindPaymentAllocationPort, UpdatePaymentAllocationPort {
    private final PaymentAllocationJpaRepository paymentAllocationJpaRepository;

    @Override
    public void saveAll(List<PaymentAllocation> allocations) {
        List<PaymentAllocationJpaEntity> entities = allocations.stream()
                .map(PaymentAllocationJpaEntity::from)
                .toList();
        paymentAllocationJpaRepository.saveAll(entities);
    }

    @Override
    public List<PaymentAllocation> findUnsettledAllocations(Integer year, Integer month) {
        return paymentAllocationJpaRepository.findAllUnsettledByPeriod(year, month).stream()
                .map(PaymentAllocationEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public List<PaymentAllocation> findBySettlementId(Long settlementId) {
        return paymentAllocationJpaRepository.findAllBySettlementId(settlementId).stream()
                .map(PaymentAllocationEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public void assignSettlement(List<Long> allocationIds, Long settlementId) {
        paymentAllocationJpaRepository.bulkAssignSettlement(allocationIds, settlementId);
    }
}
