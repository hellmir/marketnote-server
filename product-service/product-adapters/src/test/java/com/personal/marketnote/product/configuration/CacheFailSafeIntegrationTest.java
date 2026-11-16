package com.personal.marketnote.product.configuration;

import com.personal.marketnote.common.configuration.cache.SerializationFailSafeCacheErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 캐시 fail-safe 통합 회귀 테스트(#4882).
 * <p>
 * 시나리오: 캐시에 {@link SerializationException} 을 던지는 polluted entry 가 적재된 상태에서
 * {@link Cacheable} 메서드를 호출하면:
 * <ol>
 *     <li>Spring CacheInterceptor 가 cache.get(key) 시도 → SerializationException 발생</li>
 *     <li>{@link SerializationFailSafeCacheErrorHandler} 가 cache.evict(key) 로 polluted entry 무효화</li>
 *     <li>Spring 이 cache miss 로 처리하여 원본 메서드(DB 폴백 시뮬레이션) 실행</li>
 *     <li>새 결과를 cache.put(key, value) 로 작성하여 다음 호출은 캐시 hit</li>
 * </ol>
 * <p>
 * 검증 포인트:
 * <ul>
 *     <li>polluted entry 가 호출 후 사라진다 (evict 동작)</li>
 *     <li>메서드 결과가 정상 반환된다 (예외 미전파, DB 폴백 정상 동작)</li>
 *     <li>두 번째 호출은 캐시 hit 으로 메서드를 다시 실행하지 않는다 (재캐시 갱신 동작)</li>
 * </ul>
 *
 * @see SerializationFailSafeCacheErrorHandler
 */
class CacheFailSafeIntegrationTest {

    private AnnotationConfigApplicationContext context;
    private FallbackService fallbackService;
    private PollutedConcurrentMapCache pollutedCache;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.register(TestCacheConfig.class);
        context.refresh();
        fallbackService = context.getBean(FallbackService.class);
        pollutedCache = context.getBean(PollutedConcurrentMapCache.class);
    }

    @Test
    @DisplayName("polluted entry 가 적재된 상태에서 호출 시 fail-safe 핸들러가 evict 후 DB 폴백을 실행한다")
    void pollutedCache_failSafeEvictsAndFallsBackToMethod() {
        // given - polluted entry 적재 (GET 시 SerializationException 던짐)
        pollutedCache.poison("k1");
        assertThat(pollutedCache.isPoisoned("k1")).isTrue();
        fallbackService.resetCounter();

        // when - 첫 호출: polluted GET 실패 → fail-safe → evict → DB 폴백
        String first = fallbackService.findById("k1");

        // then - 결과 정상 반환 + polluted 제거 + 메서드 1회 실행
        assertThat(first).isEqualTo("DB:k1");
        assertThat(pollutedCache.isPoisoned("k1")).isFalse();
        assertThat(fallbackService.invocationCount()).isEqualTo(1);

        // when - 두 번째 호출: 새 형식으로 캐시 hit
        String second = fallbackService.findById("k1");

        // then - 메서드는 다시 실행되지 않고 캐시 결과 반환
        assertThat(second).isEqualTo("DB:k1");
        assertThat(fallbackService.invocationCount()).isEqualTo(1);
    }

    /**
     * @Configuration / @TestConfiguration 미사용 — Boot 컴포넌트 스캔 자동 픽업 차단.
     * @EnableCaching 으로 @Cacheable AOP 활성화 (proxyTargetClass=true 로 CGLIB 강제 — FallbackService 가 인터페이스 미구현이라 필수).
     */
    @EnableCaching(proxyTargetClass = true)
    static class TestCacheConfig implements CachingConfigurer {

        @Bean
        public PollutedConcurrentMapCache pollutedTestCache() {
            return new PollutedConcurrentMapCache("test-cache");
        }

        /**
         * lite-mode @Configuration(=일반 class + @Bean): inter-bean 메서드 호출은 프록시되지 않으므로
         * pollutedTestCache() 를 직접 호출하면 새 인스턴스가 만들어진다.
         * 따라서 인자 주입으로 동일 빈을 받아야 SimpleCacheManager 와 테스트가 같은 캐시 인스턴스를 공유한다.
         */
        @Bean
        public CacheManager testCacheManager(PollutedConcurrentMapCache pollutedTestCache) {
            SimpleCacheManager manager = new SimpleCacheManager();
            manager.setCaches(List.of(pollutedTestCache));
            manager.initializeCaches();
            return manager;
        }

        @Bean
        @Override
        public CacheErrorHandler errorHandler() {
            return new SerializationFailSafeCacheErrorHandler();
        }

        @Bean
        public FallbackService fallbackService() {
            return new FallbackService();
        }
    }

    /**
     * 의도적으로 GET 시 SerializationException 을 던지는 ConcurrentMapCache 변종.
     * {@link #poison(Object)} 으로 해당 키에 polluted 마커를 적재하고,
     * 이후 {@link #lookup(Object)} 호출 시 SerializationException 을 던진다.
     * {@link #evict(Object)} 호출되면 polluted 마커가 제거된다.
     */
    static class PollutedConcurrentMapCache extends ConcurrentMapCache {
        private final ConcurrentMap<Object, Boolean> poisonMarkers = new ConcurrentHashMap<>();

        PollutedConcurrentMapCache(String name) {
            super(name);
        }

        void poison(Object key) {
            poisonMarkers.put(key, Boolean.TRUE);
        }

        boolean isPoisoned(Object key) {
            return poisonMarkers.containsKey(key);
        }

        @Override
        protected Object lookup(Object key) {
            if (poisonMarkers.containsKey(key)) {
                throw new SerializationException("polluted entry — 캐시 형식 변경 잔존");
            }
            return super.lookup(key);
        }

        @Override
        public void evict(Object key) {
            poisonMarkers.remove(key);
            super.evict(key);
        }
    }

    static class FallbackService {
        private final AtomicInteger counter = new AtomicInteger();

        @Cacheable(cacheNames = "test-cache", key = "#id")
        public String findById(String id) {
            counter.incrementAndGet();
            return "DB:" + id;
        }

        void resetCounter() {
            counter.set(0);
        }

        int invocationCount() {
            return counter.get();
        }
    }
}
