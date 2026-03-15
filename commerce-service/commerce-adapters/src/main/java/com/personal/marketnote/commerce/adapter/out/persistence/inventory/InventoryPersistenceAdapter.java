package com.personal.marketnote.commerce.adapter.out.persistence.inventory;

import com.personal.marketnote.commerce.adapter.out.mapper.InventoryJpaEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryDeductionHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryRestorationHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryDeductionHistoryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryRestorationHistoryJpaRepository;
import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistories;
import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistory;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistories;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistory;
import com.personal.marketnote.commerce.exception.DuplicateInventoryDeductionException;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.exception.InventoryNotFoundException;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class InventoryPersistenceAdapter implements SaveInventoryPort, FindInventoryPort, UpdateInventoryPort, SaveInventoryDeductionHistoryPort, SaveInventoryRestorationHistoryPort {
    private final InventoryJpaRepository inventoryJpaRepository;
    private final InventoryDeductionHistoryJpaRepository inventoryDeductionHistoryJpaRepository;
    private final InventoryRestorationHistoryJpaRepository inventoryRestorationHistoryJpaRepository;

    @Override
    public void save(Inventory inventory) {
        InventoryJpaEntity inventoryEntity = InventoryJpaEntity.from(inventory);
        inventoryJpaRepository.save(inventoryEntity);
    }

    @Override
    public void save(Set<Inventory> inventories) {
        inventoryJpaRepository.saveAll(
                inventories.stream()
                        .map(InventoryJpaEntity::from)
                        .toList()
        );
    }

    @Override
    public Set<Inventory> findByPricePolicyIds(Set<Long> pricePolicyIds) {
        return inventoryJpaRepository.findByPricePolicyIds(pricePolicyIds)
                .stream()
                .map(InventoryJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Inventory> findByProductIds(Set<Long> productIds) {
        return inventoryJpaRepository.findByProductIds(productIds)
                .stream()
                .map(InventoryJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean existsByPricePolicyId(Long pricePolicyId) {
        return inventoryJpaRepository.existsByPricePolicyId(pricePolicyId);
    }

    @Override
    public void update(Set<Inventory> inventories) throws InventoryNotFoundException {
        Set<Long> pricePolicyIds = inventories.stream()
                .map(Inventory::getPricePolicyId)
                .collect(Collectors.toSet());

        Set<InventoryJpaEntity> inventoryEntities = inventoryJpaRepository.findByPricePolicyIds(pricePolicyIds);

        for (Inventory inventory : inventories) {
            InventoryJpaEntity inventoryEntity = inventoryEntities.stream()
                    .filter(entity -> entity.getPricePolicyId().equals(inventory.getPricePolicyId()))
                    .findFirst()
                    .orElseThrow(() -> new InventoryNotFoundException(inventory.getPricePolicyId()));

            inventoryEntity.updateFrom(inventory);
        }
    }

    @Override
    public void save(InventoryDeductionHistories inventoryDeductionHistories) {
        try {
            inventoryDeductionHistoryJpaRepository.saveAllAndFlush(
                    inventoryDeductionHistories.getInventoryDeductionHistories()
                            .stream()
                            .map(InventoryDeductionHistoryJpaEntity::from)
                            .toList()
            );
        } catch (DataIntegrityViolationException e) {
            Long orderId = inventoryDeductionHistories.getInventoryDeductionHistories()
                    .stream()
                    .findFirst()
                    .map(InventoryDeductionHistory::getOrderId)
                    .orElse(null);
            throw new DuplicateInventoryDeductionException(orderId);
        }
    }

    @Override
    public void save(InventoryRestorationHistories inventoryRestorationHistories) {
        try {
            inventoryRestorationHistoryJpaRepository.saveAllAndFlush(
                    inventoryRestorationHistories.getInventoryRestorationHistories()
                            .stream()
                            .map(InventoryRestorationHistoryJpaEntity::from)
                            .toList()
            );
        } catch (DataIntegrityViolationException e) {
            Long orderId = inventoryRestorationHistories.getInventoryRestorationHistories()
                    .stream()
                    .findFirst()
                    .map(InventoryRestorationHistory::getOrderId)
                    .orElse(null);
            throw new DuplicateInventoryRestorationException(orderId);
        }
    }
}
