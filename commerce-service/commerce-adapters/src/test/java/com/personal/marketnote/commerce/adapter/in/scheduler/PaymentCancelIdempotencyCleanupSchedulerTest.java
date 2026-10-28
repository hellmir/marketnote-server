package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.PaymentCancelIdempotencyCleanupProperties;
import com.personal.marketnote.commerce.port.out.payment.idempotency.DeletePaymentCancelIdempotencyPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelIdempotencyCleanupScheduler 단위 테스트")
class PaymentCancelIdempotencyCleanupSchedulerTest {

    @InjectMocks
    private PaymentCancelIdempotencyCleanupScheduler scheduler;

    @Mock
    private DeletePaymentCancelIdempotencyPort deletePort;

    @Mock(name = "commerceClock")
    private Clock commerceClock;

    @Mock
    private PaymentCancelIdempotencyCleanupProperties properties;

    @BeforeEach
    void setUpClock() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-24T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        when(commerceClock.instant()).thenReturn(fixedClock.instant());
        when(commerceClock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("프로퍼티에 설정된 ttlHours 만큼 이전 시각을 threshold 로 사용해 삭제 포트를 호출한다")
    void shouldUseConfiguredTtlHoursForThreshold() {
        // given
        when(properties.getTtlHours()).thenReturn(24L);
        LocalDateTime expected = LocalDateTime.now(commerceClock).minusHours(24L);

        // when
        scheduler.cleanupExpiredIdempotencyRecords();

        // then
        verify(deletePort).deleteOlderThan(expected);
    }

    @Test
    @DisplayName("ttlHours=12 로 재정의되면 12시간 이전을 threshold 로 사용한다")
    void shouldUseCustomTtlHoursForThreshold() {
        // given
        when(properties.getTtlHours()).thenReturn(12L);
        LocalDateTime expected = LocalDateTime.now(commerceClock).minusHours(12L);

        // when
        scheduler.cleanupExpiredIdempotencyRecords();

        // then
        verify(deletePort).deleteOlderThan(expected);
    }

    @Test
    @DisplayName("삭제 포트에서 예외가 발생해도 스케줄러는 예외를 삼킨다")
    void shouldSwallowExceptionFromDeletePort() {
        // given
        when(properties.getTtlHours()).thenReturn(24L);
        LocalDateTime expected = LocalDateTime.now(commerceClock).minusHours(24L);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(deletePort).deleteOlderThan(expected);

        // when & then
        assertThatCode(() -> scheduler.cleanupExpiredIdempotencyRecords())
                .doesNotThrowAnyException();
    }
}
