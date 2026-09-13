package com.personal.marketnote.common.adapter.in.ratelimit;

import com.personal.marketnote.common.domain.exception.RateLimitExceededException;
import com.personal.marketnote.common.utility.ElementExtractor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class RateLimitAspect {
    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String KEY_PREFIX = "rate_limit:";

    private final StringRedisTemplate redisTemplate;

    public RateLimitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        Long userId = extractUserId();
        if (userId == null) {
            return joinPoint.proceed();
        }

        String redisKey = KEY_PREFIX + rateLimited.key() + ":" + userId;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            if (currentCount == null) {
                return joinPoint.proceed();
            }

            if (currentCount == 1L) {
                redisTemplate.expire(redisKey, rateLimited.windowSeconds(), TimeUnit.SECONDS);
            }

            if (currentCount > rateLimited.maxRequests()) {
                throw new RateLimitExceededException(rateLimited.key(), userId);
            }
        } catch (RateLimitExceededException rlExceededException) {
            throw rlExceededException;
        } catch (Exception e) {
            log.warn("Rate limiting 확인 중 오류 발생, 요청을 허용합니다. key={}, userId={}, error={}",
                    rateLimited.key(), userId, e.getMessage());
        }

        return joinPoint.proceed();
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2AuthenticatedPrincipal oauth2Principal) {
            return ElementExtractor.extractUserId(oauth2Principal);
        }

        return null;
    }
}
