package com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.SettlementPolicyJpaEntity;
import com.personal.marketnote.common.domain.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementPolicyJpaRepository extends JpaRepository<SettlementPolicyJpaEntity, Long> {
    Optional<SettlementPolicyJpaEntity> findBySellerIdAndStatus(Long sellerId, EntityStatus status);

    List<SettlementPolicyJpaEntity> findAllBySellerIdInAndStatus(List<Long> sellerIds, EntityStatus status);
}
