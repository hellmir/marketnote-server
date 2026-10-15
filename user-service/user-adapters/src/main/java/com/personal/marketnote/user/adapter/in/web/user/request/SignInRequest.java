package com.personal.marketnote.user.adapter.in.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignInRequest {
    @Schema(
            name = "email",
            description = "이메일 주소",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
            name = "password",
            description = "비밀번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String password;

    @Schema(
            name = "fingerprint",
            description = "디바이스 핑거프린트",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "핑거프린트는 영숫자, 하이픈, 언더스코어만 허용합니다.")
    @Size(max = 255, message = "핑거프린트는 255자를 초과할 수 없습니다.")
    private String fingerprint;
}
