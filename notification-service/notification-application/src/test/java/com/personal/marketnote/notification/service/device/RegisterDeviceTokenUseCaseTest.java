package com.personal.marketnote.notification.service.device;

import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenCreateState;
import com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.port.in.command.RegisterDeviceTokenCommand;
import com.personal.marketnote.notification.port.in.result.device.RegisterDeviceTokenResult;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.SaveDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.UpdateDeviceTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterDeviceTokenUseCaseTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 4, 9, 10, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private RegisterDeviceTokenService registerDeviceTokenService;

    @Mock
    private FindDeviceTokenPort findDeviceTokenPort;

    @Mock
    private SaveDeviceTokenPort saveDeviceTokenPort;

    @Mock
    private UpdateDeviceTokenPort updateDeviceTokenPort;

    @Mock
    private DeleteDeviceTokenPort deleteDeviceTokenPort;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("신규 디바이스 토큰을 등록한다")
    void shouldRegisterNewDeviceToken() {
        // given
        RegisterDeviceTokenCommand command = new RegisterDeviceTokenCommand(
                100L,
                "new-fcm-token",
                "ANDROID",
                "device-uuid-001"
        );

        when(findDeviceTokenPort.findActiveByDeviceId("device-uuid-001"))
                .thenReturn(Optional.empty());
        when(saveDeviceTokenPort.save(any(DeviceToken.class)))
                .thenReturn(1L);
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        // when
        RegisterDeviceTokenResult result = registerDeviceTokenService.registerDeviceToken(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.isNew()).isTrue();
        verify(findDeviceTokenPort).findActiveByDeviceId("device-uuid-001");
        verify(saveDeviceTokenPort).save(any(DeviceToken.class));
        verify(updateDeviceTokenPort, never()).update(any());
    }

    @Test
    @DisplayName("동일 사용자가 같은 deviceId로 재등록하면 기존 토큰을 갱신한다")
    void shouldUpdateExistingDeviceTokenWhenSameOwner() {
        // given
        RegisterDeviceTokenCommand command = new RegisterDeviceTokenCommand(
                100L,
                "updated-fcm-token",
                "IOS",
                "device-uuid-001"
        );

        DeviceToken existing = DeviceToken.from(DeviceTokenCreateState.builder()
                .userId(100L)
                .token("old-fcm-token")
                .platform(Platform.ANDROID)
                .deviceId("device-uuid-001")
                .lastUsedAt(FIXED_NOW.minusDays(1))
                .build());

        when(findDeviceTokenPort.findActiveByDeviceId("device-uuid-001"))
                .thenReturn(Optional.of(existing));
        when(updateDeviceTokenPort.update(any(DeviceToken.class)))
                .thenReturn(10L);
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        // when
        RegisterDeviceTokenResult result = registerDeviceTokenService.registerDeviceToken(command);

        // then
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.isNew()).isFalse();
        assertThat(existing.getUserId()).isEqualTo(100L);
        assertThat(existing.getToken()).isEqualTo("updated-fcm-token");
        assertThat(existing.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(existing.getLastUsedAt()).isEqualTo(FIXED_NOW);
        verify(findDeviceTokenPort).findActiveByDeviceId("device-uuid-001");
        verify(updateDeviceTokenPort).update(existing);
        verify(saveDeviceTokenPort, never()).save(any());
    }

    @Test
    @DisplayName("다른 사용자가 같은 deviceId로 등록하면 기존 토큰을 삭제하고 신규 토큰을 등록한다")
    void shouldDeleteExistingAndCreateNewWhenDifferentOwner() {
        // given
        RegisterDeviceTokenCommand command = new RegisterDeviceTokenCommand(
                200L,
                "new-user-fcm-token",
                "IOS",
                "device-uuid-001"
        );

        DeviceToken existing = DeviceToken.from(DeviceTokenSnapshotState.builder()
                .id(10L)
                .userId(100L)
                .token("old-fcm-token")
                .platform(Platform.ANDROID)
                .deviceId("device-uuid-001")
                .lastUsedAt(FIXED_NOW.minusDays(1))
                .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                .createdAt(FIXED_NOW.minusDays(1))
                .modifiedAt(FIXED_NOW.minusDays(1))
                .build());

        when(findDeviceTokenPort.findActiveByDeviceId("device-uuid-001"))
                .thenReturn(Optional.of(existing));
        when(saveDeviceTokenPort.save(any(DeviceToken.class)))
                .thenReturn(11L);
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        // when
        RegisterDeviceTokenResult result = registerDeviceTokenService.registerDeviceToken(command);

        // then
        assertThat(result.id()).isEqualTo(11L);
        assertThat(result.isNew()).isTrue();
        verify(deleteDeviceTokenPort).deleteById(10L);
        verify(saveDeviceTokenPort).save(any(DeviceToken.class));
        verify(updateDeviceTokenPort, never()).update(any());
    }
}
