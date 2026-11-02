package com.personal.marketnote.user.adapter.in.web.authentication.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class RefreshedAccessTokenResponse {
    @Schema(description = "재발급된 Access Token")
    private final String accessToken;
}
