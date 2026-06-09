package com.personal.marketnote.notification.adapter.in.web.template.response;

import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.result.template.GetNotificationTemplateResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record GetNotificationTemplateResponse(
        @Schema(description = "템플릿 ID", example = "1")
        Long id,

        @Schema(description = "템플릿 코드", example = "ORDER_PAYMENT_COMPLETED")
        String templateCode,

        @Schema(description = "알림 유형", example = "ORDER_PAYMENT_COMPLETED")
        NotificationType notificationType,

        @Schema(description = "제목", example = "주문이 완료되었습니다")
        String title,

        @Schema(description = "본문 템플릿", example = "{productName} 외 {count}건이 결제되었습니다.")
        String bodyTemplate,

        @Schema(description = "URL 템플릿", example = "/order/{orderId}")
        String urlTemplate,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime modifiedAt
) {

    public static GetNotificationTemplateResponse from(GetNotificationTemplateResult result) {
        return new GetNotificationTemplateResponse(
                result.id(),
                result.templateCode(),
                result.notificationType(),
                result.title(),
                result.bodyTemplate(),
                result.urlTemplate(),
                result.createdAt(),
                result.modifiedAt()
        );
    }
}
