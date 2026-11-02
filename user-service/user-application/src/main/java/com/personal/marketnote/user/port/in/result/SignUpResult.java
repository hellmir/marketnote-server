package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.User;

public record SignUpResult(
        Long id,
        String roleId,
        boolean isNewUser,
        String accessToken,
        String refreshToken
) {
    public static SignUpResult of(User user, boolean isNewUser, String accessToken, String refreshToken) {
        return new SignUpResult(user.getId(), user.getRole().getId(), isNewUser, accessToken, refreshToken);
    }
}
