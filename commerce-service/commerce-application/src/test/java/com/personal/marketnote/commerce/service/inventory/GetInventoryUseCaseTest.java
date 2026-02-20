package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetInventoryUseCaseTest {
    @Mock
    private RegisterInventoryUseCase registerInventoryUseCase;
    @Mock
    private FindInventoryPort findInventoryPort;

    @InjectMocks
    private GetInventoryService getInventoryService;

    // ==================================================================================
    // getInventories (재고 목록 조회)
    // ==================================================================================

    @Nested
    @DisplayName("getInventories (재고 목록 조회)")
    class GetInventoriesTest {

        @Test
        @DisplayName("모든 재고가 존재하면 조회 결과를 그대로 반환한다")
        void getInventories_allExist_returnsInventories() {
            List<Long> pricePolicyIds = List.of(100L, 200L);
            Inventory inv1 = Inventory.of(1L, 100L, 10);
            Inventory inv2 = Inventory.of(2L, 200L, 20);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(new HashSet<>(Set.of(inv1, inv2)));

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Inventory::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);
            assertThat(result).extracting(Inventory::getStockValue)
                    .containsExactlyInAnyOrder(10, 20);
        }

        @Test
        @DisplayName("모든 재고가 존재하면 재고 등록을 호출하지 않는다")
        void getInventories_allExist_doesNotRegister() {
            List<Long> pricePolicyIds = List.of(100L);
            Inventory inv = Inventory.of(1L, 100L, 5);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L)))
                    .thenReturn(new HashSet<>(Set.of(inv)));

            getInventoryService.getInventories(pricePolicyIds);

            verifyNoInteractions(registerInventoryUseCase);
        }

        @Test
        @DisplayName("재고가 하나도 없으면 전체 가격 정책 ID로 재고 등록을 요청한다")
        @SuppressWarnings("unchecked")
        void getInventories_noneExist_registersAll() {
            List<Long> pricePolicyIds = List.of(100L, 200L, 300L);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L, 300L)))
                    .thenReturn(new HashSet<>());

            Inventory created1 = Inventory.of(null, 100L);
            Inventory created2 = Inventory.of(null, 200L);
            Inventory created3 = Inventory.of(null, 300L);
            when(registerInventoryUseCase.registerInventories(anySet()))
                    .thenReturn(Set.of(created1, created2, created3));

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            ArgumentCaptor<Set<RegisterInventoryCommand>> captor = ArgumentCaptor.forClass(Set.class);
            verify(registerInventoryUseCase).registerInventories(captor.capture());
            Set<RegisterInventoryCommand> commands = captor.getValue();

            assertThat(commands).hasSize(3);
            assertThat(commands).extracting(RegisterInventoryCommand::pricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L, 300L);
            assertThat(commands).allMatch(cmd -> cmd.productId() == null);
        }

        @Test
        @DisplayName("재고가 하나도 없으면 등록된 재고를 합쳐서 반환한다")
        @SuppressWarnings("unchecked")
        void getInventories_noneExist_returnsCombinedResult() {
            List<Long> pricePolicyIds = List.of(100L, 200L);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(new HashSet<>());

            Inventory created1 = Inventory.of(null, 100L);
            Inventory created2 = Inventory.of(null, 200L);
            when(registerInventoryUseCase.registerInventories(anySet()))
                    .thenReturn(Set.of(created1, created2));

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Inventory::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);
            assertThat(result).allMatch(inv -> inv.getStockValue() == 0);
        }

        @Test
        @DisplayName("일부 재고만 존재하면 재고 등록을 호출한다")
        void getInventories_partialExist_callsRegister() {
            List<Long> pricePolicyIds = List.of(100L, 200L);
            Inventory existing = Inventory.of(1L, 100L, 5);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(new HashSet<>(Set.of(existing)));
            when(registerInventoryUseCase.registerInventories(anySet()))
                    .thenReturn(Set.of());

            getInventoryService.getInventories(pricePolicyIds);

            verify(registerInventoryUseCase).registerInventories(anySet());
        }

        @Test
        @DisplayName("빈 가격 정책 ID 목록 입력 시 빈 결과를 반환한다")
        void getInventories_emptyInput_returnsEmpty() {
            List<Long> pricePolicyIds = List.of();

            when(findInventoryPort.findByPricePolicyIds(Set.of()))
                    .thenReturn(new HashSet<>());

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            assertThat(result).isEmpty();
            verifyNoInteractions(registerInventoryUseCase);
        }

        @Test
        @DisplayName("단일 가격 정책 ID로 조회 시 존재하면 해당 재고를 반환한다")
        void getInventories_singleExisting_returnsInventory() {
            List<Long> pricePolicyIds = List.of(100L);
            Inventory inv = Inventory.of(1L, 100L, 15);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L)))
                    .thenReturn(new HashSet<>(Set.of(inv)));

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            assertThat(result).hasSize(1);
            Inventory returned = result.iterator().next();
            assertThat(returned.getProductId()).isEqualTo(1L);
            assertThat(returned.getPricePolicyId()).isEqualTo(100L);
            assertThat(returned.getStockValue()).isEqualTo(15);
        }

        @Test
        @DisplayName("단일 가격 정책 ID로 조회 시 미존재하면 등록 후 반환한다")
        @SuppressWarnings("unchecked")
        void getInventories_singleMissing_registersAndReturns() {
            List<Long> pricePolicyIds = List.of(100L);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L)))
                    .thenReturn(new HashSet<>());

            Inventory created = Inventory.of(null, 100L);
            when(registerInventoryUseCase.registerInventories(anySet()))
                    .thenReturn(Set.of(created));

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            assertThat(result).hasSize(1);
            assertThat(result.iterator().next().getPricePolicyId()).isEqualTo(100L);

            ArgumentCaptor<Set<RegisterInventoryCommand>> captor = ArgumentCaptor.forClass(Set.class);
            verify(registerInventoryUseCase).registerInventories(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().iterator().next().pricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("pricePolicyIds를 HashSet으로 변환하여 FindInventoryPort에 전달한다")
        void getInventories_convertsToHashSet() {
            List<Long> pricePolicyIds = List.of(100L, 200L);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(new HashSet<>(Set.of(Inventory.of(1L, 100L), Inventory.of(2L, 200L))));

            getInventoryService.getInventories(pricePolicyIds);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<Long>> captor = ArgumentCaptor.forClass(Set.class);
            verify(findInventoryPort).findByPricePolicyIds(captor.capture());

            assertThat(captor.getValue()).isInstanceOf(HashSet.class);
            assertThat(captor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("중복 가격 정책 ID 입력 시 HashSet 변환으로 중복이 제거되어 전달한다")
        void getInventories_duplicateIds_deduplicatedByHashSet() {
            List<Long> pricePolicyIds = List.of(100L, 100L, 200L);
            Inventory inv1 = Inventory.of(1L, 100L, 5);
            Inventory inv2 = Inventory.of(2L, 200L, 10);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(new HashSet<>(Set.of(inv1, inv2)));

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<Long>> captor = ArgumentCaptor.forClass(Set.class);
            verify(findInventoryPort).findByPricePolicyIds(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
        }

        @Test
        @DisplayName("조회된 재고의 stock 값이 그대로 반환된다")
        void getInventories_preservesStockValues() {
            List<Long> pricePolicyIds = List.of(100L, 200L, 300L);
            Inventory inv1 = Inventory.of(1L, 100L, 0);
            Inventory inv2 = Inventory.of(2L, 200L, 50);
            Inventory inv3 = Inventory.of(3L, 300L, 999);

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L, 300L)))
                    .thenReturn(new HashSet<>(Set.of(inv1, inv2, inv3)));

            Set<Inventory> result = getInventoryService.getInventories(pricePolicyIds);

            assertThat(result).extracting(Inventory::getStockValue)
                    .containsExactlyInAnyOrder(0, 50, 999);
        }

        @Test
        @DisplayName("재고 조회 중 예외 발생 시 예외를 전파한다")
        void getInventories_findPortFails_propagates() {
            List<Long> pricePolicyIds = List.of(100L);
            RuntimeException exception = new RuntimeException("find fail");

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenThrow(exception);

            assertThatThrownBy(() -> getInventoryService.getInventories(pricePolicyIds))
                    .isSameAs(exception);

            verifyNoInteractions(registerInventoryUseCase);
        }

        @Test
        @DisplayName("재고 등록 중 예외 발생 시 예외를 전파한다")
        void getInventories_registerFails_propagates() {
            List<Long> pricePolicyIds = List.of(100L);
            RuntimeException exception = new RuntimeException("register fail");

            when(findInventoryPort.findByPricePolicyIds(Set.of(100L)))
                    .thenReturn(new HashSet<>());
            when(registerInventoryUseCase.registerInventories(anySet()))
                    .thenThrow(exception);

            assertThatThrownBy(() -> getInventoryService.getInventories(pricePolicyIds))
                    .isSameAs(exception);
        }
    }

    // ==================================================================================
    // existsInventory (재고 존재 여부 조회)
    // ==================================================================================

    @Nested
    @DisplayName("existsInventory (재고 존재 여부 조회)")
    class ExistsInventoryTest {

        @Test
        @DisplayName("재고가 존재하면 true를 반환한다")
        void existsInventory_exists_returnsTrue() {
            when(findInventoryPort.existsByPricePolicyId(100L)).thenReturn(true);

            boolean result = getInventoryService.existsInventory(100L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("재고가 존재하지 않으면 false를 반환한다")
        void existsInventory_notExists_returnsFalse() {
            when(findInventoryPort.existsByPricePolicyId(200L)).thenReturn(false);

            boolean result = getInventoryService.existsInventory(200L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("재고 존재 여부 확인 중 예외 발생 시 예외를 전파한다")
        void existsInventory_portFails_propagates() {
            RuntimeException exception = new RuntimeException("exists fail");

            when(findInventoryPort.existsByPricePolicyId(300L)).thenThrow(exception);

            assertThatThrownBy(() -> getInventoryService.existsInventory(300L))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("FindInventoryPort.existsByPricePolicyId를 정확히 한 번 호출한다")
        void existsInventory_callsFindPortExactlyOnce() {
            when(findInventoryPort.existsByPricePolicyId(100L)).thenReturn(true);

            getInventoryService.existsInventory(100L);

            verify(findInventoryPort, times(1)).existsByPricePolicyId(100L);
            verifyNoMoreInteractions(findInventoryPort);
        }

        @Test
        @DisplayName("입력된 pricePolicyId가 Port에 정확히 전달된다")
        void existsInventory_passesExactPricePolicyIdToPort() {
            Long pricePolicyId = 999L;
            when(findInventoryPort.existsByPricePolicyId(pricePolicyId)).thenReturn(false);

            getInventoryService.existsInventory(pricePolicyId);

            ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
            verify(findInventoryPort).existsByPricePolicyId(captor.capture());
            assertThat(captor.getValue()).isEqualTo(999L);
        }

        @Test
        @DisplayName("재고 존재 여부 조회 시 RegisterInventoryUseCase와 상호작용하지 않는다")
        void existsInventory_doesNotInteractWithRegisterUseCase() {
            when(findInventoryPort.existsByPricePolicyId(100L)).thenReturn(true);

            getInventoryService.existsInventory(100L);

            verifyNoInteractions(registerInventoryUseCase);
        }

        @Test
        @DisplayName("서로 다른 pricePolicyId로 호출하면 각각 정확한 ID로 Port를 호출한다")
        void existsInventory_withDifferentIds_delegatesEachCorrectly() {
            when(findInventoryPort.existsByPricePolicyId(100L)).thenReturn(true);
            when(findInventoryPort.existsByPricePolicyId(200L)).thenReturn(false);
            when(findInventoryPort.existsByPricePolicyId(300L)).thenReturn(true);

            assertThat(getInventoryService.existsInventory(100L)).isTrue();
            assertThat(getInventoryService.existsInventory(200L)).isFalse();
            assertThat(getInventoryService.existsInventory(300L)).isTrue();

            verify(findInventoryPort).existsByPricePolicyId(100L);
            verify(findInventoryPort).existsByPricePolicyId(200L);
            verify(findInventoryPort).existsByPricePolicyId(300L);
        }

        @Test
        @DisplayName("pricePolicyId가 1인 경우 정상적으로 위임한다")
        void existsInventory_withMinId_delegatesCorrectly() {
            when(findInventoryPort.existsByPricePolicyId(1L)).thenReturn(true);

            boolean result = getInventoryService.existsInventory(1L);

            assertThat(result).isTrue();
            verify(findInventoryPort).existsByPricePolicyId(1L);
        }

        @Test
        @DisplayName("pricePolicyId가 큰 값인 경우 정상적으로 위임한다")
        void existsInventory_withLargeId_delegatesCorrectly() {
            when(findInventoryPort.existsByPricePolicyId(Long.MAX_VALUE)).thenReturn(false);

            boolean result = getInventoryService.existsInventory(Long.MAX_VALUE);

            assertThat(result).isFalse();
            verify(findInventoryPort).existsByPricePolicyId(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("Port 예외 발생 시 RegisterInventoryUseCase와 상호작용하지 않는다")
        void existsInventory_portFails_doesNotInteractWithRegisterUseCase() {
            when(findInventoryPort.existsByPricePolicyId(100L))
                    .thenThrow(new RuntimeException("port error"));

            assertThatThrownBy(() -> getInventoryService.existsInventory(100L))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(registerInventoryUseCase);
        }

        @Test
        @DisplayName("Port가 false를 반환하면 반환값을 변환 없이 그대로 반환한다")
        void existsInventory_portReturnsFalse_returnsExactFalse() {
            when(findInventoryPort.existsByPricePolicyId(500L)).thenReturn(false);

            boolean result = getInventoryService.existsInventory(500L);

            assertThat(result).isFalse();
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("Port가 true를 반환하면 반환값을 변환 없이 그대로 반환한다")
        void existsInventory_portReturnsTrue_returnsExactTrue() {
            when(findInventoryPort.existsByPricePolicyId(600L)).thenReturn(true);

            boolean result = getInventoryService.existsInventory(600L);

            assertThat(result).isTrue();
            assertThat(result).isEqualTo(true);
        }
    }
}
