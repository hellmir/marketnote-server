package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.InsufficientAvailableStockException;
import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.exception.DuplicateInventoryReservationException;
import com.personal.marketnote.commerce.exception.InventoryLockAcquisitionException;
import com.personal.marketnote.commerce.exception.InventoryNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.ReserveInventoryCommand;
import com.personal.marketnote.commerce.port.out.inventory.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReserveInventoryUseCase 테스트")
class ReserveInventoryUseCaseTest {
    @Mock
    private FindInventoryPort findInventoryPort;
    @Mock
    private UpdateInventoryPort updateInventoryPort;
    @Mock
    private SaveInventoryReservationPort saveInventoryReservationPort;
    @Mock
    private SaveCacheStockPort saveCacheStockPort;
    @Mock
    private InventoryLockPort inventoryLockPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-04-05T10:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @InjectMocks
    private ReserveInventoryService reserveInventoryService;

    // ==================================================================================
    // 성공 케이스 (정상 동작)
    // ==================================================================================

    @Nested
    @DisplayName("재고 예약 성공")
    class ReserveSuccessTest {

        @Test
        @DisplayName("단일 주문 상품 재고 예약 시 reserved가 올바르게 증가한다")
        void reserveInventory_singleProduct_increasesReserved() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            reserveInventoryService.reserveInventory(command);

            assertThat(inventory.getReserved()).isEqualTo(3);
            assertThat(inventory.availableStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("서로 다른 가격 정책의 복수 주문 상품 재고 예약 시 각각의 reserved가 올바르게 증가한다")
        void reserveInventory_differentPolicies_reservesEach() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 2),
                    buildOrderProductItem(200L, 5)
            ));
            Inventory inventory1 = buildInventory(1L, 100L, 10);
            Inventory inventory2 = buildInventory(2L, 200L, 20);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(Set.of(inventory1, inventory2));

            reserveInventoryService.reserveInventory(command);

            assertThat(inventory1.getReserved()).isEqualTo(2);
            assertThat(inventory2.getReserved()).isEqualTo(5);
        }

        @Test
        @DisplayName("동일 가격 정책의 복수 주문 상품 예약 시 수량을 합산하여 예약한다")
        void reserveInventory_samePolicy_sumsQuantity() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 2),
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            reserveInventoryService.reserveInventory(command);

            assertThat(inventory.getReserved()).isEqualTo(5);
            assertThat(inventory.availableStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("전체 가용재고만큼 예약 시 가용재고가 0이 된다")
        void reserveInventory_entireAvailableStock_availableStockBecomesZero() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 10)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            reserveInventoryService.reserveInventory(command);

            assertThat(inventory.getReserved()).isEqualTo(10);
            assertThat(inventory.availableStock()).isEqualTo(0);
        }
    }

    // ==================================================================================
    // 분산 락 검증
    // ==================================================================================

    @Nested
    @DisplayName("분산 락 검증")
    class LockTest {

        @Test
        @DisplayName("재고 예약 시 주문 상품의 가격 정책 ID Set을 분산 락에 전달한다")
        @SuppressWarnings("unchecked")
        void reserveInventory_passesCorrectPricePolicyIdsToLock() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 2),
                    buildOrderProductItem(200L, 3)
            ));

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(
                    buildInventory(1L, 100L, 10),
                    buildInventory(2L, 200L, 20)
            ));

            reserveInventoryService.reserveInventory(command);

            ArgumentCaptor<Set<Long>> lockKeysCaptor = ArgumentCaptor.forClass(Set.class);
            verify(inventoryLockPort).executeWithLock(lockKeysCaptor.capture(), any());
            assertThat(lockKeysCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("분산 락이 작업을 실행하지 않으면 내부 Port를 호출하지 않는다")
        void reserveInventory_lockNotExecuted_doesNotCallInternalPorts() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));

            reserveInventoryService.reserveInventory(command);

            verify(inventoryLockPort).executeWithLock(any(), any());
            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryReservationPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("분산 락 획득 실패 시 InventoryLockAcquisitionException을 전파한다")
        void reserveInventory_lockAcquisitionFails_throwsException() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            InventoryLockAcquisitionException exception = new InventoryLockAcquisitionException(100L);

            doThrow(exception).when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("분산 락 획득 실패 시 내부 Port를 호출하지 않는다")
        void reserveInventory_lockAcquisitionFails_doesNotCallInternalPorts() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));

            doThrow(new InventoryLockAcquisitionException(100L))
                    .when(inventoryLockPort).executeWithLock(any(), any());

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isInstanceOf(InventoryLockAcquisitionException.class);

            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryReservationPort);
            verifyNoInteractions(saveCacheStockPort);
        }
    }

    // ==================================================================================
    // 재고 업데이트 검증
    // ==================================================================================

    @Nested
    @DisplayName("재고 업데이트 검증")
    class UpdateInventoryTest {

        @Test
        @DisplayName("재고 예약 후 변경된 reserved 값을 업데이트 포트에 전달한다")
        @SuppressWarnings("unchecked")
        void reserveInventory_passesUpdatedInventoriesToUpdatePort() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            reserveInventoryService.reserveInventory(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(updateInventoryPort).update(captor.capture());
            Set<Inventory> updated = captor.getValue();

            assertThat(updated).hasSize(1);
            Inventory updatedInventory = updated.iterator().next();
            assertThat(updatedInventory.getReserved()).isEqualTo(3);
            assertThat(updatedInventory.getStockValue()).isEqualTo(10);
        }
    }

    // ==================================================================================
    // 예약 레코드 저장 검증
    // ==================================================================================

    @Nested
    @DisplayName("예약 레코드 저장 검증")
    class SaveReservationTest {

        @Test
        @DisplayName("재고 예약 시 각 가격 정책별 InventoryReservation을 저장한다")
        void reserveInventory_savesReservationForEachPolicy() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 2),
                    buildOrderProductItem(200L, 5)
            ));

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(Set.of(
                            buildInventory(1L, 100L, 10),
                            buildInventory(2L, 200L, 20)
                    ));

            reserveInventoryService.reserveInventory(command);

            ArgumentCaptor<List<InventoryReservation>> captor = ArgumentCaptor.forClass(List.class);
            verify(saveInventoryReservationPort).save(captor.capture());
            List<InventoryReservation> reservations = captor.getValue();

            assertThat(reservations).hasSize(2);
            assertThat(reservations).extracting(InventoryReservation::getPricePolicyId)
                    .containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        @DisplayName("재고 예약 시 올바른 orderId, pricePolicyId, quantity로 InventoryReservation을 생성한다")
        void reserveInventory_createsReservationWithCorrectFields() {
            ReserveInventoryCommand command = buildCommand(99L, List.of(
                    buildOrderProductItem(100L, 3)
            ));

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L)))
                    .thenReturn(Set.of(buildInventory(1L, 100L, 10)));

            reserveInventoryService.reserveInventory(command);

            ArgumentCaptor<List<InventoryReservation>> captor = ArgumentCaptor.forClass(List.class);
            verify(saveInventoryReservationPort).save(captor.capture());
            List<InventoryReservation> reservations = captor.getValue();

            assertThat(reservations).hasSize(1);
            InventoryReservation reservation = reservations.get(0);
            assertThat(reservation.getOrderId()).isEqualTo(99L);
            assertThat(reservation.getPricePolicyId()).isEqualTo(100L);
            assertThat(reservation.getQuantity()).isEqualTo(3);
            assertThat(reservation.getReservedAt()).isNotNull();
        }

        @Test
        @DisplayName("동일 가격 정책의 복수 주문 상품 예약 시 합산된 수량으로 단일 InventoryReservation을 생성한다")
        void reserveInventory_samePolicyMultipleItems_createsSingleReservationWithSummedQuantity() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 2),
                    buildOrderProductItem(100L, 3)
            ));

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L)))
                    .thenReturn(Set.of(buildInventory(1L, 100L, 10)));

            reserveInventoryService.reserveInventory(command);

            ArgumentCaptor<List<InventoryReservation>> captor = ArgumentCaptor.forClass(List.class);
            verify(saveInventoryReservationPort).save(captor.capture());
            List<InventoryReservation> reservations = captor.getValue();

            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getQuantity()).isEqualTo(5);
        }
    }

    // ==================================================================================
    // 캐시 저장 검증
    // ==================================================================================

    @Nested
    @DisplayName("캐시 저장 검증")
    class CacheSaveTest {

        @Test
        @DisplayName("재고 예약 후 변경된 재고를 캐시에 저장한다")
        @SuppressWarnings("unchecked")
        void reserveInventory_savesUpdatedInventoriesToCache() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            reserveInventoryService.reserveInventory(command);

            ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
            verify(saveCacheStockPort).save(captor.capture());
            Set<Inventory> cached = captor.getValue();

            assertThat(cached).hasSize(1);
            Inventory cachedInventory = cached.iterator().next();
            assertThat(cachedInventory.getReserved()).isEqualTo(3);
        }
    }

    // ==================================================================================
    // 예외 검증
    // ==================================================================================

    @Nested
    @DisplayName("예외 검증")
    class ExceptionTest {

        @Test
        @DisplayName("요청된 pricePolicyId에 해당하는 재고가 DB에 없으면 InventoryNotFoundException이 발생한다")
        void reserveInventory_inventoryNotFound_throwsException() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3),
                    buildOrderProductItem(200L, 2)
            ));

            stubLockToExecuteTask();
            // 100L만 조회됨, 200L은 DB에 없음
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(Set.of(buildInventory(1L, 100L, 10)));

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isInstanceOf(InventoryNotFoundException.class);
        }

        @Test
        @DisplayName("재고가 DB에 없으면 후속 Port를 호출하지 않는다")
        void reserveInventory_inventoryNotFound_doesNotCallSubsequentPorts() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3),
                    buildOrderProductItem(200L, 2)
            ));

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(Set.of(buildInventory(1L, 100L, 10)));

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isInstanceOf(InventoryNotFoundException.class);

            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryReservationPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("가용재고 부족 시 InsufficientAvailableStockException을 전파한다")
        void reserveInventory_insufficientAvailableStock_throwsException() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 11)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isInstanceOf(InsufficientAvailableStockException.class);
        }

        @Test
        @DisplayName("가용재고 부족 시 후속 Port를 호출하지 않는다")
        void reserveInventory_insufficientAvailableStock_doesNotCallSubsequentPorts() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 11)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isInstanceOf(InsufficientAvailableStockException.class);

            verifyNoInteractions(updateInventoryPort);
            verifyNoInteractions(saveInventoryReservationPort);
            verifyNoInteractions(saveCacheStockPort);
        }

        @Test
        @DisplayName("이미 예약된 재고가 있을 때 추가 예약으로 가용재고를 초과하면 예외가 발생한다")
        void reserveInventory_existingReserved_exceedsAvailableStock_throwsException() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 6)
            ));
            // stock=10, reserved=5 → available=5, request=6 → 부족
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 5);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isInstanceOf(InsufficientAvailableStockException.class);
        }

        @Test
        @DisplayName("재고 조회 중 예외 발생 시 예외를 전파한다")
        void reserveInventory_findPortFails_propagates() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            RuntimeException exception = new RuntimeException("find fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenThrow(exception);

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 업데이트 중 예외 발생 시 예외를 전파한다")
        void reserveInventory_updatePortFails_propagates() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);
            RuntimeException exception = new RuntimeException("update fail");

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(exception).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 업데이트 중 예외 발생 시 예약 레코드 저장과 캐시 저장을 호출하지 않는다")
        void reserveInventory_updatePortFails_doesNotCallSubsequentPorts() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(new RuntimeException("update fail")).when(updateInventoryPort).update(any());

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(saveInventoryReservationPort);
            verifyNoInteractions(saveCacheStockPort);
        }
    }

    // ==================================================================================
    // 멱등성 검증
    // ==================================================================================

    @Nested
    @DisplayName("멱등성 검증")
    class IdempotencyTest {

        @Test
        @DisplayName("동일 orderId/pricePolicyId로 중복 예약 시 DuplicateInventoryReservationException이 발생한다")
        void reserveInventory_duplicateReservation_throwsException() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);
            DuplicateInventoryReservationException exception = new DuplicateInventoryReservationException(1L);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));
            doThrow(exception).when(saveInventoryReservationPort).save(any());

            assertThatThrownBy(() -> reserveInventoryService.reserveInventory(command))
                    .isSameAs(exception);
        }
    }

    // ==================================================================================
    // 호출 순서 검증
    // ==================================================================================

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrderTest {

        @Test
        @DisplayName("락 내부에서 조회 → 예약(reserve) → 업데이트 → 예약레코드 저장 → 캐시 저장 순서로 호출된다")
        void reserveInventory_callsPortsInCorrectOrder() {
            ReserveInventoryCommand command = buildCommand(1L, List.of(
                    buildOrderProductItem(100L, 3)
            ));
            Inventory inventory = buildInventory(1L, 100L, 10);

            stubLockToExecuteTask();
            when(findInventoryPort.findByPricePolicyIds(any())).thenReturn(Set.of(inventory));

            reserveInventoryService.reserveInventory(command);

            org.mockito.InOrder inOrder = inOrder(
                    findInventoryPort, updateInventoryPort, saveInventoryReservationPort, saveCacheStockPort
            );
            inOrder.verify(findInventoryPort).findByPricePolicyIds(any());
            inOrder.verify(updateInventoryPort).update(any());
            inOrder.verify(saveInventoryReservationPort).save(any());
            inOrder.verify(saveCacheStockPort).save(any(Set.class));
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

    private ReserveInventoryCommand buildCommand(Long orderId,
                                                 List<ReserveInventoryCommand.OrderProductItem> orderProducts) {
        return ReserveInventoryCommand.builder()
                .orderId(orderId)
                .orderProducts(orderProducts)
                .build();
    }

    private ReserveInventoryCommand.OrderProductItem buildOrderProductItem(Long pricePolicyId, int quantity) {
        return ReserveInventoryCommand.OrderProductItem.builder()
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .build();
    }

    private Inventory buildInventory(Long productId, Long pricePolicyId, int stock) {
        return Inventory.of(productId, pricePolicyId, stock, 0L);
    }

    private Inventory buildInventoryWithReserved(Long productId, Long pricePolicyId, int stock, int reserved) {
        return Inventory.from(
                com.personal.marketnote.commerce.domain.inventory.InventorySnapshotState.builder()
                        .productId(productId)
                        .pricePolicyId(pricePolicyId)
                        .stock(stock)
                        .version(0L)
                        .reserved(reserved)
                        .build()
        );
    }
}
