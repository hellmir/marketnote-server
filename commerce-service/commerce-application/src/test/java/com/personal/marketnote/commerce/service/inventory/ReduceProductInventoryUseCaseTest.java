package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistories;
import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistory;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.exception.InventoryLockAcquisitionException;
import com.personal.marketnote.commerce.exception.InventoryLockInterruptedException;
import com.personal.marketnote.commerce.exception.InventoryNotFoundException;
import com.personal.marketnote.commerce.port.out.inventory.*;
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
class ReduceProductInventoryUseCaseTest {
    @Mock
    private FindInventoryPort findInventoryPort;
    @Mock
    private UpdateInventoryPort updateInventoryPort;
    @Mock
    private SaveInventoryDeductionHistoryPort saveInventoryDeductionHistoryPort;
    @Mock
    private SaveCacheStockPort saveCacheStockPort;
    @Mock
    private InventoryLockPort inventoryLockPort;

    @InjectMocks
    private ReduceProductInventoryService reduceProductInventoryService;

    // ==================================================================================
    // reduce (재고 차감)
    // ==================================================================================

    @Nested
    @DisplayName("reduce (재고 차감)")
    class ReduceTest {

        // ==================================================================================
        // 성공 케이스 (정상 동작)
        // ==================================================================================

        @Test
        @DisplayName("단일 주문 상품 재고 차감 시 재고가 올바르게 감소한다")
        void reduce_singleOrderProduct_reducesInventoryStock() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory.getStockValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("서로 다른 가격 정책의 복수 주문 상품 재고 차감 시 각각의 재고가 올바르게 감소한다")
        void reduce_differentPolicies_reducesEachInventory() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 10);
            Inventory inventory2 = buildInventory(2L, 200L, 20);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory1.getStockValue()).isEqualTo(8);
            assertThat(inventory2.getStockValue()).isEqualTo(15);
        }

        @Test
        @DisplayName("동일 가격 정책의 복수 주문 상품 재고 차감 시 수량을 합산하여 차감한다")
        void reduce_samePolicyMultipleProducts_sumsQuantityAndReduces() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(100L, 3)
            );
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory.getStockValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("동일/다른 가격 정책이 혼합된 주문 상품 재고 차감 시 올바르게 그룹화하여 차감한다")
        void reduce_mixedPolicies_groupsAndReducesCorrectly() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(100L, 3),
                    buildOrderProduct(200L, 4)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 20);
            Inventory inventory2 = buildInventory(2L, 200L, 15);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory1.getStockValue()).isEqualTo(15);
            assertThat(inventory2.getStockValue()).isEqualTo(11);
        }

        // ==================================================================================
        // 분산 락 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 차감 시 주문 상품의 가격 정책 ID Set을 분산 락에 전달한다")
        @SuppressWarnings("unchecked")
        void reduce_success_passesCorrectPricePolicyIdsToLock() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 3)
            );

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventory(1L, 100L, 10),
                    buildInventory(2L, 200L, 20)
            ));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Long>> lockKeysCaptor = ArgumentCaptor.forClass(Set.class);
            verify(inventoryLockPort).executeWithLock(lockKeysCaptor.capture(), any());
            assertThat(lockKeysCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("분산 락이 작업을 실행하지 않으면 내부 Port를 호출하지 않는다")
        void reduce_lockNotExecuted_doesNotCallInternalPorts() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            verify(inventoryLockPort).executeWithLock(any(), any());
            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryDeductionHistoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("분산 락 획득 실패 시 InventoryLockAcquisitionException을 전파한다")
        void reduce_lockAcquisitionFails_throwsInventoryLockAcquisitionException() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            InventoryLockAcquisitionException exception = new InventoryLockAcquisitionException(100L);

            doThrow(exception).when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("분산 락 획득 실패 시 내부 Port를 호출하지 않는다")
        void reduce_lockAcquisitionFails_doesNotCallInternalPorts() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));

            doThrow(new InventoryLockAcquisitionException(100L))
                    .when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isInstanceOf(InventoryLockAcquisitionException.class);

            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryDeductionHistoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("분산 락 처리 중 인터럽트 발생 시 InventoryLockInterruptedException을 전파한다")
        void reduce_lockInterrupted_throwsInventoryLockInterruptedException() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            InventoryLockInterruptedException exception =
                    new InventoryLockInterruptedException(new InterruptedException());

            doThrow(exception).when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isSameAs(exception);
        }

        // ==================================================================================
        // 호출 순서 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 차감 시 재고 조회 → 업데이트 → 이력 저장 → 캐시 저장 순서로 호출한다")
        @SuppressWarnings("unchecked")
        void reduce_success_callsPortsInCorrectOrder() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            InOrder inOrder = inOrder(findInventoryPort, updateInventoryPort,
                    saveInventoryDeductionHistoryPort, saveCacheStockPort);
            inOrder.verify(findInventoryPort).findByPricePolicyIds(any());
            inOrder.verify(updateInventoryPort).update(any());
            inOrder.verify(saveInventoryDeductionHistoryPort).save(any(InventoryDeductionHistories.class));
            inOrder.verify(saveCacheStockPort).save(any(Set.class));
            inOrder.verifyNoMoreInteractions();
        }

        // ==================================================================================
        // 재고 조회 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 차감 시 주문 상품의 가격 정책 ID Set으로 재고를 조회한다")
        @SuppressWarnings("unchecked")
        void reduce_success_findsInventoriesByCorrectPricePolicyIds() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 3),
                    buildOrderProduct(100L, 1)
            );

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventory(1L, 100L, 10),
                    buildInventory(2L, 200L, 20)
            ));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Long>> captor = ArgumentCaptor.forClass(Set.class);
            verify(findInventoryPort).findByPricePolicyIds(captor.capture());
            assertThat(captor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("동일 가격 정책의 복수 주문 상품 차감 시 중복 없는 가격 정책 ID Set으로 조회한다")
        @SuppressWarnings("unchecked")
        void reduce_samePolicyMultipleProducts_findsWithDeduplicatedIds() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(100L, 3)
            );

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventory(1L, 100L, 10)
            ));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Long>> captor = ArgumentCaptor.forClass(Set.class);
            verify(findInventoryPort).findByPricePolicyIds(captor.capture());
            assertThat(captor.getValue()).containsExactly(100L);
        }

        // ==================================================================================
        // 재고 업데이트 검증
        // ==================================================================================

        @Test
        @DisplayName("단일 상품 차감 후 감소된 재고를 업데이트 포트에 전달한다")
        @SuppressWarnings("unchecked")
        void reduce_singleProduct_passesReducedStockToUpdatePort() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            Set<Inventory> updated = captor.getValue();

            assertThat(updated).hasSize(1);
            Inventory updatedInventory = updated.iterator().next();
            assertThat(updatedInventory.getPricePolicyId()).isEqualTo(100L);
            assertThat(updatedInventory.getStockValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("복수 상품 차감 후 각각 감소된 재고를 업데이트 포트에 전달한다")
        @SuppressWarnings("unchecked")
        void reduce_multipleProducts_passesAllReducedStocksToUpdatePort() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 10);
            Inventory inventory2 = buildInventory(2L, 200L, 20);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            Set<Inventory> updated = captor.getValue();

            assertThat(updated).hasSize(2);
            assertThat(updated).extracting(Inventory::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);

            Inventory updated1 = updated.stream()
                    .filter(inv -> inv.getPricePolicyId().equals(100L))
                    .findFirst().orElseThrow();
            assertThat(updated1.getStockValue()).isEqualTo(8);

            Inventory updated2 = updated.stream()
                    .filter(inv -> inv.getPricePolicyId().equals(200L))
                    .findFirst().orElseThrow();
            assertThat(updated2.getStockValue()).isEqualTo(15);
        }

        // ==================================================================================
        // 이력 저장 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 차감 시 올바른 상품ID와 가격정책ID로 차감 이력을 저장한다")
        void reduce_success_savesHistoryWithCorrectProductIdAndPricePolicyId() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<InventoryDeductionHistories> captor =
                    ArgumentCaptor.forClass(InventoryDeductionHistories.class);
            verify(saveInventoryDeductionHistoryPort).save(captor.capture());
            List<InventoryDeductionHistory> histories =
                    captor.getValue().getInventoryDeductionHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getProductId()).isEqualTo(1L);
            assertThat(histories.get(0).getPricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("재고 차감 시 올바른 차감 수량으로 차감 이력을 저장한다")
        void reduce_success_savesHistoryWithCorrectDeductionQuantity() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<InventoryDeductionHistories> captor =
                    ArgumentCaptor.forClass(InventoryDeductionHistories.class);
            verify(saveInventoryDeductionHistoryPort).save(captor.capture());
            List<InventoryDeductionHistory> histories =
                    captor.getValue().getInventoryDeductionHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getStockValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("재고 차감 시 올바른 사유로 차감 이력을 저장한다")
        void reduce_success_savesHistoryWithCorrectReason() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);
            String reason = "결제 완료 - 주문번호 12345";

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, reason);

            ArgumentCaptor<InventoryDeductionHistories> captor =
                    ArgumentCaptor.forClass(InventoryDeductionHistories.class);
            verify(saveInventoryDeductionHistoryPort).save(captor.capture());
            List<InventoryDeductionHistory> histories =
                    captor.getValue().getInventoryDeductionHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("복수 가격 정책 재고 차감 시 각 가격 정책별 차감 이력을 저장한다")
        void reduce_multipleProducts_savesHistoryForEachPolicy() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 10);
            Inventory inventory2 = buildInventory(2L, 200L, 20);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<InventoryDeductionHistories> captor =
                    ArgumentCaptor.forClass(InventoryDeductionHistories.class);
            verify(saveInventoryDeductionHistoryPort).save(captor.capture());
            List<InventoryDeductionHistory> histories =
                    captor.getValue().getInventoryDeductionHistories();

            assertThat(histories).hasSize(2);
            assertThat(histories).extracting(InventoryDeductionHistory::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);
            assertThat(histories).extracting(InventoryDeductionHistory::getStockValue)
                    .containsExactlyInAnyOrder(2, 5);
        }

        @Test
        @DisplayName("동일 가격 정책의 복수 주문 상품 차감 시 합산된 수량으로 이력을 저장한다")
        void reduce_samePolicyMultipleProducts_savesHistoryWithSummedQuantity() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(100L, 3)
            );
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<InventoryDeductionHistories> captor =
                    ArgumentCaptor.forClass(InventoryDeductionHistories.class);
            verify(saveInventoryDeductionHistoryPort).save(captor.capture());
            List<InventoryDeductionHistory> histories =
                    captor.getValue().getInventoryDeductionHistories();

            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getStockValue()).isEqualTo(5);
        }

        // ==================================================================================
        // 캐시 저장 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 차감 후 감소된 재고를 캐시에 저장한다")
        @SuppressWarnings("unchecked")
        void reduce_success_savesReducedInventoriesToCache() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(saveCacheStockPort).save(captor.capture());
            Set<Inventory> cached = captor.getValue();

            assertThat(cached).hasSize(1);
            Inventory cachedInventory = cached.iterator().next();
            assertThat(cachedInventory.getPricePolicyId()).isEqualTo(100L);
            assertThat(cachedInventory.getStockValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("복수 재고 차감 후 모든 감소된 재고를 캐시에 저장한다")
        @SuppressWarnings("unchecked")
        void reduce_multipleProducts_savesAllReducedInventoriesToCache() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory1 = buildInventory(1L, 100L, 10);
            Inventory inventory2 = buildInventory(2L, 200L, 20);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory1, inventory2));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(saveCacheStockPort).save(captor.capture());
            Set<Inventory> cached = captor.getValue();

            assertThat(cached).hasSize(2);
            assertThat(cached).extracting(Inventory::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);
        }

        // ==================================================================================
        // 예외 전파 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 조회 중 예외 발생 시 예외를 전파한다")
        void reduce_findPortFails_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            RuntimeException exception = new RuntimeException("find fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenThrow(exception);

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 조회 중 예외 발생 시 후속 Port를 호출하지 않는다")
        void reduce_findPortFails_doesNotCallSubsequentPorts() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenThrow(new RuntimeException("find fail"));

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryDeductionHistoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("재고 업데이트 중 예외 발생 시 예외를 전파한다")
        void reduce_updatePortFails_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);
            RuntimeException exception = new RuntimeException("update fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(exception).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 업데이트 중 예외 발생 시 이력 저장과 캐시 저장을 호출하지 않는다")
        void reduce_updatePortFails_doesNotCallSubsequentPorts() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(new RuntimeException("update fail")).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(saveInventoryDeductionHistoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("재고 업데이트 중 InventoryNotFoundException 발생 시 예외를 전파한다")
        void reduce_updatePortThrowsInventoryNotFound_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);
            InventoryNotFoundException exception = new InventoryNotFoundException(100L);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(exception).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("이력 저장 중 예외 발생 시 예외를 전파한다")
        void reduce_saveHistoryPortFails_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);
            RuntimeException exception = new RuntimeException("history save fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(exception).when(saveInventoryDeductionHistoryPort).save(any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("이력 저장 중 예외 발생 시 캐시 저장을 호출하지 않는다")
        void reduce_saveHistoryPortFails_doesNotSaveCache() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(new RuntimeException("history save fail"))
                    .when(saveInventoryDeductionHistoryPort).save(any());

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isInstanceOf(RuntimeException.class);

            verify(updateInventoryPort).update(any());
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("캐시 저장 중 예외 발생 시 예외를 전파한다")
        @SuppressWarnings("unchecked")
        void reduce_saveCachePortFails_propagates() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);
            RuntimeException exception = new RuntimeException("cache save fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(exception).when(saveCacheStockPort).save(any(Set.class));

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("캐시 저장 중 예외 발생 시 재고 업데이트와 이력 저장은 이미 호출되었다")
        @SuppressWarnings("unchecked")
        void reduce_saveCachePortFails_previousPortsStillCalled() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 3));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(new RuntimeException("cache save fail"))
                    .when(saveCacheStockPort).save(any(Set.class));

            assertThatThrownBy(() -> reduceProductInventoryService.reduce(orderProducts, "결제 완료"))
                    .isInstanceOf(RuntimeException.class);

            verify(findInventoryPort).findByPricePolicyIds(any());
            verify(updateInventoryPort).update(any());
            verify(saveInventoryDeductionHistoryPort).save(any(InventoryDeductionHistories.class));
        }

        // ==================================================================================
        // 엣지 케이스
        // ==================================================================================

        @Test
        @DisplayName("빈 주문 상품 목록으로 호출 시 빈 키셋으로 분산 락을 실행한다")
        @SuppressWarnings("unchecked")
        void reduce_emptyOrderProducts_executesLockWithEmptyKeys() {
            List<OrderProduct> orderProducts = List.of();

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of());

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Long>> lockKeysCaptor = ArgumentCaptor.forClass(Set.class);
            verify(inventoryLockPort).executeWithLock(lockKeysCaptor.capture(), any());
            assertThat(lockKeysCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("빈 주문 상품 목록으로 호출 시 빈 재고로 업데이트와 이력과 캐시를 저장한다")
        @SuppressWarnings("unchecked")
        void reduce_emptyOrderProducts_callsAllPortsWithEmptyData() {
            List<OrderProduct> orderProducts = List.of();

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of());

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            ArgumentCaptor<Set<Inventory>> updateCaptor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(updateCaptor.capture());
            assertThat(updateCaptor.getValue()).isEmpty();

            ArgumentCaptor<InventoryDeductionHistories> historyCaptor =
                    ArgumentCaptor.forClass(InventoryDeductionHistories.class);
            verify(saveInventoryDeductionHistoryPort).save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getInventoryDeductionHistories()).isEmpty();

            ArgumentCaptor<Set<Inventory>> cacheCaptor = ArgumentCaptor.forClass(Set.class);
            verify(saveCacheStockPort).save(cacheCaptor.capture());
            assertThat(cacheCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("수량 1인 단일 주문 상품 재고 차감 시 재고를 1만큼 감소시킨다")
        void reduce_quantityOne_reducesStockByOne() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 1));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory.getStockValue()).isEqualTo(9);
        }

        @Test
        @DisplayName("전체 재고만큼 차감 시 재고가 0이 된다")
        void reduce_entireStock_reducesToZero() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 10));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory.getStockValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("대량 수량 차감 시 올바르게 차감된다")
        void reduce_largeQuantity_reducesCorrectly() {
            List<OrderProduct> orderProducts = List.of(buildOrderProduct(100L, 9999));
            Inventory inventory = buildInventory(1L, 100L, 10000);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory.getStockValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("일부 가격 정책의 재고만 조회된 경우 조회된 재고만 차감하고 이력은 모든 가격 정책에 대해 저장한다")
        void reduce_partialInventoryMatch_reducesOnlyFoundAndSavesHistoryForAll() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(200L, 5)
            );
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory.getStockValue()).isEqualTo(8);

            ArgumentCaptor<InventoryDeductionHistories> captor =
                    ArgumentCaptor.forClass(InventoryDeductionHistories.class);
            verify(saveInventoryDeductionHistoryPort).save(captor.capture());
            List<InventoryDeductionHistory> histories =
                    captor.getValue().getInventoryDeductionHistories();

            assertThat(histories).hasSize(2);
            assertThat(histories).extracting(InventoryDeductionHistory::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);

            InventoryDeductionHistory missingHistory = histories.stream()
                    .filter(h -> h.getPricePolicyId().equals(200L))
                    .findFirst().orElseThrow();
            assertThat(missingHistory.getProductId()).isNull();
        }

        @Test
        @DisplayName("다수의 동일 가격 정책 주문 상품 차감 시 모든 수량이 합산되어 차감된다")
        void reduce_manyProductsSamePolicy_sumsAllQuantities() {
            List<OrderProduct> orderProducts = List.of(
                    buildOrderProduct(100L, 1),
                    buildOrderProduct(100L, 2),
                    buildOrderProduct(100L, 3),
                    buildOrderProduct(100L, 4)
            );
            Inventory inventory = buildInventory(1L, 100L, 50);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reduceProductInventoryService.reduce(orderProducts, "결제 완료");

            assertThat(inventory.getStockValue()).isEqualTo(40);
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
}
