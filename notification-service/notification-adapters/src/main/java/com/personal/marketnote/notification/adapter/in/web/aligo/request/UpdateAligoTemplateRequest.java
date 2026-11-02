package com.personal.marketnote.notification.adapter.in.web.aligo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateAligoTemplateRequest(
        @NotBlank(message = "senderKey는 필수입니다")
        String senderKey,

        @NotBlank(message = "템플릿명은 필수입니다")
        String templateName,

        @NotBlank(message = "템플릿 내용은 필수입니다")
        String templateContent,

        String templateTitle,
        String templateSubtitle,

        @Size(max = 5, message = "버튼은 최대 5개까지 등록할 수 있습니다")
        List<RegisterAligoTemplateRequest.ButtonRequest> buttons
) {
}
