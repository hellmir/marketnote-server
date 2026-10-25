package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoConfirmDeliveredOrdersUseCaseTest {
    @Mock
    private FindOrderPort findOrderPort;
    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-26T14:59:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private AutoConfirmDeliveredOrdersService autoConfirmDeliveredOrdersService;

    private static final int CHUNK_SIZE = 100;

    @Nested
    @DisplayName("구매 자동 확정 — 커서 청크 루프")
    class AutoConfirmTest {

        @Test
        @DisplayName("첫 청크 조회 결과가 비어있으면 ChangeOrderStatusUseCase가 호출되지 않는다")
        void shouldNotCallChangeOrderStatusWhenFirstChunkEmpty() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(List.of());

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            verifyNoInteractions(changeOrderStatusUseCase);
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE);
        }

        @Test
        @DisplayName("청크 크기 미만 결과를 한 번 조회하면 루프를 종료한다")
        void shouldStopLoopWhenChunkSmallerThanLimit() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(List.of(100L, 200L, 300L));

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            verify(changeOrderStatusUseCase, times(3))
                    .changeOrderStatus(any(ChangeOrderStatusCommand.class));
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE);
        }

        @Test
        @DisplayName("청크 결과 주문은 CONFIRMED 상태로 변경된다")
        void shouldConfirmEligibleOrder() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(List.of(100L));

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            ArgumentCaptor<ChangeOrderStatusCommand> captor
                    = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
            verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

            ChangeOrderStatusCommand command = captor.getValue();
            assertThat(command.id()).isEqualTo(100L);
            assertThat(command.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(command.isPartialProductChange()).isFalse();
        }

        @Test
        @DisplayName("청크 크기가 한도와 같으면 마지막 ID를 커서로 다음 청크를 조회한다")
        void shouldAdvanceCursorWhenChunkReachesLimit() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            List<Long> firstChunk = LongStream.rangeClosed(1, CHUNK_SIZE).boxed().toList();
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(firstChunk);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            )).thenReturn(List.of());

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE);
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            );
            verify(changeOrderStatusUseCase, times(CHUNK_SIZE))
                    .changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }

        @Test
        @DisplayName("다중 청크 처리 시 각 청크의 마지막 ID가 다음 lastId로 사용된다")
        void shouldAdvanceCursorThroughMultipleChunks() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            List<Long> firstChunk = LongStream.rangeClosed(1, CHUNK_SIZE).boxed().toList();
            List<Long> secondChunk = LongStream.rangeClosed(CHUNK_SIZE + 1L, CHUNK_SIZE * 2L).boxed().toList();
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(firstChunk);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            )).thenReturn(secondChunk);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(
                    expectedNow, CHUNK_SIZE * 2L, CHUNK_SIZE
            )).thenReturn(List.of());

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE);
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            );
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(
                    expectedNow, CHUNK_SIZE * 2L, CHUNK_SIZE
            );
            verify(changeOrderStatusUseCase, times(CHUNK_SIZE * 2))
                    .changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }

        @Test
        @DisplayName("청크 내 한 주문 처리 실패 시 다음 주문들이 정상 처리되고 커서는 그대로 전진한다")
        void shouldContinueProcessingAndAdvanceCursorWhenOneOrderFails() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(List.of(100L, 200L, 300L));
            doThrow(new RuntimeException("처리 실패"))
                    .when(changeOrderStatusUseCase)
                    .changeOrderStatus(argThat(cmd -> cmd.id().equals(200L)));

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            verify(changeOrderStatusUseCase, times(3))
                    .changeOrderStatus(any(ChangeOrderStatusCommand.class));
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE);
        }

        @Test
        @DisplayName("첫 청크가 한도와 같고 두 번째 청크가 한도 미만이면 조기 종료한다")
        void shouldReturnEarlyWhenSecondChunkSmallerThanLimit() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            List<Long> firstChunk = LongStream.rangeClosed(1, CHUNK_SIZE).boxed().toList();
            List<Long> secondChunk = List.of(CHUNK_SIZE + 1L);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(firstChunk);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            )).thenReturn(secondChunk);

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE);
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            );
            verify(findOrderPort, times(2)).findOrderIdsScheduledForAutoConfirm(
                    any(LocalDateTime.class), any(Long.class), any(Integer.class)
            );
            verify(changeOrderStatusUseCase, times(CHUNK_SIZE + 1))
                    .changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }

        @Test
        @DisplayName("청크 한도(100건) 중 일부 실패해도 lastId는 청크 마지막 ID로 전진하여 다음 청크를 조회한다")
        void shouldAdvanceCursorToChunkLastIdEvenWhenFailurePresent() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            List<Long> firstChunk = LongStream.rangeClosed(1, CHUNK_SIZE).boxed().toList();
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(firstChunk);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            )).thenReturn(List.of());
            doThrow(new RuntimeException("처리 실패"))
                    .when(changeOrderStatusUseCase)
                    .changeOrderStatus(argThat(cmd -> cmd.id().equals(50L)));

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE);
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(
                    expectedNow, (long) CHUNK_SIZE, CHUNK_SIZE
            );
            verify(changeOrderStatusUseCase, times(CHUNK_SIZE))
                    .changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }

        @Test
        @DisplayName("now 파라미터는 clock 기준의 현재 시각이다")
        void shouldPassClockNowAsQueryParameter() {
            // given
            LocalDateTime expectedNow = LocalDateTime.now(clock);
            when(findOrderPort.findOrderIdsScheduledForAutoConfirm(expectedNow, 0L, CHUNK_SIZE))
                    .thenReturn(List.of());

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders();

            // then
            // Clock이 2026-04-26T14:59:00Z = KST 2026-04-26 23:59:00
            assertThat(expectedNow).isEqualTo(LocalDateTime.of(2026, 4, 8, 23, 59, 0));
            verify(findOrderPort).findOrderIdsScheduledForAutoConfirm(eq(expectedNow), eq(0L), eq(CHUNK_SIZE));
        }
    }
}
