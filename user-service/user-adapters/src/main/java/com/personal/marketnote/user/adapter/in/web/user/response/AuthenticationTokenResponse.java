package com.personal.marketnote.user.adapter.in.web.user.response;

public record AuthenticationTokenResponse(
        String accessToken,
        String refreshToken
) {
    public static AuthenticationTokenResponse of(String accessToken, String refreshToken) {
        return new AuthenticationTokenResponse(accessToken, refreshToken);
    }
}
