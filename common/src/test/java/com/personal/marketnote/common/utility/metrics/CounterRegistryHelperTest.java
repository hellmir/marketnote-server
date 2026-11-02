package com.personal.marketnote.common.utility.metrics;

import com.personal.marketnote.common.domain.exception.illegalstate.InvalidCounterTagPairsException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CounterRegistryHelper 테스트")
class CounterRegistryHelperTest {

    private MeterRegistry meterRegistry;
    private CounterRegistryHelper helper;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        helper = new CounterRegistryHelper(meterRegistry);
    }

    @Test
    @DisplayName("동일 name+tags로 반복 호출 시 캐시된 Counter 인스턴스를 재사용한다")
    void shouldReuseCachedCounterForSameNameAndTags() {
        // when
        Counter first = helper.counter("metric.test.total", "테스트 메트릭", "tag1", "value1");
        Counter second = helper.counter("metric.test.total", "테스트 메트릭", "tag1", "value1");

        // then
        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("동일 name이지만 다른 tags면 별도 Counter 인스턴스를 반환한다")
    void shouldReturnDifferentCounterForDifferentTags() {
        // when
        Counter counterA = helper.counter("metric.test.total", "테스트 메트릭", "tag1", "valueA");
        Counter counterB = helper.counter("metric.test.total", "테스트 메트릭", "tag1", "valueB");

        // then
        assertThat(counterA).isNotSameAs(counterB);
    }

    @Test
    @DisplayName("tag 순서가 달라도 동일 키로 캐시 hit된다")
    void shouldHitCacheRegardlessOfTagOrder() {
        // when
        Counter first = helper.counter(
                "metric.test.total", "테스트 메트릭", "tagA", "1", "tagB", "2"
        );
        Counter second = helper.counter(
                "metric.test.total", "테스트 메트릭", "tagB", "2", "tagA", "1"
        );

        // then
        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("홀수 개의 tagKeyValues는 InvalidCounterTagPairsException을 던진다")
    void shouldThrowWhenTagKeyValuesAreOddLength() {
        // when & then
        assertThatThrownBy(
                () -> helper.counter("metric.test.total", "테스트 메트릭", "tag1")
        ).isInstanceOf(InvalidCounterTagPairsException.class);
    }

    @Test
    @DisplayName("increment는 counter를 조회 후 1 증가한다")
    void shouldIncrementCounterByOne() {
        // when
        helper.increment("metric.test.total", "테스트 메트릭", "tag1", "value1");
        helper.increment("metric.test.total", "테스트 메트릭", "tag1", "value1");

        // then
        Counter counter = meterRegistry.find("metric.test.total")
                .tag("tag1", "value1")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("태그 없이 counter를 등록할 수 있다")
    void shouldRegisterCounterWithoutTags() {
        // when
        helper.increment("metric.no_tag.total", "무태그 메트릭");

        // then
        Counter counter = meterRegistry.find("metric.no_tag.total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("description은 첫 등록 시에만 적용되며 캐시에서 동일 인스턴스가 사용된다")
    void shouldKeepFirstDescriptionOnRepeatedCalls() {
        // when
        Counter first = helper.counter("metric.desc.total", "최초 설명", "tag", "value");
        Counter second = helper.counter("metric.desc.total", "변경 시도 설명", "tag", "value");

        // then
        assertThat(first).isSameAs(second);
        assertThat(first.getId().getDescription()).isEqualTo("최초 설명");
    }
}
