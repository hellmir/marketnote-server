package com.personal.marketnote.common.configuration.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DltMetricsCollector 테스트")
class DltMetricsCollectorTest {

    @Test
    @DisplayName("DLT 메시지 카운터를 증가시킨다")
    void incrementDltMessageCount_incrementsCounter() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        DltMetricsCollector collector = new DltMetricsCollector(meterRegistry);

        // when
        collector.incrementDltMessageCount("commerce.order.payment-completed");
        collector.incrementDltMessageCount("commerce.order.payment-completed");

        // then
        Counter counter = meterRegistry.find("kafka.dlt.messages.total")
                .tag("original_topic", "commerce.order.payment-completed")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("서로 다른 토픽의 DLT 카운터를 독립적으로 관리한다")
    void incrementDltMessageCount_differentTopics_independentCounters() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        DltMetricsCollector collector = new DltMetricsCollector(meterRegistry);

        // when
        collector.incrementDltMessageCount("commerce.order.payment-completed");
        collector.incrementDltMessageCount("commerce.payment.approved");
        collector.incrementDltMessageCount("commerce.payment.approved");

        // then
        Counter orderCounter = meterRegistry.find("kafka.dlt.messages.total")
                .tag("original_topic", "commerce.order.payment-completed")
                .counter();
        Counter paymentCounter = meterRegistry.find("kafka.dlt.messages.total")
                .tag("original_topic", "commerce.payment.approved")
                .counter();

        assertThat(orderCounter).isNotNull();
        assertThat(orderCounter.count()).isEqualTo(1.0);
        assertThat(paymentCounter).isNotNull();
        assertThat(paymentCounter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("재처리 성공 카운터를 증가시킨다")
    void incrementDltReprocessCount_success_incrementsCounter() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        DltMetricsCollector collector = new DltMetricsCollector(meterRegistry);

        // when
        collector.incrementDltReprocessCount("commerce.order.payment-completed", "success");

        // then
        Counter counter = meterRegistry.find("kafka.dlt.reprocess.total")
                .tag("original_topic", "commerce.order.payment-completed")
                .tag("outcome", "success")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("재처리 실패 카운터를 증가시킨다")
    void incrementDltReprocessCount_failure_incrementsCounter() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        DltMetricsCollector collector = new DltMetricsCollector(meterRegistry);

        // when
        collector.incrementDltReprocessCount("commerce.payment.cancelled", "failure");

        // then
        Counter counter = meterRegistry.find("kafka.dlt.reprocess.total")
                .tag("original_topic", "commerce.payment.cancelled")
                .tag("outcome", "failure")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("MeterRegistry가 null이면 카운터를 증가시키지 않는다")
    void incrementDltMessageCount_nullMeterRegistry_noOp() {
        // given
        DltMetricsCollector collector = new DltMetricsCollector(null);

        // when & then (예외 발생하지 않음)
        collector.incrementDltMessageCount("commerce.order.payment-completed");
        collector.incrementDltReprocessCount("commerce.order.payment-completed", "success");
        collector.incrementDltResolveCount("commerce.order.payment-completed", "retry");
    }

    @Test
    @DisplayName("DLT 메시지 해결 retry 카운터를 증가시킨다")
    void incrementDltResolveCount_retry_incrementsCounter() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        DltMetricsCollector collector = new DltMetricsCollector(meterRegistry);

        // when
        collector.incrementDltResolveCount("commerce.order.payment-completed", "retry");

        // then
        Counter counter = meterRegistry.find("kafka.dlt.resolve.total")
                .tag("original_topic", "commerce.order.payment-completed")
                .tag("action", "retry")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("DLT 메시지 해결 discard 카운터를 증가시킨다")
    void incrementDltResolveCount_discard_incrementsCounter() {
        // given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        DltMetricsCollector collector = new DltMetricsCollector(meterRegistry);

        // when
        collector.incrementDltResolveCount("commerce.payment.cancelled", "discard");

        // then
        Counter counter = meterRegistry.find("kafka.dlt.resolve.total")
                .tag("original_topic", "commerce.payment.cancelled")
                .tag("action", "discard")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}
