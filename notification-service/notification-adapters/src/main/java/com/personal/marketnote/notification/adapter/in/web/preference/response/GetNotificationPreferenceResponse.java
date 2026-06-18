package com.personal.marketnote.notification.adapter.in.web.preference.response;

import com.personal.marketnote.notification.port.in.result.preference.GetNotificationPreferenceResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record GetNotificationPreferenceResponse(
        @Schema(description = "알림 타입", example = "ORDER_PAYMENT_COMPLETED")
        String notificationType,

        @Schema(description = "알림 타입 설명", example = "주문 결제 완료")
        String description,

        @Schema(description = "수신 여부", example = "true")
        boolean enabled,

        @Schema(description = "수신 동의 시점 (광고성 알림)", example = "2026-06-03T10:00:00")
        LocalDateTime consentedAt
) {

    public static GetNotificationPreferenceResponse from(GetNotificationPreferenceResult result) {
        return new GetNotificationPreferenceResponse(
                result.notificationType(),
                result.description(),
                result.enabled(),
                result.consentedAt()
        );
    }
}
