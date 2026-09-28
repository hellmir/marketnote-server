package com.personal.marketnote.common.adapter.in.ratelimit;

import com.personal.marketnote.common.domain.exception.RateLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock(lenient = true)
    private RateLimited rateLimited;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 Rate Limit 검사 없이 요청을 통과시킨다")
    void shouldProceedWithoutCheckWhenUserIsNotAuthenticated() throws Throwable {
        // given
        SecurityContextHolder.clearContext();
        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        assertThat(result).isEqualTo(expected);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("OAuth2 인증이 아닌 사용자는 Rate Limit 검사 없이 요청을 통과시킨다")
    void shouldProceedWithoutCheckWhenPrincipalIsNotOAuth2() throws Throwable {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("plainUser", "password")
        );
        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        assertThat(result).isEqualTo(expected);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("첫 번째 요청 시 Redis 키의 TTL을 설정하고 요청을 통과시킨다")
    void shouldSetTtlOnFirstRequestAndProceed() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");
        when(rateLimited.windowSeconds()).thenReturn(60);
        when(rateLimited.maxRequests()).thenReturn(5);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(1L);

        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        assertThat(result).isEqualTo(expected);
        verify(redisTemplate).expire("rate_limit:test-api:1", 60, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("제한 횟수 이내의 요청은 정상적으로 통과시킨다")
    void shouldProceedWhenRequestCountIsWithinLimit() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");
        when(rateLimited.maxRequests()).thenReturn(5);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(3L);

        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("제한 횟수를 초과하면 RateLimitExceededException을 던진다")
    void shouldThrowExceptionWhenRateLimitExceeded() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");
        when(rateLimited.maxRequests()).thenReturn(5);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(6L);

        // when & then
        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, rateLimited))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("Redis increment 결과가 null이면 요청을 통과시킨다")
    void shouldProceedWhenIncrementReturnsNull() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(null);

        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Redis 장애 시 요청을 허용한다")
    void shouldProceedWhenRedisThrowsException() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenThrow(new RuntimeException("Redis connection failed"));

        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private void setUpOAuth2Authentication(Long userId) {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        lenient().when(principal.getName()).thenReturn(String.valueOf(userId));

        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
