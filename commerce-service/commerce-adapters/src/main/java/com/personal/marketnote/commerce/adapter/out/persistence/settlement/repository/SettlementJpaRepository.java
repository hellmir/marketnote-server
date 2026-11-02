package com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.SettlementJpaEntity;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, Long> {

    Optional<SettlementJpaEntity> findBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month);

    List<SettlementJpaEntity> findAllByYearAndMonth(Integer year, Integer month);

    boolean existsBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month);

    List<SettlementJpaEntity> findAllBySellerIdAndYear(Long sellerId, Integer year);

    List<SettlementJpaEntity> findAllByStatus(SettlementStatus status);
}
