package com.personal.marketnote.user.port.out.authentication;

import java.util.Optional;

public interface FindRefreshTokenPort {
    Optional<String> findByUserId(Long userId);

    boolean matchesByUserId(Long userId, String rawRefreshToken);
}
