package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistories;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
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
public class ReduceProductInventoryService implements ReduceProductInventoryUseCase {
    private final FindInventoryPort findInventoryPort;
    private final UpdateInventoryPort updateInventoryPort;
    private final SaveInventoryDeductionHistoryPort saveInventoryDeductionHistoryPort;
    private final SaveCacheStockPort saveCacheStockPort;
    private final InventoryLockPort inventoryLockPort;

    @Override
    public void reduce(List<OrderProduct> orderProducts, String reason) {
        Map<Long, Integer> stocksByPricePolicyId = orderProducts.stream()
                .collect(
                        Collectors.groupingBy(
                                OrderProduct::getPricePolicyId, Collectors.summingInt(OrderProduct::getQuantity)
                        )
                );

        // Redisson Pub/Sub 모델 분산 락 기반의 동시성 제어
        inventoryLockPort.executeWithLock(stocksByPricePolicyId.keySet(), () -> {
            Set<Inventory> inventories = findInventoryPort.findByPricePolicyIds(stocksByPricePolicyId.keySet());
            inventories.forEach(inventory -> inventory.reduce(
                    stocksByPricePolicyId.get(inventory.getPricePolicyId())
            ));

            updateInventoryPort.update(inventories);
            Map<Long, Long> productIdsByPricePolicyId = new HashMap<>();
            inventories.forEach(inventory ->
                    productIdsByPricePolicyId.put(inventory.getPricePolicyId(), inventory.getProductId())
            );
            saveInventoryDeductionHistoryPort.save(
                    InventoryDeductionHistories.from(stocksByPricePolicyId, productIdsByPricePolicyId, reason)
            );
            saveCacheStockPort.save(inventories);
        });
    }
}
