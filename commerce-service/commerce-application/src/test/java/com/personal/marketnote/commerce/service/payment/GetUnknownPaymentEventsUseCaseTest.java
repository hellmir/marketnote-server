package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.payment.PaymentEventStatus;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEventSnapshotState;
import com.personal.marketnote.commerce.port.in.result.payment.GetUnknownPaymentEventsResult;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUnknownPaymentEventsUseCase 테스트")
class GetUnknownPaymentEventsUseCaseTest {

    @InjectMocks
    private GetUnknownPaymentEventsService getUnknownPaymentEventsService;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    @Nested
    @DisplayName("UNKNOWN 결제 이벤트 목록 조회")
    class GetUnknownPaymentEventsTest {

        @Test
        @DisplayName("UNKNOWN 상태의 결제 이벤트가 존재하면 결과 목록을 반환한다")
        void shouldReturnUnknownPaymentEventsList() {
            // given
            PspPaymentEvent event = createUnknownEvent(1L, 100L, "order-key-1", 50000L, "CARD", "9999", "결제 상태 미확인");

            when(findPspPaymentEventPort.findAllByUnknownStatus()).thenReturn(List.of(event));

            // when
            List<GetUnknownPaymentEventsResult> results = getUnknownPaymentEventsService.getUnknownPaymentEvents();

            // then
            assertThat(results).hasSize(1);
            GetUnknownPaymentEventsResult result = results.get(0);
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.orderId()).isEqualTo(100L);
            assertThat(result.orderKey()).isEqualTo("order-key-1");
            assertThat(result.amount()).isEqualTo(50000L);
            assertThat(result.method()).isEqualTo("CARD");
            assertThat(result.resultCode()).isEqualTo("9999");
            assertThat(result.resultMessage()).isEqualTo("결제 상태 미확인");
            verify(findPspPaymentEventPort).findAllByUnknownStatus();
        }

        @Test
        @DisplayName("UNKNOWN 상태의 결제 이벤트가 여러 건이면 모두 매핑되어 반환된다")
        void shouldReturnAllUnknownPaymentEvents() {
            // given
            PspPaymentEvent event1 = createUnknownEvent(1L, 100L, "order-key-1", 50000L, "CARD", "9999", "미확인");
            PspPaymentEvent event2 = createUnknownEvent(2L, 200L, "order-key-2", 30000L, "BANK", "8888", "타임아웃");

            when(findPspPaymentEventPort.findAllByUnknownStatus()).thenReturn(List.of(event1, event2));

            // when
            List<GetUnknownPaymentEventsResult> results = getUnknownPaymentEventsService.getUnknownPaymentEvents();

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).id()).isEqualTo(1L);
            assertThat(results.get(1).id()).isEqualTo(2L);
            verify(findPspPaymentEventPort).findAllByUnknownStatus();
        }

        @Test
        @DisplayName("UNKNOWN 상태의 결제 이벤트가 없으면 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenNoUnknownEvents() {
            // given
            when(findPspPaymentEventPort.findAllByUnknownStatus()).thenReturn(Collections.emptyList());

            // when
            List<GetUnknownPaymentEventsResult> results = getUnknownPaymentEventsService.getUnknownPaymentEvents();

            // then
            assertThat(results).isEmpty();
            verify(findPspPaymentEventPort).findAllByUnknownStatus();
        }
    }

    private PspPaymentEvent createUnknownEvent(Long id, Long orderId, String orderKey,
                                                Long amount, String method,
                                                String resultCode, String resultMessage) {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .orderKey(orderKey)
                .poStatus(PaymentEventStatus.UNKNOWN)
                .method(method)
                .amount(amount)
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .createdAt(LocalDateTime.of(2026, 4, 2, 10, 0, 0))
                .build();
        return PspPaymentEvent.from(state);
    }
}
