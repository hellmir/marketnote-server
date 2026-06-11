package com.personal.marketnote.notification.domain.device;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class DeviceToken extends BaseDomain {
    private Long id;
    private Long userId;
    private String token;
    private Platform platform;
    private String deviceId;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static DeviceToken from(DeviceTokenCreateState state) {
        validate(state.getUserId(), state.getToken(), state.getPlatform(), state.getDeviceId());

        DeviceToken deviceToken = DeviceToken.builder()
                .userId(state.getUserId())
                .token(state.getToken())
                .platform(state.getPlatform())
                .deviceId(state.getDeviceId())
                .lastUsedAt(state.getLastUsedAt())
                .build();
        deviceToken.activate();
        return deviceToken;
    }

    public static DeviceToken from(DeviceTokenSnapshotState state) {
        DeviceToken deviceToken = DeviceToken.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .token(state.getToken())
                .platform(state.getPlatform())
                .deviceId(state.getDeviceId())
                .lastUsedAt(state.getLastUsedAt())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        deviceToken.status = state.getStatus();
        return deviceToken;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void updateToken(String token, Platform platform, LocalDateTime lastUsedAt) {
        validate(this.userId, token, platform, this.deviceId);
        this.token = token;
        this.platform = platform;
        this.lastUsedAt = lastUsedAt;
    }

    private static void validate(Long userId, String token, Platform platform, String deviceId) {
        if (FormatValidator.hasNoValue(userId)) {
            throw new InvalidDeviceTokenException("사용자 ID는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(token) || token.isBlank()) {
            throw new InvalidDeviceTokenException("FCM 토큰은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(platform)) {
            throw new InvalidDeviceTokenException("플랫폼은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(deviceId) || deviceId.isBlank()) {
            throw new InvalidDeviceTokenException("기기 ID는 필수입니다.");
        }
    }
}
