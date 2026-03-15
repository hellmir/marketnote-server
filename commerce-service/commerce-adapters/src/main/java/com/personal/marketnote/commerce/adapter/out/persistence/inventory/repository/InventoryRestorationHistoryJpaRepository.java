package com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryRestorationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRestorationHistoryJpaRepository extends JpaRepository<InventoryRestorationHistoryJpaEntity, Long> {
}
