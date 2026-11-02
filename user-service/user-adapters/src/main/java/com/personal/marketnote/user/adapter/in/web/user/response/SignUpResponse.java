package com.personal.marketnote.user.adapter.in.web.user.response;

public record SignUpResponse(
        String accessToken,
        boolean isNewUser
) {
    public static SignUpResponse of(String accessToken, boolean isNewUser) {
        return new SignUpResponse(accessToken, isNewUser);
    }
}
