package com.personal.marketnote.notification.adapter.in.web.aligo.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteAligoTemplateRequest(
        @NotBlank(message = "senderKey는 필수입니다")
        String senderKey
) {
}
