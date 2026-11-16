package com.personal.marketnote.common.configuration.cache;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Redis 캐시 직렬화/역직렬화 결함에 대한 fail-safe {@link CacheErrorHandler} 구현체.
 * <p>
 * 동작 정책:
 * <ul>
 *     <li>{@link #handleCacheGetError(RuntimeException, Cache, Object)} — 캐시 형식 변경/오염으로 인한
 *         {@link SerializationException} 또는 Jackson {@link JsonMappingException}(MismatchedInputException 포함)
 *         발생 시: polluted entry 를 {@link Cache#evict(Object)} 로 무효화하고 {@code return} 하여 cache miss 처리.
 *         Spring Cache 가 원본 메서드를 실행하고 새 형식으로 캐시를 재작성한다. 그 외 예외는 default 동작(re-throw) 유지.
 *     <li>{@link #handleCachePutError} / {@link #handleCacheEvictError} / {@link #handleCacheClearError} —
 *         WARN 로그 후 swallow. 캐시 작성/제거 실패가 메서드 실행 결과를 막지 않도록 한다.
 * </ul>
 * <p>
 * 회귀 이력:
 * <ul>
 *     <li>#4855 — prod product-service /api/v1/products 무한 500. 캐시 형식 전환(record→class) 후
 *         이전 형식 잔존 데이터로 인한 SerializationException + MismatchedInputException 무한 루프.</li>
 *     <li>#4882 — 본 fail-safe 도입. polluted entry 자동 evict + DB 폴백 + 다음 호출에서 새 형식 재캐시.</li>
 * </ul>
 *
 * @see CacheErrorHandler
 */
@Slf4j
public class SerializationFailSafeCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        if (!isSerializationFault(exception)) {
            throw exception;
        }
        log.warn(
                "[CacheFailSafe] GET 직렬화 결함 — polluted entry evict. cache={}, key={}, exception={}",
                cache.getName(), key, exception.getClass().getName(), exception
        );
        try {
            cache.evict(key);
        } catch (RuntimeException evictException) {
            log.warn(
                    "[CacheFailSafe] polluted entry evict 실패 — swallow. cache={}, key={}",
                    cache.getName(), key, evictException
            );
        }
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn(
                "[CacheFailSafe] PUT 실패 — swallow. cache={}, key={}, exception={}",
                cache.getName(), key, exception.getClass().getName(), exception
        );
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "[CacheFailSafe] EVICT 실패 — swallow. cache={}, key={}, exception={}",
                cache.getName(), key, exception.getClass().getName(), exception
        );
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn(
                "[CacheFailSafe] CLEAR 실패 — swallow. cache={}, exception={}",
                cache.getName(), exception.getClass().getName(), exception
        );
    }

    /**
     * 예외 cause 체인을 재귀적으로 추적하여 직렬화/역직렬화 결함 클래스를 검출한다.
     * <p>
     * 검출 대상:
     * <ul>
     *     <li>{@link SerializationException} — Spring Data Redis 직렬화 예외</li>
     *     <li>{@link JsonMappingException} — Jackson 매핑 예외 (MismatchedInputException 포함, 자식 클래스)</li>
     * </ul>
     */
    private boolean isSerializationFault(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SerializationException) {
                return true;
            }
            if (current instanceof JsonMappingException) {
                return true;
            }
            Throwable cause = current.getCause();
            if (cause == current) {
                return false;
            }
            current = cause;
        }
        return false;
    }
}
