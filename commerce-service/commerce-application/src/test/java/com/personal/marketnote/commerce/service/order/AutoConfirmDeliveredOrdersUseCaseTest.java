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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private static final long AUTO_CONFIRM_DAYS = 7;

    @Nested
    @DisplayName("구매 자동 확정")
    class AutoConfirmTest {

        @Test
        @DisplayName("자동 확정 대상 주문이 없으면 ChangeOrderStatusUseCase가 호출되지 않는다")
        void shouldNotCallChangeOrderStatusWhenNoEligibleOrders() {
            // given
            LocalDateTime expectedDeliveredBefore = LocalDateTime.now(clock).minusDays(AUTO_CONFIRM_DAYS);
            when(findOrderPort.findOrderIdsEligibleForAutoConfirm(expectedDeliveredBefore))
                    .thenReturn(List.of());

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders(AUTO_CONFIRM_DAYS);

            // then
            verifyNoInteractions(changeOrderStatusUseCase);
            verify(findOrderPort).findOrderIdsEligibleForAutoConfirm(expectedDeliveredBefore);
        }

        @Test
        @DisplayName("자동 확정 대상 주문이 1건이면 해당 주문에 대해 CONFIRMED 상태로 변경한다")
        void shouldConfirmSingleEligibleOrder() {
            // given
            LocalDateTime expectedDeliveredBefore = LocalDateTime.now(clock).minusDays(AUTO_CONFIRM_DAYS);
            when(findOrderPort.findOrderIdsEligibleForAutoConfirm(expectedDeliveredBefore))
                    .thenReturn(List.of(100L));

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders(AUTO_CONFIRM_DAYS);

            // then
            ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
            verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

            ChangeOrderStatusCommand command = captor.getValue();
            assertThat(command.id()).isEqualTo(100L);
            assertThat(command.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(command.isInternalCall()).isTrue();
            assertThat(command.isPartialProductChange()).isFalse();
        }

        @Test
        @DisplayName("자동 확정 대상 주문이 여러 건이면 각각에 대해 CONFIRMED 상태로 변경한다")
        void shouldConfirmMultipleEligibleOrders() {
            // given
            LocalDateTime expectedDeliveredBefore = LocalDateTime.now(clock).minusDays(AUTO_CONFIRM_DAYS);
            when(findOrderPort.findOrderIdsEligibleForAutoConfirm(expectedDeliveredBefore))
                    .thenReturn(List.of(100L, 200L, 300L));

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders(AUTO_CONFIRM_DAYS);

            // then
            verify(changeOrderStatusUseCase, times(3)).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }

        @Test
        @DisplayName("한 주문 처리 실패 시 나머지 주문은 정상 처리된다")
        void shouldContinueProcessingWhenOneOrderFails() {
            // given
            LocalDateTime expectedDeliveredBefore = LocalDateTime.now(clock).minusDays(AUTO_CONFIRM_DAYS);
            when(findOrderPort.findOrderIdsEligibleForAutoConfirm(expectedDeliveredBefore))
                    .thenReturn(List.of(100L, 200L, 300L));
            doThrow(new RuntimeException("처리 실패"))
                    .when(changeOrderStatusUseCase).changeOrderStatus(argThat(cmd -> cmd.id().equals(200L)));

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders(AUTO_CONFIRM_DAYS);

            // then
            verify(changeOrderStatusUseCase, times(3)).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }

        @Test
        @DisplayName("deliveredBefore는 현재 시각에서 autoConfirmDays를 뺀 값이다")
        void shouldCalculateDeliveredBeforeCorrectly() {
            // given
            LocalDateTime expectedDeliveredBefore = LocalDateTime.now(clock).minusDays(AUTO_CONFIRM_DAYS);
            when(findOrderPort.findOrderIdsEligibleForAutoConfirm(expectedDeliveredBefore))
                    .thenReturn(List.of());

            // when
            autoConfirmDeliveredOrdersService.autoConfirmDeliveredOrders(AUTO_CONFIRM_DAYS);

            // then
            // Clock이 2026-04-26T14:59:00Z = KST 2026-04-26 23:59:00
            // deliveredBefore = 2026-04-01 23:59:00
            assertThat(expectedDeliveredBefore).isEqualTo(LocalDateTime.of(2026, 4, 1, 23, 59, 0));
            verify(findOrderPort).findOrderIdsEligibleForAutoConfirm(expectedDeliveredBefore);
        }
    }
}
