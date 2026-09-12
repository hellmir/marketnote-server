package com.personal.marketnote.notification.service.device;

import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupStaleDeviceTokensUseCaseTest {

    @InjectMocks
    private CleanupStaleDeviceTokensService cleanupStaleDeviceTokensService;

    @Mock
    private DeleteDeviceTokenPort deleteDeviceTokenPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-09-03T05:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("90일 이전 기준일을 계산하여 deactivateStaleTokens를 호출한다")
    void shouldCallDeactivateWithCorrectThreshold() {
        // given
        when(deleteDeviceTokenPort.deactivateStaleTokens(any(LocalDateTime.class)))
                .thenReturn(5);

        // when
        int result = cleanupStaleDeviceTokensService.cleanupStaleDeviceTokens();

        // then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(deleteDeviceTokenPort).deactivateStaleTokens(captor.capture());

        LocalDateTime threshold = captor.getValue();
        LocalDateTime expected = LocalDateTime.now(clock).minusDays(90);
        assertThat(threshold).isEqualTo(expected);
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("비활성화된 토큰이 없으면 0을 반환한다")
    void shouldReturnZeroWhenNoStaleTokens() {
        // given
        when(deleteDeviceTokenPort.deactivateStaleTokens(any(LocalDateTime.class)))
                .thenReturn(0);

        // when
        int result = cleanupStaleDeviceTokensService.cleanupStaleDeviceTokens();

        // then
        assertThat(result).isZero();
    }
}
