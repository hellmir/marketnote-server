package com.personal.marketnote.notification.adapter.in.web.template.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNotificationTemplateRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        @Schema(description = "제목", example = "수정된 알림 제목")
        String title,

        @NotBlank(message = "본문 템플릿은 필수입니다")
        @Size(max = 500, message = "본문 템플릿은 500자 이하여야 합니다")
        @Schema(description = "본문 템플릿 ({변수명} 형식)", example = "수정된 본문 {productName}")
        String bodyTemplate,

        @Size(max = 500, message = "URL 템플릿은 500자 이하여야 합니다")
        @Schema(description = "URL 템플릿 ({변수명} 형식)", example = "/order/{orderId}")
        String urlTemplate
) {
}
