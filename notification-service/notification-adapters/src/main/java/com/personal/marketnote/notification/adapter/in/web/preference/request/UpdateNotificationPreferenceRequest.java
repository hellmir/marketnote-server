package com.personal.marketnote.notification.adapter.in.web.preference.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateNotificationPreferenceRequest(
        @NotBlank(message = "알림 타입은 필수입니다")
        @Schema(description = "알림 타입", example = "EVENT")
        String notificationType,

        @NotNull(message = "수신 여부는 필수입니다")
        @Schema(description = "수신 여부", example = "true")
        Boolean enabled
) {
}
