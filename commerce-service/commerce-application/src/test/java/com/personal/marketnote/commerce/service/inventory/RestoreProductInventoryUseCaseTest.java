package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistories;
import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistory;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestoreProductInventoryUseCaseTest {
    @Mock
    private FindInventoryPort findInventoryPort;
    @Mock
    private UpdateInventoryPort updateInventoryPort;
    @Mock
    private SaveInventoryRestorationHistoryPort saveInventoryRestorationHistoryPort;
    @Mock
    private SaveCacheStockPort saveCacheStockPort;
    @Mock
    private InventoryLockPort inventoryLockPort;
    @Mock
    private PublishInventoryEventPort publishInventoryEventPort;

    @InjectMocks
    private RestoreProductInventoryService restoreProductInventoryService;

    @Nested
    @DisplayName("restore (재고 복구)")
    class RestoreTest {

        @Test
        @DisplayName("단일 주문 상품 재고 복구 시 재고가 올바르게 증가한다")
        void restore_singleOrderProduct_increasesInventoryStock() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            assertThat(inventory.getStockValue()).isEqualTo(10);
        }

        @Test
        @DisplayName("서로 다른 가격 정책의 복수 주문 상품 재고 복구 시 각각의 재고가 올바르게 증가한다")
        void restore_differentPolicies_restoresEachInventory() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 8);
            Inventory inventory2 = buildInventory(2L, 200L, 15);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            assertThat(inventory1.getStockValue()).isEqualTo(10);
            assertThat(inventory2.getStockValue()).isEqualTo(20);
        }

        @Test
        @DisplayName("동일 가격 정책의 복수 주문 상품 재고 복구 시 수량을 합산하여 복구한다")
        void restore_samePolicyMultipleProducts_sumsQuantityAndRestores() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(100L, 3)
            );
            Inventory inventory = buildInventory(1L, 100L, 5);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            assertThat(inventory.getStockValue()).isEqualTo(10);
        }

        @Test
        @DisplayName("재고 복구 시 분산 락에 올바른 가격 정책 ID Set을 전달한다")
        @SuppressWarnings("unchecked")
        void restore_success_passesCorrectPricePolicyIdsToLock() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 3)
            );

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventory(1L, 100L, 8),
                    buildInventory(2L, 200L, 17)
            ));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            ArgumentCaptor<Set<Long>> lockKeysCaptor = ArgumentCaptor.forClass(Set.class);
            verify(inventoryLockPort).executeWithLock(lockKeysCaptor.capture(), any());
            assertThat(lockKeysCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("재고 복구 시 재고 조회 → 업데이트 → 이력 저장 → 캐시 저장 순서로 호출한다")
        @SuppressWarnings("unchecked")
        void restore_success_callsPortsInCorrectOrder() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            InOrder inOrder = inOrder(findInventoryPort, updateInventoryPort,
                    saveInventoryRestorationHistoryPort, saveCacheStockPort);
            inOrder.verify(findInventoryPort).findByPricePolicyIds(any());
            inOrder.verify(updateInventoryPort).update(any());
            inOrder.verify(saveInventoryRestorationHistoryPort).save(any(InventoryRestorationHistories.class));
            inOrder.verify(saveCacheStockPort).save(any(Set.class));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("분산 락이 작업을 실행하지 않으면 내부 Port를 호출하지 않는다")
        void restore_lockNotExecuted_doesNotCallInternalPorts() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            verify(inventoryLockPort).executeWithLock(any(), any());
            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryRestorationHistoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("재고 조회 중 예외 발생 시 예외를 전파한다")
        void restore_findPortFails_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            RuntimeException exception = new RuntimeException("find fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenThrow(exception);

            assertThatThrownBy(() -> restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 업데이트 중 예외 발생 시 이력 저장과 캐시 저장을 호출하지 않는다")
        void restore_updatePortFails_doesNotSaveHistoryAndCache() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(new RuntimeException("update fail")).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소"))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(saveInventoryRestorationHistoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        // ==================================================================================
        // 복구 이력 저장 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 복구 시 올바른 상품ID와 가격정책ID로 복구 이력을 저장한다")
        void restore_success_savesHistoryWithCorrectProductIdAndPricePolicyId() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            ArgumentCaptor<InventoryRestorationHistories> captor =
                    ArgumentCaptor.forClass(InventoryRestorationHistories.class);
            verify(saveInventoryRestorationHistoryPort).save(captor.capture());
            List<InventoryRestorationHistory> histories =
                    captor.getValue().getInventoryRestorationHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getProductId()).isEqualTo(1L);
            assertThat(histories.get(0).getPricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("재고 복구 시 올바른 복구 수량으로 복구 이력을 저장한다")
        void restore_success_savesHistoryWithCorrectRestorationQuantity() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            ArgumentCaptor<InventoryRestorationHistories> captor =
                    ArgumentCaptor.forClass(InventoryRestorationHistories.class);
            verify(saveInventoryRestorationHistoryPort).save(captor.capture());
            List<InventoryRestorationHistory> histories =
                    captor.getValue().getInventoryRestorationHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getStockValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("재고 복구 시 올바른 주문ID로 복구 이력을 저장한다")
        void restore_success_savesHistoryWithCorrectOrderId() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 99L, "주문 취소");

            ArgumentCaptor<InventoryRestorationHistories> captor =
                    ArgumentCaptor.forClass(InventoryRestorationHistories.class);
            verify(saveInventoryRestorationHistoryPort).save(captor.capture());
            List<InventoryRestorationHistory> histories =
                    captor.getValue().getInventoryRestorationHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getOrderId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("재고 복구 시 올바른 사유로 복구 이력을 저장한다")
        void restore_success_savesHistoryWithCorrectReason() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);
            String reason = "주문 전액 취소에 의한 재고 복구";

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, reason);

            ArgumentCaptor<InventoryRestorationHistories> captor =
                    ArgumentCaptor.forClass(InventoryRestorationHistories.class);
            verify(saveInventoryRestorationHistoryPort).save(captor.capture());
            List<InventoryRestorationHistory> histories =
                    captor.getValue().getInventoryRestorationHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("복수 가격 정책 재고 복구 시 각 가격 정책별 복구 이력을 저장한다")
        void restore_multipleProducts_savesHistoryForEachPolicy() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 8);
            Inventory inventory2 = buildInventory(2L, 200L, 15);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            ArgumentCaptor<InventoryRestorationHistories> captor =
                    ArgumentCaptor.forClass(InventoryRestorationHistories.class);
            verify(saveInventoryRestorationHistoryPort).save(captor.capture());
            List<InventoryRestorationHistory> histories =
                    captor.getValue().getInventoryRestorationHistories();

            assertThat(histories).hasSize(2);
            assertThat(histories).extracting(InventoryRestorationHistory::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);
            assertThat(histories).extracting(InventoryRestorationHistory::getStockValue)
                    .containsExactlyInAnyOrder(2, 5);
        }

        @Test
        @DisplayName("동일 가격 정책의 복수 주문 상품 복구 시 합산된 수량으로 이력을 저장한다")
        void restore_samePolicyMultipleProducts_savesHistoryWithSummedQuantity() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(100L, 3)
            );
            Inventory inventory = buildInventory(1L, 100L, 5);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            ArgumentCaptor<InventoryRestorationHistories> captor =
                    ArgumentCaptor.forClass(InventoryRestorationHistories.class);
            verify(saveInventoryRestorationHistoryPort).save(captor.capture());
            List<InventoryRestorationHistory> histories =
                    captor.getValue().getInventoryRestorationHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getStockValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("이력 저장 중 예외 발생 시 캐시 저장을 호출하지 않는다")
        void restore_saveHistoryPortFails_doesNotSaveCache() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(new RuntimeException("history save fail"))
                    .when(saveInventoryRestorationHistoryPort).save(any());

            assertThatThrownBy(() -> restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소"))
                    .isInstanceOf(RuntimeException.class);

            verify(updateInventoryPort).update(any());
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("이력 저장 중 예외 발생 시 예외를 전파한다")
        void restore_saveHistoryPortFails_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);
            RuntimeException exception = new RuntimeException("history save fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(exception).when(saveInventoryRestorationHistoryPort).save(any());

            assertThatThrownBy(() -> restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소"))
                    .isSameAs(exception);
        }
    }

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

    // ==================================================================================
    // 이벤트 발행 검증
    // ==================================================================================

    @Nested
    @DisplayName("이벤트 발행 검증")
    class EventPublishingTest {

        @Test
        @DisplayName("단일 주문 상품 재고 복구 시 복구된 재고 수량으로 UPDATED 이벤트를 발행한다")
        void restore_singleOrderProduct_publishesUpdatedEventWithRestoredStock() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 10, InventoryChangeAction.UPDATED
            );
        }

        @Test
        @DisplayName("서로 다른 가격 정책의 복수 주문 상품 재고 복구 시 각각 UPDATED 이벤트를 발행한다")
        void restore_differentPolicies_publishesUpdatedEventForEach() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 8);
            Inventory inventory2 = buildInventory(2L, 200L, 15);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 10, InventoryChangeAction.UPDATED
            );
            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    200L, 2L, 20, InventoryChangeAction.UPDATED
            );
        }

        @Test
        @DisplayName("분산 락이 작업을 실행하지 않으면 이벤트를 발행하지 않는다")
        void restore_lockNotExecuted_doesNotPublishEvent() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));

            restoreProductInventoryService.restore(orderProducts, 1L, "주문 취소");

            verifyNoInteractions(publishInventoryEventPort);
        }
    }
}
