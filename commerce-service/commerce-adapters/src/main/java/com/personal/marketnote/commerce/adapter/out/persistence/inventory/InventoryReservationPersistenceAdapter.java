package com.personal.marketnote.commerce.adapter.out.persistence.inventory;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryReservationJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryReservationJpaRepository;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservationSnapshotState;
import com.personal.marketnote.commerce.exception.DuplicateInventoryReservationException;
import com.personal.marketnote.commerce.port.out.inventory.DeleteInventoryReservationPort;
import com.personal.marketnote.commerce.port.out.inventory.FindExpiredInventoryReservationPort;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryReservationPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveInventoryReservationPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@PersistenceAdapter
@RequiredArgsConstructor
public class InventoryReservationPersistenceAdapter implements
        SaveInventoryReservationPort, FindInventoryReservationPort, FindExpiredInventoryReservationPort, DeleteInventoryReservationPort {
    private static final int EXPIRED_RESERVATION_BATCH_SIZE = 500;
    private final InventoryReservationJpaRepository inventoryReservationJpaRepository;

    @Override
    public void save(List<InventoryReservation> inventoryReservations) {
        try {
            inventoryReservationJpaRepository.saveAllAndFlush(
                    inventoryReservations.stream()
                            .map(InventoryReservationJpaEntity::from)
                            .toList()
            );
        } catch (DataIntegrityViolationException e) {
            Long orderId = inventoryReservations.stream()
                    .findFirst()
                    .map(InventoryReservation::getOrderId)
                    .orElse(null);
            throw new DuplicateInventoryReservationException(orderId);
        }
    }

    @Override
    public List<InventoryReservation> findByOrderIdAndPricePolicyIds(Long orderId, Set<Long> pricePolicyIds) {
        return inventoryReservationJpaRepository.findByOrderIdAndPricePolicyIdIn(orderId, pricePolicyIds)
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<InventoryReservation> findExpiredBefore(LocalDateTime cutoff) {
        return inventoryReservationJpaRepository
                .findByReservedAtBefore(cutoff, PageRequest.of(0, EXPIRED_RESERVATION_BATCH_SIZE))
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    private InventoryReservation mapToDomain(InventoryReservationJpaEntity entity) {
        return InventoryReservation.from(InventoryReservationSnapshotState.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .pricePolicyId(entity.getPricePolicyId())
                .quantity(entity.getQuantity())
                .reservedAt(entity.getReservedAt())
                .build());
    }

    @Override
    public void deleteByOrderIdAndPricePolicyIds(Long orderId, Set<Long> pricePolicyIds) {
        inventoryReservationJpaRepository.deleteByOrderIdAndPricePolicyIdIn(orderId, pricePolicyIds);
    }
}
