package com.personal.marketnote.notification.adapter.in.web.aligo.request;

import jakarta.validation.constraints.NotBlank;

public record AuthenticateAligoSenderProfileRequest(
        @NotBlank(message = "카카오채널 ID는 필수입니다")
        String plusId,

        @NotBlank(message = "전화번호는 필수입니다")
        String phoneNumber
) {
}
