package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.InventoryLockPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import com.personal.marketnote.commerce.port.out.inventory.UpdateInventoryPort;
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
    private SaveCacheStockPort saveCacheStockPort;
    @Mock
    private InventoryLockPort inventoryLockPort;

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

            restoreProductInventoryService.restore(orderProducts, "주문 취소");

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

            restoreProductInventoryService.restore(orderProducts, "주문 취소");

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

            restoreProductInventoryService.restore(orderProducts, "주문 취소");

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

            restoreProductInventoryService.restore(orderProducts, "주문 취소");

            ArgumentCaptor<Set<Long>> lockKeysCaptor = ArgumentCaptor.forClass(Set.class);
            verify(inventoryLockPort).executeWithLock(lockKeysCaptor.capture(), any());
            assertThat(lockKeysCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("재고 복구 시 재고 조회 → 업데이트 → 캐시 저장 순서로 호출한다")
        @SuppressWarnings("unchecked")
        void restore_success_callsPortsInCorrectOrder() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            restoreProductInventoryService.restore(orderProducts, "주문 취소");

            InOrder inOrder = inOrder(findInventoryPort, updateInventoryPort, saveCacheStockPort);
            inOrder.verify(findInventoryPort).findByPricePolicyIds(any());
            inOrder.verify(updateInventoryPort).update(any());
            inOrder.verify(saveCacheStockPort).save(any(Set.class));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("분산 락이 작업을 실행하지 않으면 내부 Port를 호출하지 않는다")
        void restore_lockNotExecuted_doesNotCallInternalPorts() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));

            restoreProductInventoryService.restore(orderProducts, "주문 취소");

            verify(inventoryLockPort).executeWithLock(any(), any());
            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("재고 조회 중 예외 발생 시 예외를 전파한다")
        void restore_findPortFails_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            RuntimeException exception = new RuntimeException("find fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenThrow(exception);

            assertThatThrownBy(() -> restoreProductInventoryService.restore(orderProducts, "주문 취소"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 업데이트 중 예외 발생 시 캐시 저장을 호출하지 않는다")
        void restore_updatePortFails_doesNotSaveCache() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 7);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(new RuntimeException("update fail")).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> restoreProductInventoryService.restore(orderProducts, "주문 취소"))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(saveCacheStockPort);
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
}
