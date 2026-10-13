package com.personal.marketnote.user.adapter.out.cache;

import com.personal.marketnote.user.port.out.authentication.DeleteRefreshTokenPort;
import com.personal.marketnote.user.port.out.authentication.FindRefreshTokenPort;
import com.personal.marketnote.user.port.out.authentication.SaveRefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements DeleteRefreshTokenPort, SaveRefreshTokenPort, FindRefreshTokenPort {
    private static final String KEY_PREFIX = "refreshToken:";
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void deleteByUserId(Long userId) {
        stringRedisTemplate.delete(KEY_PREFIX + userId);
    }

    @Override
    public void saveByUserId(Long userId, String hashedRefreshToken, long ttlMillis) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + userId, hashedRefreshToken, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return Optional.ofNullable(value);
    }
}
