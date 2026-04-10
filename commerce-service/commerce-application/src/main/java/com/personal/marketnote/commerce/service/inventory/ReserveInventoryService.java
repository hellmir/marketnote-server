package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservationCreateState;
import com.personal.marketnote.commerce.exception.InventoryNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.ReserveInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReserveInventoryUseCase;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ReserveInventoryService implements ReserveInventoryUseCase {
    private final FindInventoryPort findInventoryPort;
    private final UpdateInventoryPort updateInventoryPort;
    private final SaveInventoryReservationPort saveInventoryReservationPort;
    private final SaveCacheStockPort saveCacheStockPort;
    private final InventoryLockPort inventoryLockPort;
    private final Clock clock;

    @Override
    public void reserveInventory(ReserveInventoryCommand command) {
        Map<Long, Integer> quantitiesByPricePolicyId = command.orderProducts().stream()
                .collect(Collectors.groupingBy(
                        ReserveInventoryCommand.OrderProductItem::pricePolicyId,
                        Collectors.summingInt(ReserveInventoryCommand.OrderProductItem::quantity)
                ));

        inventoryLockPort.executeWithLock(quantitiesByPricePolicyId.keySet(), () -> {
            Set<Inventory> inventories = findInventoryPort.findByPricePolicyIds(quantitiesByPricePolicyId.keySet());

            validateAllInventoriesFound(inventories, quantitiesByPricePolicyId.keySet());

            inventories.forEach(inventory ->
                    inventory.reserve(quantitiesByPricePolicyId.get(inventory.getPricePolicyId()))
            );

            updateInventoryPort.update(inventories);

            List<InventoryReservation> reservations = createReservations(
                    command.orderId(), quantitiesByPricePolicyId
            );
            saveInventoryReservationPort.save(reservations);

            saveCacheStockPort.save(inventories);
        });
    }

    private void validateAllInventoriesFound(Set<Inventory> inventories, Set<Long> requestedPricePolicyIds) {
        if (inventories.size() == requestedPricePolicyIds.size()) {
            return;
        }
        Set<Long> foundPricePolicyIds = inventories.stream()
                .map(Inventory::getPricePolicyId)
                .collect(Collectors.toSet());
        Long missingPricePolicyId = requestedPricePolicyIds.stream()
                .filter(id -> !foundPricePolicyIds.contains(id))
                .findFirst()
                .orElseThrow();
        throw new InventoryNotFoundException(missingPricePolicyId);
    }

    private List<InventoryReservation> createReservations(Long orderId,
                                                           Map<Long, Integer> quantitiesByPricePolicyId) {
        LocalDateTime reservedAt = LocalDateTime.now(clock);
        return quantitiesByPricePolicyId.entrySet().stream()
                .map(entry -> InventoryReservation.from(
                        InventoryReservationCreateState.builder()
                                .orderId(orderId)
                                .pricePolicyId(entry.getKey())
                                .quantity(entry.getValue())
                                .reservedAt(reservedAt)
                                .build()
                ))
                .toList();
    }
}
