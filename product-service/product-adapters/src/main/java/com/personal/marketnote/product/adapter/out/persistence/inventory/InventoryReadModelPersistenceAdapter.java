package com.personal.marketnote.product.adapter.out.persistence.inventory;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.inventory.entity.InventoryReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.inventory.repository.InventoryReadModelJpaRepository;
import com.personal.marketnote.product.port.out.inventory.FindStockPort;
import com.personal.marketnote.product.port.out.inventory.SaveInventoryReadModelPort;
import com.personal.marketnote.product.port.out.result.GetInventoryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class InventoryReadModelPersistenceAdapter implements FindStockPort, SaveInventoryReadModelPort {
    private final InventoryReadModelJpaRepository inventoryReadModelJpaRepository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public Set<GetInventoryResult> findByPricePolicyIds(List<Long> pricePolicyIds) {
        List<InventoryReadModelJpaEntity> entities =
                inventoryReadModelJpaRepository.findByPricePolicyIdInAndStatus(pricePolicyIds, EntityStatus.ACTIVE);

        Set<Long> foundPricePolicyIds = entities.stream()
                .map(InventoryReadModelJpaEntity::getPricePolicyId)
                .collect(Collectors.toSet());

        Set<GetInventoryResult> results = entities.stream()
                .map(entity -> GetInventoryResult.of(entity.getPricePolicyId(), entity.getStockQuantity()))
                .collect(Collectors.toSet());

        pricePolicyIds.stream()
                .filter(id -> !foundPricePolicyIds.contains(id))
                .map(GetInventoryResult::generateResultWithoutStock)
                .forEach(results::add);

        return results;
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long pricePolicyId, Long productId, Integer stockQuantity) {
        Optional<InventoryReadModelJpaEntity> existing =
                inventoryReadModelJpaRepository.findByPricePolicyId(pricePolicyId);

        if (existing.isPresent()) {
            existing.get().updateFrom(stockQuantity);
            return;
        }

        try {
            InventoryReadModelJpaEntity entity = InventoryReadModelJpaEntity.of(pricePolicyId, productId, stockQuantity);
            inventoryReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("재고 Read Model 중복 저장 (멱등 처리). pricePolicyId={}", pricePolicyId);
            inventoryReadModelJpaRepository.findByPricePolicyId(pricePolicyId)
                    .ifPresent(entity -> entity.updateFrom(stockQuantity));
        }
    }
}
