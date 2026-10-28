package com.personal.marketnote.common.utility.metrics;

import com.personal.marketnote.common.domain.exception.illegalstate.InvalidCounterTagPairsException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Micrometer Counter 지연 등록 + 캐시 공통 유틸.
 *
 * <p>per-call {@code Counter.builder(...).register(...)} 오버헤드(빌더 객체 생성 + Micrometer 내부
 * getOrCreate Map 조회)를 제거하기 위해, (name, tags) 조합별 Counter를 {@link ConcurrentHashMap}에
 * 캐싱한다. Micrometer {@code register()}는 idempotent이므로 race 조건에서도 정합성 안전.
 *
 * <p>{@link ConditionalOnBean}({@link MeterRegistry}.class)로 액추에이터 미구동 환경(일부 테스트)에서는
 * 빈 생성 생략. 호출처는 {@code @Autowired(required = false) @Nullable CounterRegistryHelper}로 주입한다.
 */
@Component
@ConditionalOnBean(MeterRegistry.class)
public class CounterRegistryHelper {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<CounterKey, Counter> cache = new ConcurrentHashMap<>();

    public CounterRegistryHelper(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Counter 조회 또는 등록. 캐시 hit 시 기존 인스턴스 반환, miss 시 register 후 캐시.
     *
     * @param name         메트릭 이름
     * @param description  설명 (첫 등록 시에만 반영)
     * @param tagKeyValues 태그 키-값 쌍 (가변, 짝수 개)
     */
    public Counter counter(String name, String description, String... tagKeyValues) {
        validateTagPairs(tagKeyValues);
        CounterKey key = CounterKey.of(name, tagKeyValues);
        return cache.computeIfAbsent(key, k -> register(name, description, tagKeyValues));
    }

    /** 편의 API — 조회와 동시에 1 증가. */
    public void increment(String name, String description, String... tagKeyValues) {
        counter(name, description, tagKeyValues).increment();
    }

    private Counter register(String name, String description, String[] tagKeyValues) {
        Counter.Builder builder = Counter.builder(name).description(description);
        for (int i = 0; i < tagKeyValues.length; i += 2) {
            builder = builder.tag(tagKeyValues[i], tagKeyValues[i + 1]);
        }
        return builder.register(meterRegistry);
    }

    private static void validateTagPairs(String[] tagKeyValues) {
        if (tagKeyValues.length % 2 != 0) {
            throw new InvalidCounterTagPairsException(tagKeyValues.length);
        }
    }

    /**
     * 캐시 키 — (name, tags) 조합. Tag 리스트는 key 순으로 정렬하여 동일 tag 셋이
     * 호출 순서와 무관하게 동일 키가 되도록 정규화.
     */
    private record CounterKey(String name, List<Tag> tags) {
        static CounterKey of(String name, String[] tagKeyValues) {
            List<Tag> tags = new ArrayList<>(tagKeyValues.length / 2);
            for (int i = 0; i < tagKeyValues.length; i += 2) {
                tags.add(Tag.of(tagKeyValues[i], tagKeyValues[i + 1]));
            }
            tags.sort(Comparator.comparing(Tag::getKey));
            return new CounterKey(name, List.copyOf(tags));
        }
    }
}
