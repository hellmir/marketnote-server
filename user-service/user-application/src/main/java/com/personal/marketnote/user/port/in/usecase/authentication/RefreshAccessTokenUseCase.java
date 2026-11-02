package com.personal.marketnote.user.port.in.usecase.authentication;

public interface RefreshAccessTokenUseCase {
    void validateRefreshToken(String refreshToken);
}
