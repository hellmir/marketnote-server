package com.personal.marketnote.notification.adapter.in.web.template.response;

import com.personal.marketnote.notification.port.in.result.template.RegisterNotificationTemplateResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterNotificationTemplateResponse(
        @Schema(description = "생성된 템플릿 ID", example = "1")
        Long id
) {

    public static RegisterNotificationTemplateResponse from(RegisterNotificationTemplateResult result) {
        return new RegisterNotificationTemplateResponse(result.id());
    }
}
