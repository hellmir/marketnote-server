package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveInventoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

            var inOrder = inOrder(findInventoryPort, saveInventoryPort, saveCacheStockPort);
            inOrder.verify(findInventoryPort).existsByPricePolicyId(pricePolicyId);
            inOrder.verify(saveInventoryPort).save(any(Inventory.class));
            inOrder.verify(saveCacheStockPort).save(pricePolicyId, 0);
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("productId가 null인 커맨드로 재고 등록 시 productId가 null인 재고를 저장한다")
        void registerInventory_nullProductId_savesWithNullProductId() {
            Long pricePolicyId = 400L;
            RegisterInventoryCommand command = RegisterInventoryCommand.of(pricePolicyId);

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
}
