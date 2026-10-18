package com.personal.marketnote.common.security.hmac;

import com.personal.marketnote.common.security.hmac.exception.HmacNonceReplayedException;
import com.personal.marketnote.common.security.hmac.exception.HmacNonceValidationUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.REDIS_NONCE_KEY_PREFIX;
import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.TIMESTAMP_TOLERANCE_SECONDS;

@RequiredArgsConstructor
@Slf4j
public class HmacNonceValidator {
    private final StringRedisTemplate stringRedisTemplate;

    public void validateAndStore(String nonce) {
        String key = REDIS_NONCE_KEY_PREFIX + nonce;
        try {
            Boolean isNewKey = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", Duration.ofSeconds(TIMESTAMP_TOLERANCE_SECONDS));
            if (Boolean.FALSE.equals(isNewKey)) {
                throw new HmacNonceReplayedException(nonce);
            }
        } catch (HmacNonceReplayedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis Nonce 검증 실패 (fail-secure): nonce={}, error={}", nonce, e.getMessage(), e);
            throw new HmacNonceValidationUnavailableException(nonce, e);
        }
    }
}
