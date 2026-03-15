package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistories;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RestoreProductInventoryService implements RestoreProductInventoryUseCase {
    private final FindInventoryPort findInventoryPort;
    private final UpdateInventoryPort updateInventoryPort;
    private final SaveInventoryRestorationHistoryPort saveInventoryRestorationHistoryPort;
    private final SaveCacheStockPort saveCacheStockPort;
    private final InventoryLockPort inventoryLockPort;

    @Override
    public void restore(List<OrderProduct> orderProducts, Long orderId, String reason) {
        Map<Long, Integer> stocksByPricePolicyId = orderProducts.stream()
                .collect(
                        Collectors.groupingBy(
                                OrderProduct::getPricePolicyId, Collectors.summingInt(OrderProduct::getQuantity)
                        )
                );

        inventoryLockPort.executeWithLock(stocksByPricePolicyId.keySet(), () -> {
            Set<Inventory> inventories = findInventoryPort.findByPricePolicyIds(stocksByPricePolicyId.keySet());
            inventories.forEach(inventory -> inventory.restore(
                    stocksByPricePolicyId.get(inventory.getPricePolicyId())
            ));

            updateInventoryPort.update(inventories);

            Map<Long, Long> productIdsByPricePolicyId = new HashMap<>();
            inventories.forEach(inventory ->
                    productIdsByPricePolicyId.put(inventory.getPricePolicyId(), inventory.getProductId())
            );
            saveInventoryRestorationHistoryPort.save(
                    InventoryRestorationHistories.from(stocksByPricePolicyId, productIdsByPricePolicyId, orderId, reason)
            );

            saveCacheStockPort.save(inventories);
        });
    }
}
