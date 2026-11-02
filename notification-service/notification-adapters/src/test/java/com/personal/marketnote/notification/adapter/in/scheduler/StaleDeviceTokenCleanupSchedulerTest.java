package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.device.CleanupStaleDeviceTokensUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaleDeviceTokenCleanupSchedulerTest {

    @InjectMocks
    private StaleDeviceTokenCleanupScheduler staleDeviceTokenCleanupScheduler;

    @Mock
    private CleanupStaleDeviceTokensUseCase cleanupStaleDeviceTokensUseCase;

    @Test
    @DisplayName("Stale 디바이스 토큰 정리 스케줄러가 UseCase를 호출하여 만료 토큰을 비활성화한다")
    void shouldCleanupStaleDeviceTokens() {
        // given
        when(cleanupStaleDeviceTokensUseCase.cleanupStaleDeviceTokens()).thenReturn(7);

        // when
        staleDeviceTokenCleanupScheduler.cleanupStaleDeviceTokens();

        // then
        verify(cleanupStaleDeviceTokensUseCase).cleanupStaleDeviceTokens();
    }

    @Test
    @DisplayName("비활성화 건수가 0이면 UseCase 호출은 정상 수행되고 로그만 다르다")
    void shouldHandleZeroDeactivatedCount() {
        // given
        when(cleanupStaleDeviceTokensUseCase.cleanupStaleDeviceTokens()).thenReturn(0);

        // when
        staleDeviceTokenCleanupScheduler.cleanupStaleDeviceTokens();

        // then
        verify(cleanupStaleDeviceTokensUseCase).cleanupStaleDeviceTokens();
    }
}
