package com.personal.marketnote.fulfillment.adapter.in.scheduler;

import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.PollShippingStatusUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDeliveryStatusPollingScheduler 테스트")
class FulfillmentDeliveryStatusPollingSchedulerTest {

    @InjectMocks
    private FulfillmentDeliveryStatusPollingScheduler scheduler;

    @Mock
    private PollShippingStatusUseCase pollShippingStatusUseCase;

    @Mock
    private FulfillmentAuthProperties fasstoAuthProperties;

    @Mock
    private Clock clock;

    private void stubClock(String instantStr) {
        Clock fixedClock = Clock.fixed(Instant.parse(instantStr), ZoneId.of("Asia/Seoul"));
        when(clock.withZone(ZoneId.of("Asia/Seoul"))).thenReturn(fixedClock);
    }

    @Nested
    @DisplayName("성공")
    class Success {

        @Test
        @DisplayName("폴링 시간대(08~21시) 내에서 customerCode가 있으면 폴링을 실행한다")
        void shouldPollWhenWithinWindowAndCustomerCodeExists() {
            // given
            stubClock("2026-09-28T05:00:00Z"); // KST 14:00
            when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

            // when
            scheduler.pollDeliveryStatuses();

            // then
            verify(pollShippingStatusUseCase).pollShippingStatuses(any(PollShippingStatusCommand.class));
        }
    }

    @Nested
    @DisplayName("실패")
    class Failure {

        @Test
        @DisplayName("폴링 시간대 외(21시 이후)이면 폴링을 실행하지 않는다")
        void shouldNotPollWhenAfterPollingWindow() {
            // given
            stubClock("2026-09-28T13:00:00Z"); // KST 22:00

            // when
            scheduler.pollDeliveryStatuses();

            // then
            verify(pollShippingStatusUseCase, never()).pollShippingStatuses(any());
        }

        @Test
        @DisplayName("폴링 시간대 외(08시 이전)이면 폴링을 실행하지 않는다")
        void shouldNotPollWhenBeforePollingWindow() {
            // given
            stubClock("2026-09-20T21:00:00Z"); // KST 06:00

            // when
            scheduler.pollDeliveryStatuses();

            // then
            verify(pollShippingStatusUseCase, never()).pollShippingStatuses(any());
        }

        @Test
        @DisplayName("customerCode가 없으면 폴링을 실행하지 않는다")
        void shouldNotPollWhenCustomerCodeMissing() {
            // given
            stubClock("2026-09-28T05:00:00Z"); // KST 14:00
            when(fasstoAuthProperties.getCustomerCode()).thenReturn(null);

            // when
            scheduler.pollDeliveryStatuses();

            // then
            verify(pollShippingStatusUseCase, never()).pollShippingStatuses(any());
        }

        @Test
        @DisplayName("폴링 중 예외 발생 시 예외를 전파하지 않는다")
        void shouldNotPropagateExceptionDuringPolling() {
            // given
            stubClock("2026-09-28T05:00:00Z"); // KST 14:00
            when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
            doThrow(new RuntimeException("폴링 실패"))
                    .when(pollShippingStatusUseCase).pollShippingStatuses(any(PollShippingStatusCommand.class));

            // when & then
            assertThatCode(() -> scheduler.pollDeliveryStatuses())
                    .doesNotThrowAnyException();
        }
    }
}
