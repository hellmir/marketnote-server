package com.personal.marketnote.notification.domain.device;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceTokenTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 9, 10, 0);

    @Test
    @DisplayName("CreateState로 디바이스 토큰을 생성한다")
    void shouldCreateDeviceTokenFromCreateState() {
        DeviceToken deviceToken = DeviceToken.from(DeviceTokenCreateState.builder()
                .userId(100L)
                .token("fcm-token-value")
                .platform(Platform.ANDROID)
                .deviceId("device-uuid-001")
                .lastUsedAt(NOW)
                .build());

        assertThat(deviceToken.getUserId()).isEqualTo(100L);
        assertThat(deviceToken.getToken()).isEqualTo("fcm-token-value");
        assertThat(deviceToken.getPlatform()).isEqualTo(Platform.ANDROID);
        assertThat(deviceToken.getDeviceId()).isEqualTo("device-uuid-001");
        assertThat(deviceToken.getLastUsedAt()).isEqualTo(NOW);
        assertThat(deviceToken.isActive()).isTrue();
    }

    @Test
    @DisplayName("userId가 null이면 예외를 던진다")
    void shouldThrowExceptionWhenUserIdIsNull() {
        DeviceTokenCreateState state = DeviceTokenCreateState.builder()
                .userId(null)
                .token("fcm-token-value")
                .platform(Platform.ANDROID)
                .deviceId("device-uuid-001")
                .lastUsedAt(NOW)
                .build();

        assertThatThrownBy(() -> DeviceToken.from(state))
                .isInstanceOf(InvalidDeviceTokenException.class);
    }

    @Test
    @DisplayName("token이 비어있으면 예외를 던진다")
    void shouldThrowExceptionWhenTokenIsBlank() {
        DeviceTokenCreateState state = DeviceTokenCreateState.builder()
                .userId(100L)
                .token(" ")
                .platform(Platform.ANDROID)
                .deviceId("device-uuid-001")
                .lastUsedAt(NOW)
                .build();

        assertThatThrownBy(() -> DeviceToken.from(state))
                .isInstanceOf(InvalidDeviceTokenException.class);
    }

    @Test
    @DisplayName("deviceId가 비어있으면 예외를 던진다")
    void shouldThrowExceptionWhenDeviceIdIsBlank() {
        DeviceTokenCreateState state = DeviceTokenCreateState.builder()
                .userId(100L)
                .token("fcm-token-value")
                .platform(Platform.ANDROID)
                .deviceId("")
                .lastUsedAt(NOW)
                .build();

        assertThatThrownBy(() -> DeviceToken.from(state))
                .isInstanceOf(InvalidDeviceTokenException.class);
    }

    @Test
    @DisplayName("platform이 null이면 예외를 던진다")
    void shouldThrowExceptionWhenPlatformIsNull() {
        DeviceTokenCreateState state = DeviceTokenCreateState.builder()
                .userId(100L)
                .token("fcm-token-value")
                .platform(null)
                .deviceId("device-uuid-001")
                .lastUsedAt(NOW)
                .build();

        assertThatThrownBy(() -> DeviceToken.from(state))
                .isInstanceOf(InvalidDeviceTokenException.class);
    }

    @Test
    @DisplayName("SnapshotState로 디바이스 토큰을 복원한다")
    void shouldRestoreDeviceTokenFromSnapshotState() {
        DeviceToken deviceToken = DeviceToken.from(DeviceTokenSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .token("fcm-token-value")
                .platform(Platform.IOS)
                .deviceId("device-uuid-001")
                .lastUsedAt(NOW)
                .status(EntityStatus.ACTIVE)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());

        assertThat(deviceToken.getId()).isEqualTo(1L);
        assertThat(deviceToken.getUserId()).isEqualTo(100L);
        assertThat(deviceToken.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(deviceToken.isActive()).isTrue();
    }

    @Test
    @DisplayName("token을 갱신한다")
    void shouldUpdateToken() {
        DeviceToken deviceToken = activeDeviceToken();
        LocalDateTime later = NOW.plusDays(1);

        deviceToken.updateToken("new-token", Platform.IOS, later);

        assertThat(deviceToken.getUserId()).isEqualTo(100L);
        assertThat(deviceToken.getToken()).isEqualTo("new-token");
        assertThat(deviceToken.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(deviceToken.getLastUsedAt()).isEqualTo(later);
        assertThat(deviceToken.getDeviceId()).isEqualTo("device-uuid-001");
    }

    @Test
    @DisplayName("token 갱신 시 새 token이 비어있으면 예외를 던진다")
    void shouldThrowExceptionWhenUpdatingWithBlankToken() {
        DeviceToken deviceToken = activeDeviceToken();

        assertThatThrownBy(() -> deviceToken.updateToken("", Platform.IOS, NOW))
                .isInstanceOf(InvalidDeviceTokenException.class);
    }

    @Test
    @DisplayName("isOwnedBy는 소유자 userId와 일치할 때만 true를 반환한다")
    void shouldReturnTrueWhenOwnedByUser() {
        DeviceToken deviceToken = activeDeviceToken();

        assertThat(deviceToken.isOwnedBy(100L)).isTrue();
        assertThat(deviceToken.isOwnedBy(200L)).isFalse();
    }

    private DeviceToken activeDeviceToken() {
        return DeviceToken.from(DeviceTokenCreateState.builder()
                .userId(100L)
                .token("fcm-token-value")
                .platform(Platform.ANDROID)
                .deviceId("device-uuid-001")
                .lastUsedAt(NOW)
                .build());
    }
}
