package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventorySnapshotState;
import com.personal.marketnote.commerce.exception.InventoryLockAcquisitionException;
import com.personal.marketnote.commerce.exception.InventoryLockInterruptedException;
import com.personal.marketnote.commerce.exception.InventoryProductNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryItemCommand;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.InventoryLockPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import com.personal.marketnote.commerce.port.out.inventory.UpdateInventoryPort;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncFulfillmentVendorInventoryUseCaseTest {
    @Mock
    private FindInventoryPort findInventoryPort;
    @Mock
    private UpdateInventoryPort updateInventoryPort;
    @Mock
    private SaveCacheStockPort saveCacheStockPort;
    @Mock
    private InventoryLockPort inventoryLockPort;
    @Mock
    private PublishInventoryEventPort publishInventoryEventPort;

    @InjectMocks
    private SyncFulfillmentVendorInventoryService syncFulfillmentVendorInventoryService;

    // ==================================================================================
    // syncInventories (풀필먼트 벤더 재고 동기화, productKey 전용)
    // ==================================================================================

    @Nested
    @DisplayName("syncInventories (풀필먼트 벤더 재고 동기화)")
    class SyncInventoriesTest {

        // ==================================================================================
        // 입력 검증
        // ==================================================================================

        @Test
        @DisplayName("null 커맨드 입력 시 IllegalArgumentException을 던진다")
        void syncInventories_nullCommand_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sync fulfillment vendor inventory command is required.");
        }

        @Test
        @DisplayName("inventories가 null인 커맨드 입력 시 IllegalArgumentException을 던진다")
        void syncInventories_nullInventories_throwsIllegalArgumentException() {
            SyncFulfillmentVendorInventoryCommand command = SyncFulfillmentVendorInventoryCommand.of(null);

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sync fulfillment vendor inventory command is required.");
        }

        @Test
        @DisplayName("null 커맨드 입력 시 어떤 Port도 호출하지 않는다")
        void syncInventories_nullCommand_doesNotCallAnyPort() {
            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
            verifyNoInteractions(inventoryLockPort);
        }

        @Test
        @DisplayName("항목 중 productKey가 null이면 InvalidFulfillmentSyncCommandException을 던진다")
        void syncInventories_nullProductKey_throwsInvalidFulfillmentSyncCommandException() {
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(null, 50)
            );

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(com.personal.marketnote.commerce.exception.InvalidFulfillmentSyncCommandException.class);

            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(inventoryLockPort);
        }

        @Test
        @DisplayName("inventories 크기가 1000을 초과하면 InvalidFulfillmentSyncCommandException을 던지고 어떤 Port도 호출하지 않는다")
        void syncInventories_exceedsSizeLimit_throwsInvalidFulfillmentSyncCommandException() {
            java.util.List<SyncFulfillmentVendorInventoryItemCommand> items = new java.util.ArrayList<>();
            for (int i = 0; i < 1001; i++) {
                items.add(SyncFulfillmentVendorInventoryItemCommand.of(UUID.randomUUID(), 1));
            }
            SyncFulfillmentVendorInventoryCommand command = SyncFulfillmentVendorInventoryCommand.of(items);

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(com.personal.marketnote.commerce.exception.InvalidFulfillmentSyncCommandException.class);

            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
            verifyNoInteractions(inventoryLockPort);
        }

        // ==================================================================================
        // 성공 케이스 (정상 동작)
        // ==================================================================================

        @Test
        @DisplayName("단일 상품 재고 동기화 시 새 재고 값으로 업데이트 포트에 전달한다")
        @SuppressWarnings("unchecked")
        void syncInventories_singleProduct_updatesWithNewStockValue() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 5L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            Set<Inventory> updated = captor.getValue();

            assertThat(updated).hasSize(1);
            Inventory updatedInventory = updated.iterator().next();
            assertThat(updatedInventory.getProductId()).isEqualTo(1L);
            assertThat(updatedInventory.getStockValue()).isEqualTo(50);
        }

        @Test
        @DisplayName("복수 상품 재고 동기화 시 각 상품의 새 재고 값을 업데이트 포트에 전달한다")
        @SuppressWarnings("unchecked")
        void syncInventories_multipleProducts_updatesEachWithNewStockValue() {
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey1, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey2, 30)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 0L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 1L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 2L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            Set<Inventory> updated = captor.getValue();

            assertThat(updated).hasSize(2);
            assertThat(updated).extracting(Inventory::getProductId)
                    .containsExactlyInAnyOrder(1L, 2L);

            Inventory updated1 = updated.stream()
                    .filter(inv -> inv.getProductId().equals(1L))
                    .findFirst().orElseThrow();
            assertThat(updated1.getStockValue()).isEqualTo(50);

            Inventory updated2 = updated.stream()
                    .filter(inv -> inv.getProductId().equals(2L))
                    .findFirst().orElseThrow();
            assertThat(updated2.getStockValue()).isEqualTo(30);
        }

        @Test
        @DisplayName("재고 0으로 동기화 시 0으로 업데이트 포트에 전달한다")
        @SuppressWarnings("unchecked")
        void syncInventories_zeroStock_updatesWithZero() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 0)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            assertThat(captor.getValue().iterator().next().getStockValue()).isEqualTo(0);
        }

        // ==================================================================================
        // 상품 조회 검증
        // ==================================================================================

        @Test
        @DisplayName("커맨드의 productKey Set으로 findByProductKeys를 정확히 한 번 호출한다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_callsFindByProductKeysWithCorrectKeys() {
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey1, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey2, 30)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 0L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 1L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 2L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<UUID>> captor = ArgumentCaptor.forClass(Set.class);
            verify(findInventoryPort, times(1)).findByProductKeys(captor.capture());
            assertThat(captor.getValue()).containsExactlyInAnyOrder(productKey1, productKey2);
            verify(findInventoryPort, never()).findByProductIds(any());
        }

        // ==================================================================================
        // 상품 존재 검증
        // ==================================================================================

        @Test
        @DisplayName("요청한 productKey 중 일부가 존재하지 않으면 InventoryProductNotFoundException을 던진다")
        void syncInventories_partialProductKeyMissing_throwsInventoryProductNotFoundException() {
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey1, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey2, 30)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 0L)
            ));

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(InventoryProductNotFoundException.class);
        }

        @Test
        @DisplayName("요청한 productKey가 전부 존재하지 않으면 InventoryProductNotFoundException을 던진다")
        void syncInventories_allProductKeysMissing_throwsInventoryProductNotFoundException() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(InventoryProductNotFoundException.class);
        }

        @Test
        @DisplayName("상품 미존재 시 분산 락과 후속 Port를 호출하지 않는다")
        void syncInventories_productMissing_doesNotCallLockAndSubsequentPorts() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(InventoryProductNotFoundException.class);

            verifyNoInteractions(inventoryLockPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        // ==================================================================================
        // 분산 락 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 동기화 시 조회된 재고의 가격 정책 ID Set을 분산 락에 전달한다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_passesCorrectPricePolicyIdsToLock() {
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey1, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey2, 30)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 0L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 1L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 2L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Long>> lockKeysCaptor = ArgumentCaptor.forClass(Set.class);
            verify(inventoryLockPort).executeWithLock(lockKeysCaptor.capture(), any());
            assertThat(lockKeysCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("분산 락이 작업을 실행하지 않으면 락 내부 Port를 호출하지 않는다")
        void syncInventories_lockNotExecuted_doesNotCallInternalPorts() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            verify(inventoryLockPort).executeWithLock(any(), any());
            verify(findInventoryPort, never()).findByPricePolicyIds(any());
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("분산 락 획득 실패 시 InventoryLockAcquisitionException을 전파한다")
        void syncInventories_lockAcquisitionFails_throwsInventoryLockAcquisitionException() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            InventoryLockAcquisitionException exception = new InventoryLockAcquisitionException(100L);
            doThrow(exception).when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("분산 락 획득 실패 시 락 내부 Port를 호출하지 않는다")
        void syncInventories_lockAcquisitionFails_doesNotCallInternalPorts() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            doThrow(new InventoryLockAcquisitionException(100L))
                    .when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(InventoryLockAcquisitionException.class);

            verify(findInventoryPort, never()).findByPricePolicyIds(any());
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("분산 락 처리 중 인터럽트 발생 시 InventoryLockInterruptedException을 전파한다")
        void syncInventories_lockInterrupted_throwsInventoryLockInterruptedException() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            InventoryLockInterruptedException exception =
                    new InventoryLockInterruptedException(new InterruptedException());
            doThrow(exception).when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isSameAs(exception);
        }

        // ==================================================================================
        // 호출 순서 검증
        // ==================================================================================

        @Test
        @DisplayName("재고 동기화 시 productKey 조회 → 락 내 재고 재조회 → 업데이트 → 캐시 저장 순서로 호출한다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_callsPortsInCorrectOrder() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            InOrder inOrder = inOrder(findInventoryPort, updateInventoryPort, saveCacheStockPort);
            inOrder.verify(findInventoryPort).findByProductKeys(any());
            inOrder.verify(findInventoryPort).findByPricePolicyIds(any());
            inOrder.verify(updateInventoryPort).update(any());
            inOrder.verify(saveCacheStockPort).save(any(Set.class));
            inOrder.verifyNoMoreInteractions();
        }

        // ==================================================================================
        // 락 내부 재조회 검증
        // ==================================================================================

        @Test
        @DisplayName("락 내부에서 가격 정책 ID Set으로 재고를 재조회한다")
        @SuppressWarnings("unchecked")
        void syncInventories_insideLock_findsByPricePolicyIds() {
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey1, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey2, 30)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 0L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 1L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 2L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Long>> captor = ArgumentCaptor.forClass(Set.class);
            verify(findInventoryPort).findByPricePolicyIds(captor.capture());
            assertThat(captor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("락 내부 재조회 재고에 동기화 대상이 아닌 상품이 포함되면 업데이트에서 제외된다")
        @SuppressWarnings("unchecked")
        void syncInventories_nonTargetProductInLockedInventories_filteredOutFromUpdate() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L),
                    buildInventoryWithProductKey(3L, UUID.randomUUID(), 100L, 15, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            Set<Inventory> updated = captor.getValue();

            assertThat(updated).hasSize(1);
            assertThat(updated.iterator().next().getProductId()).isEqualTo(1L);
        }

        // ==================================================================================
        // 업데이트 값 검증
        // ==================================================================================

        @Test
        @DisplayName("업데이트되는 Inventory에 올바른 productId와 pricePolicyId가 설정된다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_updatedInventoryHasCorrectProductAndPricePolicyIds() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 5L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            Inventory updated = captor.getValue().iterator().next();

            assertThat(updated.getProductId()).isEqualTo(1L);
            assertThat(updated.getPricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("업데이트되는 Inventory에 커맨드의 재고 수량이 설정된다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_updatedInventoryHasCommandStockValue() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 77)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            assertThat(captor.getValue().iterator().next().getStockValue()).isEqualTo(77);
        }

        @Test
        @DisplayName("업데이트되는 Inventory에 락 내부 재조회된 재고의 version이 보존된다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_updatedInventoryPreservesLockedVersion() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 42L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            assertThat(captor.getValue().iterator().next().getVersion()).isEqualTo(42L);
        }

        // ==================================================================================
        // 캐시 저장 검증
        // ==================================================================================

        @Test
        @DisplayName("동기화된 재고를 캐시에 저장한다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_savesUpdatedInventoriesToCache() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(saveCacheStockPort).save(captor.capture());
            Set<Inventory> cached = captor.getValue();

            assertThat(cached).hasSize(1);
            Inventory cachedInventory = cached.iterator().next();
            assertThat(cachedInventory.getProductId()).isEqualTo(1L);
            assertThat(cachedInventory.getStockValue()).isEqualTo(50);
        }

        @Test
        @DisplayName("업데이트 포트와 캐시 저장 포트에 동일한 Inventory Set을 전달한다")
        @SuppressWarnings("unchecked")
        void syncInventories_success_sameInventorySetPassedToUpdateAndCache() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> updateCaptor = ArgumentCaptor.forClass(Set.class);
            ArgumentCaptor<Set<Inventory>> cacheCaptor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(updateCaptor.capture());
            verify(saveCacheStockPort).save(cacheCaptor.capture());

            assertThat(updateCaptor.getValue()).isSameAs(cacheCaptor.getValue());
        }

        // ==================================================================================
        // 예외 전파 검증
        // ==================================================================================

        @Test
        @DisplayName("findByProductKeys 중 예외 발생 시 예외를 전파한다")
        void syncInventories_findByProductKeysFails_propagatesException() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            RuntimeException exception = new RuntimeException("find by product keys fail");
            when(findInventoryPort.findByProductKeys(any())).thenThrow(exception);

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("findByProductKeys 중 예외 발생 시 후속 Port를 호출하지 않는다")
        void syncInventories_findByProductKeysFails_doesNotCallSubsequentPorts() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenThrow(new RuntimeException("fail"));

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(inventoryLockPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("findByPricePolicyIds 중 예외 발생 시 예외를 전파한다")
        void syncInventories_findByPricePolicyIdsFails_propagatesException() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            RuntimeException exception = new RuntimeException("find by price policy ids fail");
            when(findInventoryPort.findByPricePolicyIds(any())).thenThrow(exception);

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("findByPricePolicyIds 중 예외 발생 시 후속 Port를 호출하지 않는다")
        void syncInventories_findByPricePolicyIdsFails_doesNotCallSubsequentPorts() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenThrow(new RuntimeException("fail"));

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("업데이트 중 예외 발생 시 예외를 전파한다")
        void syncInventories_updateFails_propagatesException() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));
            RuntimeException exception = new RuntimeException("update fail");
            doThrow(exception).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("업데이트 중 예외 발생 시 캐시 저장을 호출하지 않는다")
        void syncInventories_updateFails_doesNotSaveCache() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));
            doThrow(new RuntimeException("update fail")).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("캐시 저장 중 예외 발생 시 예외를 전파한다")
        @SuppressWarnings("unchecked")
        void syncInventories_saveCacheFails_propagatesException() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));
            RuntimeException exception = new RuntimeException("cache save fail");
            doThrow(exception).when(saveCacheStockPort).save(any(Set.class));

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("캐시 저장 중 예외 발생 시 업데이트는 이미 호출되었다")
        @SuppressWarnings("unchecked")
        void syncInventories_saveCacheFails_updateAlreadyCalled() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));
            doThrow(new RuntimeException("cache save fail"))
                    .when(saveCacheStockPort).save(any(Set.class));

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(RuntimeException.class);

            verify(findInventoryPort).findByProductKeys(any());
            verify(findInventoryPort).findByPricePolicyIds(any());
            verify(updateInventoryPort).update(any());
        }

        // ==================================================================================
        // 엣지 케이스
        // ==================================================================================

        @Test
        @DisplayName("복수 상품이 동일 가격 정책을 가지면 중복 없는 가격 정책 ID Set으로 락을 요청한다")
        @SuppressWarnings("unchecked")
        void syncInventories_samePricePolicyMultipleProducts_deduplicatesPricePolicyIds() {
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey1, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey2, 30)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 0L),
                    buildInventoryWithProductKey(2L, productKey2, 100L, 20, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 1L),
                    buildInventoryWithProductKey(2L, productKey2, 100L, 20, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Long>> lockKeysCaptor = ArgumentCaptor.forClass(Set.class);
            verify(inventoryLockPort).executeWithLock(lockKeysCaptor.capture(), any());
            assertThat(lockKeysCaptor.getValue()).containsExactly(100L);
        }

        @Test
        @DisplayName("커맨드에 중복 productKey가 있으면 마지막 재고 값이 사용된다")
        @SuppressWarnings("unchecked")
        void syncInventories_duplicateProductKey_lastStockValueWins() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 99)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            assertThat(captor.getValue().iterator().next().getStockValue()).isEqualTo(99);
        }

        @Test
        @DisplayName("대량 재고 값으로 동기화 시 올바르게 업데이트된다")
        @SuppressWarnings("unchecked")
        void syncInventories_largeStockValue_updatesCorrectly() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 999999)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            assertThat(captor.getValue().iterator().next().getStockValue()).isEqualTo(999999);
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

    private SyncFulfillmentVendorInventoryCommand buildCommand(
            SyncFulfillmentVendorInventoryItemCommand... items
    ) {
        return SyncFulfillmentVendorInventoryCommand.of(List.of(items));
    }

    private Inventory buildInventoryWithProductKey(
            Long productId, UUID productKey, Long pricePolicyId, int stock, Long version
    ) {
        return Inventory.from(InventorySnapshotState.builder()
                .productId(productId)
                .productKey(productKey)
                .pricePolicyId(pricePolicyId)
                .stock(stock)
                .version(version)
                .reserved(0)
                .build());
    }

    // ==================================================================================
    // 이벤트 발행 검증
    // ==================================================================================

    @Nested
    @DisplayName("이벤트 발행 검증")
    class EventPublishingTest {

        @Test
        @DisplayName("단일 상품 재고 동기화 시 동기화된 재고 수량으로 UPDATED 이벤트를 발행한다")
        void syncInventories_singleProduct_publishesUpdatedEventWithSyncedStock() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 1L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 50, InventoryChangeAction.UPDATED
            );
        }

        @Test
        @DisplayName("복수 상품 재고 동기화 시 각 상품에 대해 UPDATED 이벤트를 발행한다")
        void syncInventories_multipleProducts_publishesUpdatedEventForEach() {
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey1, 50),
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey2, 30)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 0L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 0L)
            ));
            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey1, 100L, 10, 1L),
                    buildInventoryWithProductKey(2L, productKey2, 200L, 20, 2L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 50, InventoryChangeAction.UPDATED
            );
            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    200L, 2L, 30, InventoryChangeAction.UPDATED
            );
        }

        @Test
        @DisplayName("분산 락이 작업을 실행하지 않으면 이벤트를 발행하지 않는다")
        void syncInventories_lockNotExecuted_doesNotPublishEvent() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of(
                    buildInventoryWithProductKey(1L, productKey, 100L, 10, 0L)
            ));

            syncFulfillmentVendorInventoryService.syncInventories(command);

            verifyNoInteractions(publishInventoryEventPort);
        }

        @Test
        @DisplayName("상품 미존재 시 이벤트를 발행하지 않는다")
        void syncInventories_productMissing_doesNotPublishEvent() {
            UUID productKey = UUID.randomUUID();
            SyncFulfillmentVendorInventoryCommand command = buildCommand(
                    SyncFulfillmentVendorInventoryItemCommand.of(productKey, 50)
            );
            when(findInventoryPort.findByProductKeys(any())).thenReturn(Set.of());

            assertThatThrownBy(() -> syncFulfillmentVendorInventoryService.syncInventories(command))
                    .isInstanceOf(InventoryProductNotFoundException.class);

            verifyNoInteractions(publishInventoryEventPort);
        }
    }
}
