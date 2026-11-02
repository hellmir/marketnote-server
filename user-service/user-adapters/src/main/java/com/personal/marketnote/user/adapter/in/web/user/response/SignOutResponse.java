package com.personal.marketnote.user.adapter.in.web.user.response;

public record SignOutResponse(
        String accessToken
) {
    public static SignOutResponse of(String accessToken, String refreshToken) {
        return new SignOutResponse(refreshToken);
    }
}
