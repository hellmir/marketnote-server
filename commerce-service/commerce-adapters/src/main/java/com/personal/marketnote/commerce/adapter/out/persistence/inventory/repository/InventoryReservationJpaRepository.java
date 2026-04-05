package com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservationJpaEntity, Long> {
    List<InventoryReservationJpaEntity> findByOrderIdAndPricePolicyIdIn(Long orderId, Set<Long> pricePolicyIds);

    @Modifying
    @Query("DELETE FROM InventoryReservationJpaEntity e WHERE e.orderId = :orderId AND e.pricePolicyId IN :pricePolicyIds")
    void deleteByOrderIdAndPricePolicyIdIn(@Param("orderId") Long orderId, @Param("pricePolicyIds") Set<Long> pricePolicyIds);
}
