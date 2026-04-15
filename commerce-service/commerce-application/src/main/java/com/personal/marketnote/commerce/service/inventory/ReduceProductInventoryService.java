package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistories;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import com.personal.marketnote.common.utility.FormatValidator;
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
    private final PublishInventoryEventPort publishInventoryEventPort;
    private final FindInventoryReservationPort findInventoryReservationPort;
    private final DeleteInventoryReservationPort deleteInventoryReservationPort;

    @Override
    public void reduce(List<OrderProduct> orderProducts, Long orderId, String reason) {
        Map<Long, Integer> stocksByPricePolicyId = orderProducts.stream()
                .collect(
                        Collectors.groupingBy(
                                OrderProduct::getPricePolicyId, Collectors.summingInt(OrderProduct::getQuantity)
                        )
                );

        // Redisson Pub/Sub 모델 분산 락 기반의 동시성 제어
        inventoryLockPort.executeWithLock(stocksByPricePolicyId.keySet(), () -> {
            Set<Inventory> inventories = findInventoryPort.findByPricePolicyIds(stocksByPricePolicyId.keySet());

            List<InventoryReservation> reservations = findInventoryReservationPort
                    .findByOrderIdAndPricePolicyIds(orderId, stocksByPricePolicyId.keySet());

            Map<Long, InventoryReservation> reservationByPricePolicyId = reservations.stream()
                    .collect(Collectors.toMap(InventoryReservation::getPricePolicyId, r -> r));

            Set<Long> reservedPricePolicyIds = reservationByPricePolicyId.keySet();

            inventories.forEach(inventory -> {
                int quantity = stocksByPricePolicyId.get(inventory.getPricePolicyId());
                InventoryReservation reservation = reservationByPricePolicyId.get(inventory.getPricePolicyId());
                if (FormatValidator.hasValue(reservation)) {
                    inventory.confirmReservation(reservation.getQuantity());
                    return;
                }
                inventory.reduce(quantity);
            });

            if (!reservedPricePolicyIds.isEmpty()) {
                deleteInventoryReservationPort.deleteByOrderIdAndPricePolicyIds(orderId, reservedPricePolicyIds);
            }

            updateInventoryPort.update(inventories);
            Map<Long, Long> productIdsByPricePolicyId = new HashMap<>();
            inventories.forEach(inventory ->
                    productIdsByPricePolicyId.put(inventory.getPricePolicyId(), inventory.getProductId())
            );
            saveInventoryDeductionHistoryPort.save(
                    InventoryDeductionHistories.from(stocksByPricePolicyId, productIdsByPricePolicyId, orderId, reason)
            );
            saveCacheStockPort.save(inventories);

            inventories.forEach(inventory ->
                    publishInventoryEventPort.publishInventoryChangedEvent(
                            inventory.getPricePolicyId(), inventory.getProductId(),
                            inventory.getStockValue(), InventoryChangeAction.UPDATED
                    )
            );
        });
    }
}
