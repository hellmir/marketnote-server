package com.personal.marketnote.commerce.adapter.out.persistence.inventory;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity.InventoryReservationJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryReservationJpaRepository;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.exception.DuplicateInventoryReservationException;
import com.personal.marketnote.commerce.port.out.inventory.SaveInventoryReservationPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class InventoryReservationPersistenceAdapter implements SaveInventoryReservationPort {
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
}
