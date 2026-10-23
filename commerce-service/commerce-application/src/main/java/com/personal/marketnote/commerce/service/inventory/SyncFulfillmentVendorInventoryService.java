package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.exception.InvalidFulfillmentSyncCommandException;
import com.personal.marketnote.commerce.exception.InventoryProductNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryItemCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.SyncFulfillmentVendorInventoryUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.InventoryLockPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import com.personal.marketnote.commerce.port.out.inventory.UpdateInventoryPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
    private final PublishInventoryEventPort publishInventoryEventPort;

    @Override
    public void syncInventories(SyncFulfillmentVendorInventoryCommand command) {
        if (FormatValidator.hasNoValue(command) || FormatValidator.hasNoValue(command.inventories())) {
            throw new InvalidFulfillmentSyncCommandException();
        }

        List<SyncFulfillmentVendorInventoryItemCommand> items = command.inventories();
        if (items.size() > SyncFulfillmentVendorInventoryCommand.MAX_INVENTORIES_SIZE) {
            throw new InvalidFulfillmentSyncCommandException();
        }
        validateProductKeys(items);

        Set<UUID> productKeys = items.stream()
                .map(SyncFulfillmentVendorInventoryItemCommand::productKey)
                .collect(Collectors.toSet());
        Set<Inventory> inventories = findInventoryPort.findByProductKeys(productKeys);
        validateInventoryExistsByProductKey(productKeys, inventories);

        Map<Long, Integer> stocksByProductId = resolveStocksByProductId(inventories, items);

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

            updatedInventories.forEach(inventory ->
                    publishInventoryEventPort.publishInventoryChangedEvent(
                            inventory.getPricePolicyId(), inventory.getProductId(),
                            inventory.getStockValue(), InventoryChangeAction.UPDATED
                    )
            );
        });
    }

    private void validateProductKeys(List<SyncFulfillmentVendorInventoryItemCommand> items) {
        boolean hasInvalidItem = items.stream()
                .anyMatch(item -> FormatValidator.hasNoValue(item.productKey()));
        if (hasInvalidItem) {
            throw new InvalidFulfillmentSyncCommandException();
        }
    }

    private Map<Long, Integer> resolveStocksByProductId(
            Set<Inventory> inventories,
            List<SyncFulfillmentVendorInventoryItemCommand> items
    ) {
        Map<UUID, Long> productIdByProductKey = inventories.stream()
                .filter(Inventory::hasProductKey)
                .collect(Collectors.toMap(Inventory::getProductKey, Inventory::getProductId, (a, b) -> a));

        Map<Long, Integer> stocksByProductId = new LinkedHashMap<>();
        for (SyncFulfillmentVendorInventoryItemCommand item : items) {
            Long productId = productIdByProductKey.get(item.productKey());
            if (FormatValidator.hasNoValue(productId)) {
                throw new InvalidFulfillmentSyncCommandException();
            }
            stocksByProductId.put(productId, item.stock());
        }

        return stocksByProductId;
    }

    private void validateInventoryExistsByProductKey(Set<UUID> productKeys, Set<Inventory> inventories) {
        Set<UUID> foundProductKeys = inventories.stream()
                .filter(Inventory::hasProductKey)
                .map(Inventory::getProductKey)
                .collect(Collectors.toSet());

        UUID missingProductKey = productKeys.stream()
                .filter(productKey -> !foundProductKeys.contains(productKey))
                .findFirst()
                .orElse(null);

        if (FormatValidator.hasValue(missingProductKey)) {
            throw new InventoryProductNotFoundException(missingProductKey);
        }
    }
}
