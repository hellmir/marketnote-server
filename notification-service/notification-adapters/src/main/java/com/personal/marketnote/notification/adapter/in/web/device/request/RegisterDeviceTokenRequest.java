package com.personal.marketnote.notification.adapter.in.web.device.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDeviceTokenRequest(
        @NotBlank(message = "FCM 토큰은 필수입니다")
        @Size(max = 4096, message = "FCM 토큰은 4096자 이하여야 합니다")
        @Schema(description = "FCM 토큰", example = "cXXXXXXXX:APA91bH...")
        String token,

        @NotBlank(message = "플랫폼은 필수입니다")
        @Pattern(regexp = "ANDROID|IOS", message = "플랫폼은 ANDROID 또는 IOS만 허용됩니다")
        @Schema(description = "플랫폼 (ANDROID/IOS)", example = "ANDROID")
        String platform,

        @NotBlank(message = "기기 ID는 필수입니다")
        @Size(max = 200, message = "기기 ID는 200자 이하여야 합니다")
        @Schema(description = "기기 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        String deviceId
) {
}
