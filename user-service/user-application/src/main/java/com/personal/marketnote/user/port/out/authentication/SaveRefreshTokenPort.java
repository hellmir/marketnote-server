package com.personal.marketnote.user.port.out.authentication;

public interface SaveRefreshTokenPort {
    void saveByUserId(Long userId, String refreshToken, long ttlMillis);
}
