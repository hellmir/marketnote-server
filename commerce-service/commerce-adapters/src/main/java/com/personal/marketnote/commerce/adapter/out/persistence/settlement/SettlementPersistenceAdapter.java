package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.SettlementJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.settlement.mapper.SettlementEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository.SettlementJpaRepository;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.SaveSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SettlementPersistenceAdapter implements SaveSettlementPort, FindSettlementPort, UpdateSettlementPort {
    private final SettlementJpaRepository settlementJpaRepository;

    @Override
    public Settlement save(Settlement settlement) {
        SettlementJpaEntity entity = SettlementJpaEntity.from(settlement);
        SettlementJpaEntity saved = settlementJpaRepository.save(entity);
        return SettlementEntityToDomainMapper.toDomain(saved);
    }

    @Override
    public Optional<Settlement> findById(Long id) {
        return settlementJpaRepository.findById(id)
                .map(SettlementEntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<Settlement> findBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month) {
        return settlementJpaRepository.findBySellerIdAndYearAndMonth(sellerId, year, month)
                .map(SettlementEntityToDomainMapper::toDomain);
    }

    @Override
    public List<Settlement> findAllByYearAndMonth(Integer year, Integer month) {
        return settlementJpaRepository.findAllByYearAndMonth(year, month).stream()
                .map(SettlementEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month) {
        return settlementJpaRepository.existsBySellerIdAndYearAndMonth(sellerId, year, month);
    }

    @Override
    public List<Settlement> findAllBySellerIdAndYear(Long sellerId, Integer year) {
        return settlementJpaRepository.findAllBySellerIdAndYear(sellerId, year).stream()
                .map(SettlementEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public List<Settlement> findAllByStatus(SettlementStatus status) {
        return settlementJpaRepository.findAllByStatus(status).stream()
                .map(SettlementEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public Settlement update(Settlement settlement) {
        SettlementJpaEntity entity = SettlementJpaEntity.from(settlement);
        SettlementJpaEntity updated = settlementJpaRepository.save(entity);
        return SettlementEntityToDomainMapper.toDomain(updated);
    }
}
