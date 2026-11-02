package com.personal.marketnote.user.adapter.in.web.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshAccessTokenRequest {
    @Schema(
            name = "refreshToken",
            description = "리프레시 토큰값",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "리프레시 토큰값은 필수입니다.")
    private String refreshToken;
}
