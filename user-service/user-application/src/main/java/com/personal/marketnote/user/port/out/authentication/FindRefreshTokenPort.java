package com.personal.marketnote.user.port.out.authentication;

import java.util.Optional;

public interface FindRefreshTokenPort {
    Optional<String> findByUserId(Long userId);
}
