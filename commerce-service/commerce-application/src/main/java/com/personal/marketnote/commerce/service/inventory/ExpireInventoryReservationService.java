package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistories;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ExpireInventoryReservationUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class ExpireInventoryReservationService implements ExpireInventoryReservationUseCase {
    private final FindExpiredInventoryReservationPort findExpiredInventoryReservationPort;
    private final FindInventoryReservationPort findInventoryReservationPort;
    private final FindInventoryPort findInventoryPort;
    private final UpdateInventoryPort updateInventoryPort;
    private final DeleteInventoryReservationPort deleteInventoryReservationPort;
    private final SaveInventoryRestorationHistoryPort saveInventoryRestorationHistoryPort;
    private final SaveCacheStockPort saveCacheStockPort;
    private final InventoryLockPort inventoryLockPort;
    private final PublishInventoryEventPort publishInventoryEventPort;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void expireTimedOutReservations(LocalDateTime cutoff) {
        List<InventoryReservation> expiredReservations =
                findExpiredInventoryReservationPort.findExpiredBefore(cutoff);

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("만료 예약 감지. count={}", expiredReservations.size());

        Map<Long, List<InventoryReservation>> reservationsByOrderId = expiredReservations.stream()
                .collect(Collectors.groupingBy(InventoryReservation::getOrderId));

        reservationsByOrderId.forEach(this::expireByOrderIdSafely);
    }

    private void expireByOrderIdSafely(Long orderId, List<InventoryReservation> reservations) {
        try {
            log.info("만료 예약 해소 시작. orderId={}, reservationCount={}", orderId, reservations.size());
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            transactionTemplate.executeWithoutResult(status ->
                    expireByOrderId(orderId, extractPricePolicyIds(reservations))
            );
            log.info("만료 예약 해소 완료. orderId={}", orderId);
        } catch (Exception e) {
            log.error("만료 예약 해소 실패. orderId={}", orderId, e);
        }
    }

    private void expireByOrderId(Long orderId, Set<Long> pricePolicyIds) {
        inventoryLockPort.executeWithLock(pricePolicyIds, () -> {
            List<InventoryReservation> currentReservations =
                    findInventoryReservationPort.findByOrderIdAndPricePolicyIds(orderId, pricePolicyIds);

            if (currentReservations.isEmpty()) {
                log.info("예약이 이미 처리됨. orderId={}", orderId);
                return;
            }

            Map<Long, Integer> quantityByPricePolicyId = currentReservations.stream()
                    .collect(Collectors.toMap(
                            InventoryReservation::getPricePolicyId,
                            InventoryReservation::getQuantity
                    ));

            Set<Long> currentPricePolicyIds = quantityByPricePolicyId.keySet();
            Set<Inventory> inventories = findInventoryPort.findByPricePolicyIds(currentPricePolicyIds);

            inventories.forEach(inventory ->
                    inventory.releaseReservation(quantityByPricePolicyId.get(inventory.getPricePolicyId()))
            );

            deleteInventoryReservationPort.deleteByOrderIdAndPricePolicyIds(orderId, currentPricePolicyIds);
            updateInventoryPort.update(inventories);

            Map<Long, Long> productIdsByPricePolicyId = inventories.stream()
                    .collect(Collectors.toMap(Inventory::getPricePolicyId, Inventory::getProductId));
            saveInventoryRestorationHistoryPort.save(
                    InventoryRestorationHistories.from(quantityByPricePolicyId, productIdsByPricePolicyId, orderId, "예약 만료 - 스케줄러 자동 해소")
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

    private Set<Long> extractPricePolicyIds(List<InventoryReservation> reservations) {
        return reservations.stream()
                .map(InventoryReservation::getPricePolicyId)
                .collect(Collectors.toSet());
    }
}
