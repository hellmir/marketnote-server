package com.personal.marketnote.product.adapter.out.persistence.inventory.repository;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.inventory.entity.InventoryReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryReadModelJpaRepository extends JpaRepository<InventoryReadModelJpaEntity, Long> {

    Optional<InventoryReadModelJpaEntity> findByPricePolicyId(Long pricePolicyId);

    List<InventoryReadModelJpaEntity> findByPricePolicyIdInAndStatus(List<Long> pricePolicyIds, EntityStatus status);
}
