package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistories;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReleaseInventoryReservationUseCase;
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
public class ReleaseInventoryReservationService implements ReleaseInventoryReservationUseCase {
    private final FindInventoryPort findInventoryPort;
    private final UpdateInventoryPort updateInventoryPort;
    private final FindInventoryReservationPort findInventoryReservationPort;
    private final DeleteInventoryReservationPort deleteInventoryReservationPort;
    private final SaveInventoryRestorationHistoryPort saveInventoryRestorationHistoryPort;
    private final SaveCacheStockPort saveCacheStockPort;
    private final InventoryLockPort inventoryLockPort;
    private final PublishInventoryEventPort publishInventoryEventPort;

    @Override
    public void release(List<OrderProduct> orderProducts, Long orderId, String reason) {
        Map<Long, Integer> stocksByPricePolicyId = orderProducts.stream()
                .collect(Collectors.groupingBy(
                        OrderProduct::getPricePolicyId, Collectors.summingInt(OrderProduct::getQuantity)
                ));

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
                    inventory.releaseReservation(reservation.getQuantity());
                    return;
                }
                inventory.restore(quantity);
            });

            if (!reservedPricePolicyIds.isEmpty()) {
                deleteInventoryReservationPort.deleteByOrderIdAndPricePolicyIds(orderId, reservedPricePolicyIds);
            }

            updateInventoryPort.update(inventories);

            Map<Long, Long> productIdsByPricePolicyId = new HashMap<>();
            inventories.forEach(inventory ->
                    productIdsByPricePolicyId.put(inventory.getPricePolicyId(), inventory.getProductId())
            );
            saveInventoryRestorationHistoryPort.save(
                    InventoryRestorationHistories.from(stocksByPricePolicyId, productIdsByPricePolicyId, orderId, reason)
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
