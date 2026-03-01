package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.exception.InvalidFulfillmentSyncCommandException;
import com.personal.marketnote.commerce.exception.InventoryProductNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryItemCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.SyncFulfillmentVendorInventoryUseCase;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.InventoryLockPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import com.personal.marketnote.commerce.port.out.inventory.UpdateInventoryPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class SyncFulfillmentVendorInventoryService implements SyncFulfillmentVendorInventoryUseCase {
    private final FindInventoryPort findInventoryPort;
    private final UpdateInventoryPort updateInventoryPort;
    private final SaveCacheStockPort saveCacheStockPort;
    private final InventoryLockPort inventoryLockPort;

    @Override
    public void syncInventories(SyncFulfillmentVendorInventoryCommand command) {
        if (FormatValidator.hasNoValue(command) || FormatValidator.hasNoValue(command.inventories())) {
            throw new InvalidFulfillmentSyncCommandException();
        }

        Map<Long, Integer> stocksByProductId = resolveStocksByProductId(command.inventories());
        Set<Long> productIds = stocksByProductId.keySet();

        Set<Inventory> inventories = findInventoryPort.findByProductIds(productIds);
        validateInventoryExists(productIds, inventories);

        Set<Long> pricePolicyIds = inventories.stream()
                .map(Inventory::getPricePolicyId)
                .collect(Collectors.toSet());
        inventoryLockPort.executeWithLock(pricePolicyIds, () -> {
            Set<Inventory> lockedInventories = findInventoryPort.findByPricePolicyIds(pricePolicyIds);
            Set<Inventory> updatedInventories = lockedInventories.stream()
                    .filter(inventory -> stocksByProductId.containsKey(inventory.getProductId()))
                    .map(inventory -> Inventory.of(
                            inventory.getProductId(),
                            inventory.getPricePolicyId(),
                            stocksByProductId.get(inventory.getProductId()),
                            inventory.getVersion()
                    ))
                    .collect(Collectors.toSet());

            updateInventoryPort.update(updatedInventories);
            saveCacheStockPort.save(updatedInventories);
        });
    }

    private Map<Long, Integer> resolveStocksByProductId(
            Iterable<SyncFulfillmentVendorInventoryItemCommand> items
    ) {
        Map<Long, Integer> stocksByProductId = new LinkedHashMap<>();
        for (SyncFulfillmentVendorInventoryItemCommand item : items) {
            stocksByProductId.put(item.productId(), item.stock());
        }

        return stocksByProductId;
    }

    private void validateInventoryExists(Set<Long> productIds, Set<Inventory> inventories) {
        Set<Long> foundProductIds = inventories.stream()
                .map(Inventory::getProductId)
                .collect(Collectors.toSet());

        Long missingProductId = productIds.stream()
                .filter(productId -> !foundProductIds.contains(productId))
                .findFirst()
                .orElse(null);

        if (FormatValidator.hasValue(missingProductId)) {
            throw new InventoryProductNotFoundException(missingProductId);
        }
    }
}
