package com.personal.marketnote.notification.adapter.in.web.template.response;

import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.result.template.UpdateNotificationTemplateResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateNotificationTemplateResponse(
        @Schema(description = "템플릿 ID", example = "1")
        Long id,

        @Schema(description = "템플릿 코드", example = "ORDER_PAYMENT_COMPLETED")
        String templateCode,

        @Schema(description = "알림 유형", example = "ORDER_PAYMENT_COMPLETED")
        NotificationType notificationType,

        @Schema(description = "알림 카테고리", example = "INFORMATIONAL")
        NotificationCategory notificationCategory,

        @Schema(description = "제목", example = "수정된 제목")
        String title,

        @Schema(description = "본문 템플릿", example = "수정된 본문 {productName}")
        String bodyTemplate,

        @Schema(description = "URL 템플릿", example = "/order/{orderId}")
        String urlTemplate
) {

    public static UpdateNotificationTemplateResponse from(UpdateNotificationTemplateResult result) {
        return new UpdateNotificationTemplateResponse(
                result.id(),
                result.templateCode(),
                result.notificationType(),
                result.notificationCategory(),
                result.title(),
                result.bodyTemplate(),
                result.urlTemplate()
        );
    }
}
