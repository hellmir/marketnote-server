package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveInventoryPort;
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

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterInventoryUseCaseTest {
    @Mock
    private SaveInventoryPort saveInventoryPort;
    @Mock
    private SaveCacheStockPort saveCacheStockPort;
    @Mock
    private FindInventoryPort findInventoryPort;
    @Mock
    private PublishInventoryEventPort publishInventoryEventPort;

    @InjectMocks
    private RegisterInventoryService registerInventoryService;

    // ==================================================================================
    // registerInventory (단건 등록)
    // ==================================================================================

    @Nested
    @DisplayName("registerInventory (단건 등록)")
    class RegisterInventoryTest {

        @Test
        @DisplayName("재고 등록 시 productId와 pricePolicyId를 갖는 재고를 저장한다")
        void registerInventory_success_savesInventory() {
            Long productId = 1L;
            Long pricePolicyId = 100L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);

            registerInventoryService.registerInventory(command);

            ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
            verify(saveInventoryPort).save(captor.capture());
            Inventory saved = captor.getValue();

            assertThat(saved.getProductId()).isEqualTo(productId);
            assertThat(saved.getPricePolicyId()).isEqualTo(pricePolicyId);
            assertThat(saved.getStockValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("재고 등록 시 캐시에 해당 가격 정책의 재고를 0으로 저장한다")
        void registerInventory_success_savesCacheWithZeroStock() {
            Long productId = 2L;
            Long pricePolicyId = 200L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);

            registerInventoryService.registerInventory(command);

            verify(saveCacheStockPort).save(pricePolicyId, 0);
        }

        @Test
        @DisplayName("재고 등록 시 중복 여부 확인 -> 재고 저장 -> 캐시 저장 순서로 호출한다")
        void registerInventory_success_callsPortsInOrder() {
            Long productId = 3L;
            Long pricePolicyId = 300L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);

            registerInventoryService.registerInventory(command);

            InOrder inOrder = inOrder(findInventoryPort, saveInventoryPort, saveCacheStockPort);
            inOrder.verify(findInventoryPort).existsByPricePolicyId(pricePolicyId);
            inOrder.verify(saveInventoryPort).save(any(Inventory.class));
            inOrder.verify(saveCacheStockPort).save(pricePolicyId, 0);
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("productId가 null인 커맨드로 재고 등록 시 productId가 null인 재고를 저장한다")
        void registerInventory_nullProductId_savesWithNullProductId() {
            Long pricePolicyId = 400L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(null, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);

            registerInventoryService.registerInventory(command);

            ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
            verify(saveInventoryPort).save(captor.capture());
            Inventory saved = captor.getValue();

            assertThat(saved.getProductId()).isNull();
            assertThat(saved.getPricePolicyId()).isEqualTo(pricePolicyId);
            assertThat(saved.getStockValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("이미 존재하는 가격 정책 ID로 재고 등록 시 InventoryAlreadyExistsException을 던진다")
        void registerInventory_alreadyExists_throwsInventoryAlreadyExistsException() {
            Long productId = 5L;
            Long pricePolicyId = 500L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(true);

            assertThatThrownBy(() -> registerInventoryService.registerInventory(command))
                    .isInstanceOf(InventoryAlreadyExistsException.class)
                    .hasMessageContaining("500");
        }

        @Test
        @DisplayName("이미 존재하는 재고로 등록 실패 시 재고 저장과 캐시 저장을 호출하지 않는다")
        void registerInventory_alreadyExists_doesNotSave() {
            Long productId = 6L;
            Long pricePolicyId = 600L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(true);

            assertThatThrownBy(() -> registerInventoryService.registerInventory(command))
                    .isInstanceOf(InventoryAlreadyExistsException.class);

            verify(saveInventoryPort, never()).save(any(Inventory.class));
            verify(saveCacheStockPort, never()).save(anyLong(), anyInt());
        }

        @Test
        @DisplayName("재고 존재 여부 확인 중 예외 발생 시 예외를 전파한다")
        void registerInventory_findPortFails_propagates() {
            Long productId = 7L;
            Long pricePolicyId = 700L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);
            RuntimeException exception = new RuntimeException("find fail");

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenThrow(exception);

            assertThatThrownBy(() -> registerInventoryService.registerInventory(command))
                    .isSameAs(exception);

            verifyNoInteractions(saveInventoryPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("재고 저장 중 예외 발생 시 예외를 전파한다")
        void registerInventory_saveInventoryFails_propagates() {
            Long productId = 8L;
            Long pricePolicyId = 800L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);
            RuntimeException exception = new RuntimeException("save fail");

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);
            doThrow(exception).when(saveInventoryPort).save(any(Inventory.class));

            assertThatThrownBy(() -> registerInventoryService.registerInventory(command))
                    .isSameAs(exception);

            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("캐시 저장 중 예외 발생 시 예외를 전파한다")
        void registerInventory_saveCacheFails_propagates() {
            Long productId = 9L;
            Long pricePolicyId = 900L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);
            RuntimeException exception = new RuntimeException("cache fail");

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);
            doThrow(exception).when(saveCacheStockPort).save(pricePolicyId, 0);

            assertThatThrownBy(() -> registerInventoryService.registerInventory(command))
                    .isSameAs(exception);

            verify(saveInventoryPort).save(any(Inventory.class));
        }
    }

    // ==================================================================================
    // registerInventories (다건 등록)
    // ==================================================================================

    @Nested
    @DisplayName("registerInventories (다건 등록)")
    class RegisterInventoriesTest {

        @Test
        @DisplayName("복수 재고 등록 시 커맨드에 해당하는 재고를 모두 저장한다")
        @SuppressWarnings("unchecked")
        void registerInventories_success_savesAllInventories() {
            Set<RegisterInventoryCommand> commands = new LinkedHashSet<>();
            commands.add(RegisterInventoryCommand.of(1L, 100L));
            commands.add(RegisterInventoryCommand.of(2L, 200L));
            commands.add(RegisterInventoryCommand.of(3L, 300L));

            Set<Inventory> result = registerInventoryService.registerInventories(commands);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(saveInventoryPort).save(captor.capture());
            Set<Inventory> saved = captor.getValue();

            assertThat(saved).hasSize(3);
            assertThat(saved).extracting(Inventory::getProductId).containsExactlyInAnyOrder(1L, 2L, 3L);
            assertThat(saved).extracting(Inventory::getPricePolicyId).containsExactlyInAnyOrder(100L, 200L, 300L);
            assertThat(saved).allMatch(inv -> inv.getStockValue() == 0);
        }

        @Test
        @DisplayName("복수 재고 등록 시 캐시에 재고를 저장한다")
        @SuppressWarnings("unchecked")
        void registerInventories_success_savesCacheForAll() {
            Set<RegisterInventoryCommand> commands = new LinkedHashSet<>();
            commands.add(RegisterInventoryCommand.of(10L, 1000L));
            commands.add(RegisterInventoryCommand.of(20L, 2000L));

            registerInventoryService.registerInventories(commands);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(saveCacheStockPort).save(captor.capture());
            Set<Inventory> cached = captor.getValue();

            assertThat(cached).hasSize(2);
            assertThat(cached).extracting(Inventory::getPricePolicyId).containsExactlyInAnyOrder(1000L, 2000L);
        }

        @Test
        @DisplayName("복수 재고 등록 시 생성된 재고 목록을 반환한다")
        void registerInventories_success_returnsCreatedInventories() {
            Set<RegisterInventoryCommand> commands = new LinkedHashSet<>();
            commands.add(RegisterInventoryCommand.of(11L, 1100L));
            commands.add(RegisterInventoryCommand.of(12L, 1200L));

            Set<Inventory> result = registerInventoryService.registerInventories(commands);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Inventory::getProductId).containsExactlyInAnyOrder(11L, 12L);
            assertThat(result).extracting(Inventory::getPricePolicyId).containsExactlyInAnyOrder(1100L, 1200L);
            assertThat(result).allMatch(inv -> inv.getStockValue() == 0);
        }

        @Test
        @DisplayName("복수 재고 등록 시 재고 저장 -> 캐시 저장 순서로 호출한다")
        @SuppressWarnings("unchecked")
        void registerInventories_success_callsPortsInOrder() {
            Set<RegisterInventoryCommand> commands = Set.of(
                    RegisterInventoryCommand.of(13L, 1300L)
            );

            registerInventoryService.registerInventories(commands);

            InOrder inOrder = inOrder(saveInventoryPort, saveCacheStockPort);
            inOrder.verify(saveInventoryPort).save(any(Set.class));
            inOrder.verify(saveCacheStockPort).save(any(Set.class));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("복수 재고 등록 시 중복 확인 없이 바로 저장한다")
        @SuppressWarnings("unchecked")
        void registerInventories_doesNotCheckDuplicates() {
            Set<RegisterInventoryCommand> commands = Set.of(
                    RegisterInventoryCommand.of(14L, 1400L)
            );

            registerInventoryService.registerInventories(commands);

            verifyNoInteractions(findInventoryPort);
            verify(saveInventoryPort).save(any(Set.class));
        }

        @Test
        @DisplayName("하나의 커맨드로 복수 재고 등록 시 단일 재고를 저장하고 반환한다")
        void registerInventories_singleCommand_savesSingleInventory() {
            Set<RegisterInventoryCommand> commands = Set.of(
                    RegisterInventoryCommand.of(15L, 1500L)
            );

            Set<Inventory> result = registerInventoryService.registerInventories(commands);

            assertThat(result).hasSize(1);

            Inventory inventory = result.iterator().next();
            assertThat(inventory.getProductId()).isEqualTo(15L);
            assertThat(inventory.getPricePolicyId()).isEqualTo(1500L);
            assertThat(inventory.getStockValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("빈 커맨드 목록으로 복수 재고 등록 시 빈 목록을 저장하고 반환한다")
        @SuppressWarnings("unchecked")
        void registerInventories_emptyCommands_savesEmptyAndReturnsEmpty() {
            Set<RegisterInventoryCommand> commands = Set.of();

            Set<Inventory> result = registerInventoryService.registerInventories(commands);

            assertThat(result).isEmpty();

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(saveInventoryPort).save(captor.capture());
            assertThat(captor.getValue()).isEmpty();

            verify(saveCacheStockPort).save(captor.capture());
            assertThat(captor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("productId가 null인 커맨드를 포함하여 복수 재고 등록 시 null productId 재고도 저장한다")
        @SuppressWarnings("unchecked")
        void registerInventories_nullProductId_savesWithNullProductId() {
            Set<RegisterInventoryCommand> commands = new LinkedHashSet<>();
            commands.add(RegisterInventoryCommand.of(null, 1600L));
            commands.add(RegisterInventoryCommand.of(17L, 1700L));

            Set<Inventory> result = registerInventoryService.registerInventories(commands);

            assertThat(result).hasSize(2);

            Inventory nullProductIdInventory = result.stream()
                    .filter(inv -> inv.getPricePolicyId().equals(1600L))
                    .findFirst()
                    .orElseThrow();
            assertThat(nullProductIdInventory.getProductId()).isNull();
            assertThat(nullProductIdInventory.getStockValue()).isEqualTo(0);

            Inventory withProductIdInventory = result.stream()
                    .filter(inv -> inv.getPricePolicyId().equals(1700L))
                    .findFirst()
                    .orElseThrow();
            assertThat(withProductIdInventory.getProductId()).isEqualTo(17L);
            assertThat(withProductIdInventory.getStockValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("복수 재고 저장 중 예외 발생 시 예외를 전파한다")
        @SuppressWarnings("unchecked")
        void registerInventories_saveInventoryFails_propagates() {
            Set<RegisterInventoryCommand> commands = Set.of(
                    RegisterInventoryCommand.of(18L, 1800L)
            );
            RuntimeException exception = new RuntimeException("batch save fail");

            doThrow(exception).when(saveInventoryPort).save(any(Set.class));

            assertThatThrownBy(() -> registerInventoryService.registerInventories(commands))
                    .isSameAs(exception);

            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("복수 재고 캐시 저장 중 예외 발생 시 예외를 전파한다")
        @SuppressWarnings("unchecked")
        void registerInventories_saveCacheFails_propagates() {
            Set<RegisterInventoryCommand> commands = Set.of(
                    RegisterInventoryCommand.of(19L, 1900L)
            );
            RuntimeException exception = new RuntimeException("batch cache fail");

            doThrow(exception).when(saveCacheStockPort).save(any(Set.class));

            assertThatThrownBy(() -> registerInventoryService.registerInventories(commands))
                    .isSameAs(exception);

            verify(saveInventoryPort).save(any(Set.class));
        }

        @Test
        @DisplayName("복수 재고 등록 시 각 재고마다 CREATED 이벤트를 발행한다")
        void registerInventories_success_publishesCreatedEventForEach() {
            Set<RegisterInventoryCommand> commands = new LinkedHashSet<>();
            commands.add(RegisterInventoryCommand.of(1L, 100L));
            commands.add(RegisterInventoryCommand.of(2L, 200L));

            registerInventoryService.registerInventories(commands);

            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 0, InventoryChangeAction.CREATED
            );
            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    200L, 2L, 0, InventoryChangeAction.CREATED
            );
            verify(publishInventoryEventPort, times(2)).publishInventoryChangedEvent(
                    any(), any(), any(), any()
            );
        }
    }

    // ==================================================================================
    // 이벤트 발행 검증
    // ==================================================================================

    @Nested
    @DisplayName("이벤트 발행 검증")
    class EventPublishingTest {

        @Test
        @DisplayName("단건 재고 등록 시 CREATED 이벤트를 발행한다")
        void registerInventory_success_publishesCreatedEvent() {
            Long productId = 20L;
            Long pricePolicyId = 2000L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);

            registerInventoryService.registerInventory(command);

            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    pricePolicyId, productId, 0, InventoryChangeAction.CREATED
            );
        }

        @Test
        @DisplayName("단건 재고 등록 시 올바른 재고 수량 0으로 이벤트를 발행한다")
        void registerInventory_success_publishesEventWithZeroStock() {
            Long productId = 21L;
            Long pricePolicyId = 2100L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);

            registerInventoryService.registerInventory(command);

            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    2100L, 21L, 0, InventoryChangeAction.CREATED
            );
        }

        @Test
        @DisplayName("이미 존재하는 재고로 등록 실패 시 이벤트를 발행하지 않는다")
        void registerInventory_alreadyExists_doesNotPublishEvent() {
            Long productId = 22L;
            Long pricePolicyId = 2200L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(productId, pricePolicyId);

            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(true);

            assertThatThrownBy(() -> registerInventoryService.registerInventory(command))
                    .isInstanceOf(InventoryAlreadyExistsException.class);

            verifyNoInteractions(publishInventoryEventPort);
        }
    }
}
