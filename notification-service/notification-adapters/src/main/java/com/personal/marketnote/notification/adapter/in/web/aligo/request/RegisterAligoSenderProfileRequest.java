package com.personal.marketnote.notification.adapter.in.web.aligo.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterAligoSenderProfileRequest(
        @NotBlank(message = "senderKey는 필수입니다")
        String senderKey,

        @NotBlank(message = "카테고리 코드는 필수입니다")
        String categoryCode
) {
}
