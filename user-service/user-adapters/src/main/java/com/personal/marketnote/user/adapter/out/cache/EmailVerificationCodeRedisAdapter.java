package com.personal.marketnote.user.adapter.out.cache;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.port.out.authentication.SaveEmailVerificationCodePort;
import com.personal.marketnote.user.port.out.authentication.VerifyCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailVerificationCodeRedisAdapter implements SaveEmailVerificationCodePort, VerifyCodePort {
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${verification.redis.prefix:email:verification:}")
    private String keyPrefix;

    @Override
    public void save(String email, String verificationCode, int ttlMinutes) {
        String key = buildKey(email);
        stringRedisTemplate.opsForValue().set(key, verificationCode, Duration.ofMinutes(ttlMinutes));
    }

    @Override
    public boolean verify(String email, String targetCode) {
        String key = buildKey(email);
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        boolean isMatch = FormatValidator.equals(storedCode, targetCode);

        if (isMatch) {
            stringRedisTemplate.delete(key);
        }

        return isMatch;
    }

    private String buildKey(String email) {
        return keyPrefix + email;
    }
}
