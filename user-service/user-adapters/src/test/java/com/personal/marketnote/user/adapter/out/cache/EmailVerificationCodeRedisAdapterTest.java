package com.personal.marketnote.user.adapter.out.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationCodeRedisAdapterTest {

    @InjectMocks
    private EmailVerificationCodeRedisAdapter adapter;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "keyPrefix", "email:verification:");
    }

    @Nested
    @DisplayName("save - 인증코드 저장")
    class SaveTest {

        @Test
        @DisplayName("이메일과 인증코드를 TTL과 함께 Redis에 저장한다")
        void shouldSaveVerificationCodeWithTtl() {
            // given
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

            // when
            adapter.save("user@example.com", "123456", 5);

            // then
            verify(valueOperations).set("email:verification:user@example.com", "123456", Duration.ofMinutes(5));
        }
    }

    @Nested
    @DisplayName("verify - 인증코드 검증")
    class VerifyTest {

        @Test
        @DisplayName("인증코드가 일치하면 true를 반환하고 키를 삭제한다")
        void shouldReturnTrueAndDeleteKeyWhenCodeMatches() {
            // given
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("email:verification:user@example.com")).thenReturn("123456");

            // when
            boolean result = adapter.verify("user@example.com", "123456");

            // then
            assertThat(result).isTrue();
            verify(stringRedisTemplate).delete("email:verification:user@example.com");
        }

        @Test
        @DisplayName("인증코드가 불일치하면 false를 반환하고 키를 삭제하지 않는다")
        void shouldReturnFalseAndNotDeleteKeyWhenCodeMismatch() {
            // given
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("email:verification:user@example.com")).thenReturn("123456");

            // when
            boolean result = adapter.verify("user@example.com", "999999");

            // then
            assertThat(result).isFalse();
            verify(stringRedisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("저장된 인증코드가 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoStoredCode() {
            // given
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("email:verification:user@example.com")).thenReturn(null);

            // when
            boolean result = adapter.verify("user@example.com", "123456");

            // then
            assertThat(result).isFalse();
            verify(stringRedisTemplate, never()).delete(anyString());
        }
    }
}
