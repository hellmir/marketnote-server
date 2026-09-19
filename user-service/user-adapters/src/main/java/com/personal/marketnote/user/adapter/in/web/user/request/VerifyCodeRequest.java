package com.personal.marketnote.user.adapter.in.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class VerifyCodeRequest {
    @Schema(
            name = "email",
            description = "이메일 주소",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "이메일 주소는 필수값입니다.")
    private String email;

    @Schema(
            name = "verificationCode",
            description = "인증 코드",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "인증 코드는 필수값입니다.")
    private String verificationCode;
}
