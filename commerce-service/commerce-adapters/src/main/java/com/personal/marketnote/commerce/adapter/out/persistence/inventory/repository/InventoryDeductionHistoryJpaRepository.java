package com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryDeductionHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryDeductionHistoryJpaRepository extends JpaRepository<InventoryDeductionHistoryJpaEntity, Long> {
}
