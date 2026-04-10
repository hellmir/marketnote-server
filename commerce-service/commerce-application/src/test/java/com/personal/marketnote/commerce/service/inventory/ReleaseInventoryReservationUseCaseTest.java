package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservationSnapshotState;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistories;
import com.personal.marketnote.commerce.domain.inventory.InventorySnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseInventoryReservationUseCaseTest {
    @Mock
    private FindInventoryPort findInventoryPort;
    @Mock
    private UpdateInventoryPort updateInventoryPort;
    @Mock
    private FindInventoryReservationPort findInventoryReservationPort;
    @Mock
    private DeleteInventoryReservationPort deleteInventoryReservationPort;
    @Mock
    private SaveInventoryRestorationHistoryPort saveInventoryRestorationHistoryPort;
    @Mock
    private SaveCacheStockPort saveCacheStockPort;
    @Mock
    private InventoryLockPort inventoryLockPort;
    @Mock
    private PublishInventoryEventPort publishInventoryEventPort;

    @InjectMocks
    private ReleaseInventoryReservationService releaseInventoryReservationService;

    // ==================================================================================
    // 예약 해소 (예약 레코드 존재 시 — releaseReservation)
    // ==================================================================================

    @Nested
    @DisplayName("예약 레코드 존재 시 예약 해소 (releaseReservation)")
    class ReleaseWithReservationTest {

        @Test
        @DisplayName("예약 레코드가 존재하면 reserved를 감소시키고 stock은 유지한다")
        void release_withReservation_decreasesReservedAndKeepsStock() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of(reservation));

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상 - 예약 해소");

            assertThat(inventory.getStockValue()).isEqualTo(10);
            assertThat(inventory.getReserved()).isEqualTo(0);
        }

        @Test
        @DisplayName("예약 레코드가 존재하면 예약 레코드를 삭제한다")
        void release_withReservation_deletesReservationRecords() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of(reservation));

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상 - 예약 해소");

            verify(deleteInventoryReservationPort).deleteByOrderIdAndPricePolicyIds(eq(1L), eq(Set.of(100L)));
        }

        @Test
        @DisplayName("예약 해소 후 복원 이력을 저장한다")
        void release_withReservation_savesRestorationHistory() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of(reservation));

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상 - 예약 해소");

            verify(saveInventoryRestorationHistoryPort).save(any(InventoryRestorationHistories.class));
        }

        @Test
        @DisplayName("예약 해소 후 캐시와 이벤트를 갱신한다")
        void release_withReservation_updatesCacheAndPublishesEvent() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of(reservation));

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상 - 예약 해소");

            verify(saveCacheStockPort).save(any(Set.class));
            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 10, InventoryChangeAction.UPDATED
            );
        }
    }

    // ==================================================================================
    // 예약 레코드 미존재 시 — restore fallback
    // ==================================================================================

    @Nested
    @DisplayName("예약 레코드 미존재 시 restore fallback")
    class RestoreFallbackTest {

        @Test
        @DisplayName("예약 레코드가 없으면 restore()로 stock을 복원한다")
        void release_withoutReservation_restoresStock() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of());

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상 - 재고 복구");

            assertThat(inventory.getStockValue()).isEqualTo(10);
        }

        @Test
        @DisplayName("예약 레코드가 없으면 예약 삭제를 호출하지 않는다")
        void release_withoutReservation_doesNotDeleteReservation() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of());

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상 - 재고 복구");

            verifyNoInteractions(deleteInventoryReservationPort);
        }

        @Test
        @DisplayName("restore fallback 후 복원 이력과 캐시를 저장한다")
        void release_withoutReservation_savesHistoryAndCache() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of());

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상 - 재고 복구");

            verify(saveInventoryRestorationHistoryPort).save(any(InventoryRestorationHistories.class));
            verify(saveCacheStockPort).save(any(Set.class));
            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 10, InventoryChangeAction.UPDATED
            );
        }
    }

    // ==================================================================================
    // 혼합 케이스
    // ==================================================================================

    @Nested
    @DisplayName("복수 가격 정책 혼합 케이스")
    class MixedCaseTest {

        @Test
        @DisplayName("일부 예약 있고 일부 없는 경우 각각 releaseReservation과 restore로 처리한다")
        void release_partialReservation_mixesReleaseAndRestore() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventoryWithReserved(1L, 100L, 10, 2);
            Inventory inventory2 = buildInventory(2L, 200L, 15);
            InventoryReservation reservation = buildReservation(1L, 100L, 2);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(eq(1L), any()))
                    .thenReturn(List.of(reservation));

            releaseInventoryReservationService.release(orderProducts, 1L, "SAGA 보상");

            assertThat(inventory1.getStockValue()).isEqualTo(10);
            assertThat(inventory1.getReserved()).isEqualTo(0);
            assertThat(inventory2.getStockValue()).isEqualTo(20);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private void stubLockToExecuteTask() {
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(inventoryLockPort).executeWithLock(any(), any());
    }

    private OrderProduct buildOrderProduct(Long pricePolicyId, int quantity) {
        return OrderProduct.from(
                OrderProductSnapshotState.builder()
                        .pricePolicyId(pricePolicyId)
                        .quantity(quantity)
                        .build()
        );
    }

    private Inventory buildInventory(Long productId, Long pricePolicyId, int stock) {
        return Inventory.of(productId, pricePolicyId, stock, 0L);
    }

    private Inventory buildInventoryWithReserved(Long productId, Long pricePolicyId, int stock, int reserved) {
        return Inventory.from(InventorySnapshotState.builder()
                .productId(productId)
                .pricePolicyId(pricePolicyId)
                .stock(stock)
                .version(0L)
                .reserved(reserved)
                .build());
    }

    private InventoryReservation buildReservation(Long orderId, Long pricePolicyId, int quantity) {
        return InventoryReservation.from(InventoryReservationSnapshotState.builder()
                .id(1L)
                .orderId(orderId)
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .reservedAt(LocalDateTime.now())
                .build());
    }
}
