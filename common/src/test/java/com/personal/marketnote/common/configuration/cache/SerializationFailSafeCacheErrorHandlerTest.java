package com.personal.marketnote.common.configuration.cache;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.data.redis.serializer.SerializationException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SerializationFailSafeCacheErrorHandler 단위 테스트.
 * <p>
 * 검증 정책(#4882):
 * <ul>
 *     <li>SerializationException / JsonMappingException(MismatchedInputException 포함) 발생 시
 *         polluted entry 를 cache.evict(key) 로 무효화하고 swallow → 호출자는 cache miss 로 처리</li>
 *     <li>그 외 예외는 default 동작(re-throw) 유지하여 fail-safe 가 모든 결함을 숨기지 않도록 좁게 제한</li>
 *     <li>PUT / EVICT / CLEAR 결함은 모두 swallow — 캐시 부수 작업 실패가 메서드 결과를 막지 않도록</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class SerializationFailSafeCacheErrorHandlerTest {

    private static final String FAILURE_METRIC = "cache.deserialization.failure";

    private SerializationFailSafeCacheErrorHandler handler;
    private MeterRegistry meterRegistry;

    @Mock
    private Cache cache;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        handler = new SerializationFailSafeCacheErrorHandler(meterRegistry);
        lenient().when(cache.getName()).thenReturn("test-cache");
    }

    @Test
    @DisplayName("GET 시 SerializationException 발생 → polluted entry 를 evict 하고 swallow 한다")
    void handleCacheGetError_serializationException_evictsAndSwallows() {
        SerializationException exception = new SerializationException("형식 변경 잔존 데이터");

        assertThatCode(() -> handler.handleCacheGetError(exception, cache, "k1"))
            .doesNotThrowAnyException();

        verify(cache).evict("k1");
    }

    @Test
    @DisplayName("GET 시 SerializationException(cause=MismatchedInputException) cause 재귀 추적으로 evict 한다")
    void handleCacheGetError_nestedMismatchedInput_evicts() {
        MismatchedInputException mismatched = MismatchedInputException.from(
            (com.fasterxml.jackson.core.JsonParser) null, "type id 미발견"
        );
        SerializationException exception = new SerializationException("redis 역직렬화 실패", mismatched);

        assertThatCode(() -> handler.handleCacheGetError(exception, cache, "k2"))
            .doesNotThrowAnyException();

        verify(cache).evict("k2");
    }

    @Test
    @DisplayName("GET 시 RuntimeException(cause=JsonMappingException) cause 재귀 추적으로 evict 한다")
    void handleCacheGetError_nestedJsonMappingException_evicts() {
        JsonMappingException mappingException = JsonMappingException.from(
            (com.fasterxml.jackson.core.JsonParser) null, "필드 매핑 실패"
        );
        RuntimeException exception = new RuntimeException("wrapper", mappingException);

        assertThatCode(() -> handler.handleCacheGetError(exception, cache, "k3"))
            .doesNotThrowAnyException();

        verify(cache).evict("k3");
    }

    @Test
    @DisplayName("GET 시 결함 클래스가 아닌 예외(IllegalStateException)는 default 동작으로 re-throw 한다")
    void handleCacheGetError_nonSerializationFault_rethrows() {
        IllegalStateException exception = new IllegalStateException("커넥션 단절");

        assertThatThrownBy(() -> handler.handleCacheGetError(exception, cache, "k4"))
            .isSameAs(exception);

        verify(cache, never()).evict("k4");
    }

    @Test
    @DisplayName("GET 시 evict 자체가 실패해도 swallow 하여 호출자에게 예외가 전파되지 않는다")
    void handleCacheGetError_evictFailure_swallowed() {
        SerializationException exception = new SerializationException("결함");
        doThrow(new RuntimeException("redis 단절")).when(cache).evict("k5");

        assertThatCode(() -> handler.handleCacheGetError(exception, cache, "k5"))
            .doesNotThrowAnyException();

        verify(cache).evict("k5");
    }

    @Test
    @DisplayName("PUT 시 SerializationException 발생해도 swallow 하여 메서드 결과 반환을 막지 않는다")
    void handleCachePutError_swallows() {
        SerializationException exception = new SerializationException("PUT 실패");

        assertThatCode(() -> handler.handleCachePutError(exception, cache, "k6", "value"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("EVICT 실패 시 swallow 한다")
    void handleCacheEvictError_swallows() {
        RuntimeException exception = new RuntimeException("EVICT 실패");

        assertThatCode(() -> handler.handleCacheEvictError(exception, cache, "k7"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CLEAR 실패 시 swallow 한다")
    void handleCacheClearError_swallows() {
        RuntimeException exception = new RuntimeException("CLEAR 실패");

        assertThatCode(() -> handler.handleCacheClearError(exception, cache))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("역직렬화 실패 시 cache.deserialization.failure 카운터가 증가한다")
    void handleCacheGetError_serializationFault_incrementsCounter() {
        SerializationException exception = new SerializationException("형식 변경 잔존 데이터");

        handler.handleCacheGetError(exception, cache, "k1");

        Counter counter = meterRegistry.find(FAILURE_METRIC).counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("cache/exception 태그가 정확히 매칭된다")
    void handleCacheGetError_serializationFault_taggedWithCacheAndException() {
        SerializationException exception = new SerializationException("결함");

        handler.handleCacheGetError(exception, cache, "k2");

        Counter counter = meterRegistry.find(FAILURE_METRIC)
            .tag("cache", "test-cache")
            .tag("exception", "SerializationException")
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("역직렬화가 아닌 일반 예외는 카운터를 증가시키지 않는다")
    void handleCacheGetError_nonSerializationFault_doesNotIncrementCounter() {
        IllegalStateException exception = new IllegalStateException("커넥션 단절");

        assertThatThrownBy(() -> handler.handleCacheGetError(exception, cache, "k3"))
            .isSameAs(exception);

        Counter counter = meterRegistry.find(FAILURE_METRIC).counter();
        assertThat(counter).isNull();
    }

    @Test
    @DisplayName("cause 체인의 MismatchedInputException 도 outermost 예외 클래스명으로 태깅된다")
    void handleCacheGetError_nestedMismatchedInput_taggedByOutermostException() {
        MismatchedInputException mismatched = MismatchedInputException.from(
            (com.fasterxml.jackson.core.JsonParser) null, "type id 미발견"
        );
        SerializationException exception = new SerializationException("redis 역직렬화 실패", mismatched);

        handler.handleCacheGetError(exception, cache, "k4");

        Counter counter = meterRegistry.find(FAILURE_METRIC)
            .tag("exception", "SerializationException")
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}
