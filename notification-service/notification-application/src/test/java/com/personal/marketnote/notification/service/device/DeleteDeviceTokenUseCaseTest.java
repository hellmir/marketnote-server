package com.personal.marketnote.notification.service.device;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenNotFoundException;
import com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.port.in.command.DeleteDeviceTokenCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteDeviceTokenUseCaseTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 9, 10, 0);

    @InjectMocks
    private DeleteDeviceTokenService deleteDeviceTokenService;

    @Mock
    private FindDeviceTokenPort findDeviceTokenPort;

    @Mock
    private DeleteDeviceTokenPort deleteDeviceTokenPort;

    @Test
    @DisplayName("소유자가 자기 디바이스 토큰을 삭제한다")
    void shouldDeleteDeviceTokenByOwner() {
        // given
        DeleteDeviceTokenCommand command = new DeleteDeviceTokenCommand(100L, "device-uuid-001");
        DeviceToken existing = activeDeviceToken(10L, 100L, "device-uuid-001");

        when(findDeviceTokenPort.findActiveByDeviceId("device-uuid-001"))
                .thenReturn(Optional.of(existing));

        // when
        deleteDeviceTokenService.deleteDeviceToken(command);

        // then
        verify(deleteDeviceTokenPort).deleteById(10L);
    }

    @Test
    @DisplayName("디바이스 토큰이 존재하지 않으면 예외를 던진다")
    void shouldThrowExceptionWhenDeviceTokenNotFound() {
        // given
        DeleteDeviceTokenCommand command = new DeleteDeviceTokenCommand(100L, "device-uuid-001");

        when(findDeviceTokenPort.findActiveByDeviceId("device-uuid-001"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deleteDeviceTokenService.deleteDeviceToken(command))
                .isInstanceOf(DeviceTokenNotFoundException.class);
        verify(deleteDeviceTokenPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("다른 사용자가 디바이스 토큰을 삭제하려 하면 예외를 던진다")
    void shouldThrowExceptionWhenNotOwner() {
        // given
        DeleteDeviceTokenCommand command = new DeleteDeviceTokenCommand(200L, "device-uuid-001");
        DeviceToken existing = activeDeviceToken(10L, 100L, "device-uuid-001");

        when(findDeviceTokenPort.findActiveByDeviceId("device-uuid-001"))
                .thenReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> deleteDeviceTokenService.deleteDeviceToken(command))
                .isInstanceOf(DeviceTokenNotFoundException.class);
        verify(deleteDeviceTokenPort, never()).deleteById(any());
    }

    private DeviceToken activeDeviceToken(Long id, Long userId, String deviceId) {
        return DeviceToken.from(DeviceTokenSnapshotState.builder()
                .id(id)
                .userId(userId)
                .token("fcm-token-value")
                .platform(Platform.ANDROID)
                .deviceId(deviceId)
                .lastUsedAt(NOW)
                .status(EntityStatus.ACTIVE)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());
    }
}
