package com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, Long> {
    Optional<InventoryJpaEntity> findByPricePolicyId(Long pricePolicyId);

    @Query("""
            SELECT i FROM InventoryJpaEntity i
            WHERE i.pricePolicyId
            IN :pricePolicyIds
            ORDER BY i.pricePolicyId ASC
            """)
    Set<InventoryJpaEntity> findByPricePolicyIds(Set<Long> pricePolicyIds);

    @Query("""
            SELECT i FROM InventoryJpaEntity i
            WHERE i.productId
            IN :productIds
            ORDER BY i.productId ASC
            """)
    Set<InventoryJpaEntity> findByProductIds(Set<Long> productIds);

    @Query("""
            SELECT i FROM InventoryJpaEntity i
            WHERE i.productKey
            IN :productKeys
            ORDER BY i.pricePolicyId ASC
            """)
    Set<InventoryJpaEntity> findByProductKeys(Set<UUID> productKeys);

    @Query("""
            SELECT COUNT(i) > 0 FROM InventoryJpaEntity i
            WHERE i.pricePolicyId = :pricePolicyId
            """)
    boolean existsByPricePolicyId(Long pricePolicyId);
}
