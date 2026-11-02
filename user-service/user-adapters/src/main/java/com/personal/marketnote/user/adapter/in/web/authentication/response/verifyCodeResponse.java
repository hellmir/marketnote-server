package com.personal.marketnote.user.adapter.in.web.authentication.response;

public record verifyCodeResponse(
        String accessToken,
        String refreshToken
) {
    public static verifyCodeResponse of(String accessToken, String refreshToken) {
        return new verifyCodeResponse(accessToken, refreshToken);
    }
}
