package com.personal.marketnote.common.outbox.adapter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("OutboxMetricsCollector 테스트")
class OutboxMetricsCollectorTest {

    @Test
    @DisplayName("FAILED 전환 시 outbox.events.failed.total 카운터가 증가한다")
    void incrementFailedCount_incrementsCounter() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        OutboxMetricsCollector collector = new OutboxMetricsCollector(meterRegistry);

        // when
        collector.incrementFailedCount("commerce.payment.approved");
        collector.incrementFailedCount("commerce.payment.approved");

        // then
        Counter counter = meterRegistry.find("outbox.events.failed.total")
                .tag("topic", "commerce.payment.approved")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("resolve 시 outbox.events.resolved.total 카운터가 증가한다")
    void incrementResolvedCount_incrementsCounter() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        OutboxMetricsCollector collector = new OutboxMetricsCollector(meterRegistry);

        // when
        collector.incrementResolvedCount("commerce.payment.approved", "retry");

        // then
        Counter counter = meterRegistry.find("outbox.events.resolved.total")
                .tag("topic", "commerce.payment.approved")
                .tag("action", "retry")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("MeterRegistry가 null이어도 예외가 발생하지 않는다")
    void nullMeterRegistry_noException() {
        // given
        OutboxMetricsCollector collector = new OutboxMetricsCollector(null);

        // when & then
        assertThatCode(() -> {
            collector.incrementFailedCount("commerce.payment.approved");
            collector.incrementResolvedCount("commerce.payment.approved", "retry");
        }).doesNotThrowAnyException();
    }
}
