package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.*;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.commerce.port.out.inventory.*;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpireInventoryReservationUseCaseTest {
    @Mock
    private FindExpiredInventoryReservationPort findExpiredInventoryReservationPort;
    @Mock
    private FindInventoryReservationPort findInventoryReservationPort;
    @Mock
    private FindInventoryPort findInventoryPort;
    @Mock
    private UpdateInventoryPort updateInventoryPort;
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
    @Mock
    private PlatformTransactionManager transactionManager;

    private ExpireInventoryReservationService expireInventoryReservationService;

    private static final LocalDateTime CUTOFF = LocalDateTime.of(2026, 4, 2, 8, 50, 0);

    @BeforeEach
    void setUp() {
        expireInventoryReservationService = new ExpireInventoryReservationService(
                findExpiredInventoryReservationPort,
                findInventoryReservationPort,
                findInventoryPort,
                updateInventoryPort,
                deleteInventoryReservationPort,
                saveInventoryRestorationHistoryPort,
                saveCacheStockPort,
                inventoryLockPort,
                publishInventoryEventPort,
                transactionManager
        );
    }

    // ==================================================================================
    // 만료 예약이 없는 경우
    // ==================================================================================

    @Test
    @DisplayName("만료 예약이 없으면 해소 로직을 실행하지 않는다")
    void expireTimedOutReservations_noExpired_doesNotProcess() {
        // given
        when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF)).thenReturn(List.of());

        // when
        expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

        // then
        verifyNoInteractions(inventoryLockPort);
        verifyNoInteractions(findInventoryPort);
    }

    // ==================================================================================
    // 단일 주문 만료 예약 해소
    // ==================================================================================

    @Nested
    @DisplayName("단일 주문 만료 예약 해소")
    class SingleOrderExpirationTest {

        @Test
        @DisplayName("만료 예약이 존재하면 reserved를 감소시키고 stock은 유지한다")
        void expireTimedOutReservations_decreasesReservedAndKeepsStock() {
            // given
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF)).thenReturn(List.of(reservation));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of(reservation));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            assertThat(inventory.getStockValue()).isEqualTo(10);
            assertThat(inventory.getReserved()).isEqualTo(0);
        }

        @Test
        @DisplayName("만료 예약 레코드를 삭제한다")
        void expireTimedOutReservations_deletesReservationRecords() {
            // given
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF)).thenReturn(List.of(reservation));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of(reservation));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            verify(deleteInventoryReservationPort).deleteByOrderIdAndPricePolicyIds(eq(1L), eq(Set.of(100L)));
        }

        @Test
        @DisplayName("만료 예약 해소 후 복원 이력을 저장한다")
        void expireTimedOutReservations_savesRestorationHistory() {
            // given
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF)).thenReturn(List.of(reservation));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of(reservation));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            verify(saveInventoryRestorationHistoryPort).save(any(InventoryRestorationHistories.class));
        }

        @Test
        @DisplayName("만료 예약 해소 후 캐시와 이벤트를 갱신한다")
        void expireTimedOutReservations_updatesCacheAndPublishesEvent() {
            // given
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF)).thenReturn(List.of(reservation));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of(reservation));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            verify(saveCacheStockPort).save(any(Set.class));
            verify(publishInventoryEventPort).publishInventoryChangedEvent(
                    100L, 1L, 10, InventoryChangeAction.UPDATED
            );
        }

        @Test
        @DisplayName("분산 락 내에서 해소가 실행된다")
        void expireTimedOutReservations_executesWithinDistributedLock() {
            // given
            Inventory inventory = buildInventoryWithReserved(1L, 100L, 10, 3);
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF)).thenReturn(List.of(reservation));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of(reservation));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            verify(inventoryLockPort).executeWithLock(eq(Set.of(100L)), any(Runnable.class));
        }
    }

    // ==================================================================================
    // TOCTOU Guard 테스트
    // ==================================================================================

    @Nested
    @DisplayName("TOCTOU Guard (락 획득 후 재조회)")
    class ToctouGuardTest {

        @Test
        @DisplayName("락 획득 후 예약이 이미 처리되었으면 해소를 건너뛴다")
        void expireTimedOutReservations_reservationAlreadyProcessed_skips() {
            // given
            InventoryReservation reservation = buildReservation(1L, 100L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF)).thenReturn(List.of(reservation));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of());

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            verifyNoInteractions(findInventoryPort);
            verifyNoInteractions(deleteInventoryReservationPort);
            verifyNoInteractions(updateInventoryPort);
        }
    }

    // ==================================================================================
    // 복수 주문/가격 정책 케이스
    // ==================================================================================

    @Nested
    @DisplayName("복수 가격 정책 및 주문 케이스")
    class MultipleOrdersTest {

        @Test
        @DisplayName("복수 가격 정책에 대해 각각 예약을 해소한다")
        void expireTimedOutReservations_handlesMultiplePricePolicies() {
            // given
            Inventory inventory1 = buildInventoryWithReserved(1L, 100L, 10, 2);
            Inventory inventory2 = buildInventoryWithReserved(2L, 200L, 20, 5);
            InventoryReservation reservation1 = buildReservation(1L, 100L, 2);
            InventoryReservation reservation2 = buildReservation(1L, 200L, 5);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF))
                    .thenReturn(List.of(reservation1, reservation2));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L, 200L)))
                    .thenReturn(List.of(reservation1, reservation2));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L, 200L)))
                    .thenReturn(Set.of(inventory1, inventory2));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            assertThat(inventory1.getStockValue()).isEqualTo(10);
            assertThat(inventory1.getReserved()).isEqualTo(0);
            assertThat(inventory2.getStockValue()).isEqualTo(20);
            assertThat(inventory2.getReserved()).isEqualTo(0);
        }

        @Test
        @DisplayName("복수 주문의 만료 예약이 각각 별도 트랜잭션으로 처리된다")
        void expireTimedOutReservations_multipleOrders_processedSeparately() {
            // given
            Inventory inventory1 = buildInventoryWithReserved(1L, 100L, 10, 2);
            Inventory inventory2 = buildInventoryWithReserved(2L, 200L, 20, 3);
            InventoryReservation reservation1 = buildReservation(1L, 100L, 2);
            InventoryReservation reservation2 = buildReservation(2L, 200L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF))
                    .thenReturn(List.of(reservation1, reservation2));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of(reservation1));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(2L, Set.of(200L)))
                    .thenReturn(List.of(reservation2));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L))).thenReturn(Set.of(inventory1));
            when(findInventoryPort.findByPricePolicyIds(Set.of(200L))).thenReturn(Set.of(inventory2));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            verify(inventoryLockPort).executeWithLock(eq(Set.of(100L)), any(Runnable.class));
            verify(inventoryLockPort).executeWithLock(eq(Set.of(200L)), any(Runnable.class));
        }

        @Test
        @DisplayName("하나의 주문 해소가 실패해도 나머지 주문은 계속 처리한다")
        void expireTimedOutReservations_oneOrderFails_continuesProcessingOthers() {
            // given
            Inventory inventory2 = buildInventoryWithReserved(2L, 200L, 20, 3);
            InventoryReservation reservation1 = buildReservation(1L, 100L, 2);
            InventoryReservation reservation2 = buildReservation(2L, 200L, 3);

            stubTransactionTemplate();
            stubLockToExecuteTask();
            when(findExpiredInventoryReservationPort.findExpiredBefore(CUTOFF))
                    .thenReturn(List.of(reservation1, reservation2));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(1L, Set.of(100L)))
                    .thenReturn(List.of(reservation1));
            when(findInventoryReservationPort.findByOrderIdAndPricePolicyIds(2L, Set.of(200L)))
                    .thenReturn(List.of(reservation2));
            when(findInventoryPort.findByPricePolicyIds(Set.of(100L)))
                    .thenThrow(new RuntimeException("DB 오류"));
            when(findInventoryPort.findByPricePolicyIds(Set.of(200L)))
                    .thenReturn(Set.of(inventory2));

            // when
            expireInventoryReservationService.expireTimedOutReservations(CUTOFF);

            // then
            assertThat(inventory2.getReserved()).isEqualTo(0);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private void stubTransactionTemplate() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    private void stubLockToExecuteTask() {
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(inventoryLockPort).executeWithLock(any(), any());
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
                .reservedAt(LocalDateTime.of(2026, 4, 2, 8, 30, 0))
                .build());
    }
}
