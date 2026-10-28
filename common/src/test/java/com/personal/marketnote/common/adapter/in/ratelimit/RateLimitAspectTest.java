package com.personal.marketnote.common.adapter.in.ratelimit;

import com.personal.marketnote.common.domain.exception.RateLimitExceededException;
import com.personal.marketnote.common.utility.metrics.CounterRegistryHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    private RateLimitAspect rateLimitAspect;
    private MeterRegistry meterRegistry;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock(lenient = true)
    private RateLimited rateLimited;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        rateLimitAspect = new RateLimitAspect(redisTemplate, new CounterRegistryHelper(meterRegistry));
    }

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

    @Test
    @DisplayName("요청이 추적되면 rate_limit.hit.total 카운터가 key 태그와 함께 증가한다")
    void shouldIncrementHitCounterWhenRequestIsTracked() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");
        when(rateLimited.maxRequests()).thenReturn(5);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(3L);

        when(joinPoint.proceed()).thenReturn("result");

        // when
        rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        double hitCount = meterRegistry.counter("rate_limit.hit.total", "key", "test-api").count();
        assertThat(hitCount).isEqualTo(1.0);

        double exceededCount = meterRegistry.counter("rate_limit.exceeded.total", "key", "test-api").count();
        assertThat(exceededCount).isEqualTo(0.0);
    }

    @Test
    @DisplayName("제한 초과 시 rate_limit.exceeded.total 카운터가 key 태그와 함께 증가한다")
    void shouldIncrementExceededCounterWhenLimitExceeded() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");
        when(rateLimited.maxRequests()).thenReturn(5);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(6L);

        // when
        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, rateLimited))
                .isInstanceOf(RateLimitExceededException.class);

        // then
        double hitCount = meterRegistry.counter("rate_limit.hit.total", "key", "test-api").count();
        assertThat(hitCount).isEqualTo(1.0);

        double exceededCount = meterRegistry.counter("rate_limit.exceeded.total", "key", "test-api").count();
        assertThat(exceededCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Redis increment 결과가 null이면 카운터를 증가시키지 않는다")
    void shouldNotIncrementCounterWhenIncrementReturnsNull() throws Throwable {
        // given
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(null);

        when(joinPoint.proceed()).thenReturn("result");

        // when
        rateLimitAspect.checkRateLimit(joinPoint, rateLimited);

        // then
        double hitCount = meterRegistry.counter("rate_limit.hit.total", "key", "test-api").count();
        assertThat(hitCount).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Redis increment가 null 반환 시 untracked 카운터를 증가시키고 요청을 허용한다")
    void shouldIncrementUntrackedCounterAndProceedWhenIncrementReturnsNull() throws Throwable {
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
        verify(joinPoint).proceed();

        double untrackedCount = meterRegistry.counter("rate_limit.untracked.total", "key", "test-api").count();
        assertThat(untrackedCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("CounterRegistryHelper가 null일 때 Redis increment null 반환이어도 NPE 없이 요청을 허용한다")
    void shouldProceedWithoutUntrackedCounterWhenCounterRegistryHelperIsNullAndIncrementReturnsNull() throws Throwable {
        // given
        RateLimitAspect aspectWithoutMetrics = new RateLimitAspect(redisTemplate, null);
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(null);

        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object result = aspectWithoutMetrics.checkRateLimit(joinPoint, rateLimited);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("CounterRegistryHelper가 null이어도 NPE 없이 정상 동작한다")
    void shouldWorkWithoutNullPointerExceptionWhenCounterRegistryHelperIsNull() throws Throwable {
        // given
        RateLimitAspect aspectWithoutMetrics = new RateLimitAspect(redisTemplate, null);
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");
        when(rateLimited.maxRequests()).thenReturn(5);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(3L);

        when(joinPoint.proceed()).thenReturn("result");

        // when & then
        assertThatCode(() -> aspectWithoutMetrics.checkRateLimit(joinPoint, rateLimited))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CounterRegistryHelper가 null일 때 제한 초과도 카운터 기록 없이 예외만 던진다")
    void shouldThrowExceededExceptionWithoutMetricsWhenCounterRegistryHelperIsNull() throws Throwable {
        // given
        RateLimitAspect aspectWithoutMetrics = new RateLimitAspect(redisTemplate, null);
        setUpOAuth2Authentication(1L);
        when(rateLimited.key()).thenReturn("test-api");
        when(rateLimited.maxRequests()).thenReturn(5);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate_limit:test-api:1")).thenReturn(6L);

        // when & then
        assertThatThrownBy(() -> aspectWithoutMetrics.checkRateLimit(joinPoint, rateLimited))
                .isInstanceOf(RateLimitExceededException.class);
    }

    private void setUpOAuth2Authentication(Long userId) {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        lenient().when(principal.getName()).thenReturn(String.valueOf(userId));

        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
