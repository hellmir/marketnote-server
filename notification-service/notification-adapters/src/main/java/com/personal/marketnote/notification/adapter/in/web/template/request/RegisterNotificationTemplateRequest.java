package com.personal.marketnote.notification.adapter.in.web.template.request;

import com.personal.marketnote.notification.domain.template.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterNotificationTemplateRequest(
        @NotBlank(message = "템플릿 코드는 필수입니다")
        @Size(max = 100, message = "템플릿 코드는 100자 이하여야 합니다")
        @Schema(description = "템플릿 코드", example = "ORDER_PAYMENT_COMPLETED")
        String templateCode,

        @NotNull(message = "알림 유형은 필수입니다")
        @Schema(description = "알림 유형", example = "ORDER_PAYMENT_COMPLETED")
        NotificationType notificationType,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        @Schema(description = "제목", example = "주문이 완료되었습니다")
        String title,

        @NotBlank(message = "본문 템플릿은 필수입니다")
        @Size(max = 500, message = "본문 템플릿은 500자 이하여야 합니다")
        @Schema(description = "본문 템플릿 ({변수명} 형식)", example = "{productName} 외 {count}건이 결제되었습니다.")
        String bodyTemplate,

        @Size(max = 500, message = "URL 템플릿은 500자 이하여야 합니다")
        @Schema(description = "URL 템플릿 ({변수명} 형식)", example = "/order/{orderId}")
        String urlTemplate
) {
}
