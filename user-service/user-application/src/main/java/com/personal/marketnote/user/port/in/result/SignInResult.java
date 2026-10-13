package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.User;

public record SignInResult(
        Long id,
        String roleId,
        boolean isRequiredTermsAgreed,
        String accessToken,
        String refreshToken
) {
    public static SignInResult of(User user, String accessToken, String refreshToken) {
        return new SignInResult(user.getId(), user.getRole().getId(), user.isRequiredTermsAgreed(), accessToken, refreshToken);
    }
}
