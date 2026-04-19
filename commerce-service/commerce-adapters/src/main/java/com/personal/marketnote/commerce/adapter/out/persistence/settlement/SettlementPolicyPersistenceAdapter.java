package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.SettlementPolicyJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.settlement.mapper.SettlementPolicyEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository.SettlementPolicyJpaRepository;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.SaveSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPolicyPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class SettlementPolicyPersistenceAdapter
        implements FindSettlementPolicyPort, SaveSettlementPolicyPort, UpdateSettlementPolicyPort {

    private final SettlementPolicyJpaRepository settlementPolicyJpaRepository;

    @Override
    public Optional<SettlementPolicy> findById(Long id) {
        return settlementPolicyJpaRepository.findById(id)
                .map(SettlementPolicyEntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<SettlementPolicy> findActiveBySellerId(Long sellerId) {
        return settlementPolicyJpaRepository.findBySellerIdAndStatus(sellerId, EntityStatus.ACTIVE)
                .map(SettlementPolicyEntityToDomainMapper::toDomain);
    }

    @Override
    public List<SettlementPolicy> findAll() {
        return settlementPolicyJpaRepository.findAll().stream()
                .map(SettlementPolicyEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public Map<Long, SettlementPolicy> findActiveBySellerIdIn(List<Long> sellerIds) {
        List<SettlementPolicyJpaEntity> entities =
                settlementPolicyJpaRepository.findAllBySellerIdInAndStatus(sellerIds, EntityStatus.ACTIVE);

        return entities.stream()
                .map(SettlementPolicyEntityToDomainMapper::toDomain)
                .collect(Collectors.toMap(SettlementPolicy::getSellerId, policy -> policy));
    }

    @Override
    public SettlementPolicy save(SettlementPolicy settlementPolicy) {
        SettlementPolicyJpaEntity entity = SettlementPolicyJpaEntity.from(settlementPolicy);
        SettlementPolicyJpaEntity saved = settlementPolicyJpaRepository.save(entity);
        return SettlementPolicyEntityToDomainMapper.toDomain(saved);
    }

    @Override
    public SettlementPolicy update(SettlementPolicy settlementPolicy) {
        SettlementPolicyJpaEntity entity = SettlementPolicyJpaEntity.from(settlementPolicy);
        SettlementPolicyJpaEntity updated = settlementPolicyJpaRepository.save(entity);
        return SettlementPolicyEntityToDomainMapper.toDomain(updated);
    }
}
