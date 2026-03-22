package com.personal.marketnote.common.security.hmac;

import com.personal.marketnote.common.security.hmac.exception.HmacNonceReplayedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.REDIS_NONCE_KEY_PREFIX;
import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.TIMESTAMP_TOLERANCE_SECONDS;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmacNonceValidatorTest {
    @InjectMocks
    private HmacNonceValidator hmacNonceValidator;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final String NONCE = "550e8400-e29b-41d4-a716-446655440000";
    private static final String NONCE_KEY = REDIS_NONCE_KEY_PREFIX + NONCE;

    @Test
    @DisplayName("신규 Nonce는 Redis에 저장되고 검증을 통과한다")
    void shouldPassForNewNonce() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(NONCE_KEY, "1", Duration.ofSeconds(TIMESTAMP_TOLERANCE_SECONDS)))
                .thenReturn(true);

        assertThatCode(() -> hmacNonceValidator.validateAndStore(NONCE))
                .doesNotThrowAnyException();

        verify(valueOperations).setIfAbsent(NONCE_KEY, "1", Duration.ofSeconds(TIMESTAMP_TOLERANCE_SECONDS));
    }

    @Test
    @DisplayName("중복 Nonce는 HmacNonceReplayedException이 발생한다")
    void shouldThrowForDuplicateNonce() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(NONCE_KEY, "1", Duration.ofSeconds(TIMESTAMP_TOLERANCE_SECONDS)))
                .thenReturn(false);

        assertThatThrownBy(() -> hmacNonceValidator.validateAndStore(NONCE))
                .isInstanceOf(HmacNonceReplayedException.class);
    }

    @Test
    @DisplayName("Redis 장애 시 fail-open으로 검증을 통과한다")
    void shouldFailOpenWhenRedisUnavailable() {
        when(stringRedisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        assertThatCode(() -> hmacNonceValidator.validateAndStore(NONCE))
                .doesNotThrowAnyException();
    }
}
