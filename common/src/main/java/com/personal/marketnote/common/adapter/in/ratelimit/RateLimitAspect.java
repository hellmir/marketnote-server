package com.personal.marketnote.common.adapter.in.ratelimit;

import com.personal.marketnote.common.domain.exception.RateLimitExceededException;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.common.utility.FormatValidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limit AOP — Fixed Window 전략 + 관측성(Observability) 우선 설계
 *
 * <p><b>전략 선택 배경:</b>
 * <ul>
 *   <li>현재 구현은 Redis INCR + EXPIRE 기반의 Fixed Window 카운터이다.</li>
 *   <li>Fixed Window는 윈도우 경계(boundary) 순간에 최대 2배의 버스트 트래픽을 허용하는
 *       이론적 단점이 있으나, 실제 프로덕션에서 이 현상이 문제로 관측되기 전까지
 *       Sliding Window / Token Bucket 전환은 보류한다 (premature optimization 회피).</li>
 *   <li>판단 근거 수집을 위해 아래 2개의 Micrometer Counter 메트릭을 방출한다.</li>
 * </ul>
 *
 * <p><b>방출 메트릭:</b>
 * <ul>
 *   <li>{@code rate_limit.hit.total} — Rate Limit이 적용된 모든 요청 건수 (초과/비초과 포함).</li>
 *   <li>{@code rate_limit.exceeded.total} — 제한을 초과하여 거부된 요청 건수.</li>
 * </ul>
 *
 * <p><b>태그 규약:</b>
 * <ul>
 *   <li>오직 {@code key} 태그만 사용한다 — {@code @RateLimited#key()}의 유한 집합이므로 카디널리티 안전.</li>
 *   <li>{@code userId}는 절대 태그로 사용하지 않는다 — 수백만 단위의 고(高)카디널리티를 유발하여
 *       Prometheus/메트릭 스토리지 부하를 초래한다.</li>
 * </ul>
 *
 * <p><b>관측 후 의사결정:</b> 프로덕션 Grafana에서 {@code rate(rate_limit.hit.total[1s])} 시계열로
 * 윈도우 경계 버스트 패턴을 확인한 뒤 Sliding Window Counter / Token Bucket 전환 여부를 결정한다.
 */
@Aspect
@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class RateLimitAspect {
    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String KEY_PREFIX = "rate_limit:";

    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;

    public RateLimitAspect(
            StringRedisTemplate redisTemplate,
            @Autowired(required = false) @Nullable MeterRegistry meterRegistry
    ) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(rateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        Long userId = extractUserId();
        if (FormatValidator.hasNoValue(userId)) {
            return joinPoint.proceed();
        }

        String redisKey = KEY_PREFIX + rateLimited.key() + ":" + userId;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            if (FormatValidator.hasNoValue(currentCount)) {
                return joinPoint.proceed();
            }

            incrementHitCounter(rateLimited.key());

            if (currentCount == 1L) {
                redisTemplate.expire(redisKey, rateLimited.windowSeconds(), TimeUnit.SECONDS);
            }

            if (currentCount > rateLimited.maxRequests()) {
                incrementExceededCounter(rateLimited.key());
                throw new RateLimitExceededException(rateLimited.key(), userId);
            }
        } catch (RateLimitExceededException rlExceededException) {
            throw rlExceededException;
        } catch (Exception e) {
            log.warn("Rate limiting 확인 중 오류 발생, 요청을 허용합니다. key={}, userId={}, error={}",
                    rateLimited.key(), userId, e.getMessage()
            );
        }

        return joinPoint.proceed();
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (FormatValidator.hasNoValue(authentication)) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2AuthenticatedPrincipal oauth2Principal) {
            return ElementExtractor.extractUserId(oauth2Principal);
        }

        return null;
    }

    private void incrementHitCounter(String key) {
        if (FormatValidator.hasNoValue(meterRegistry)) {
            return;
        }
        Counter.builder("rate_limit.hit.total")
                .tag("key", key)
                .description("RateLimited 적용 요청 건수 (키별, 초과/비초과 포함)")
                .register(meterRegistry)
                .increment();
    }

    private void incrementExceededCounter(String key) {
        if (FormatValidator.hasNoValue(meterRegistry)) {
            return;
        }
        Counter.builder("rate_limit.exceeded.total")
                .tag("key", key)
                .description("RateLimited 초과 요청 건수 (키별)")
                .register(meterRegistry)
                .increment();
    }
}
