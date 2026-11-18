package com.personal.marketnote.common.logging;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("KafkaMdcRecordInterceptor")
class KafkaMdcRecordInterceptorTest {

    private final KafkaMdcRecordInterceptor interceptor = new KafkaMdcRecordInterceptor();

    @SuppressWarnings("unchecked")
    private final Consumer<String, Object> consumer = (Consumer<String, Object>) mock(Consumer.class);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Consumer 레코드 헤더의 X-Request-ID를 MDC traceId로 설정한다")
    void putsHeaderTraceIdIntoMdc() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.TRACE_ID_HEADER, "header-trace".getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY)).isEqualTo("header-trace");
    }

    @Test
    @DisplayName("헤더에 X-Request-ID가 없으면 신규 UUID를 MDC에 생성한다")
    void generatesNewUuidWhenHeaderAbsent() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");

        // when
        interceptor.intercept(record, consumer);

        // then
        String traceId = MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY);
        assertThat(traceId).isNotNull();
        assertThat(traceId.length()).isEqualTo(36);
    }

    @Test
    @DisplayName("afterRecord 콜백 실행 후 MDC traceId가 정리된다")
    void clearsMdcAfterAfterRecord() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.TRACE_ID_HEADER, "trace-z".getBytes(StandardCharsets.UTF_8));
        interceptor.intercept(record, consumer);

        // when
        interceptor.afterRecord(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("헤더에 개행/제어문자가 포함되면 신규 UUID로 폴백한다")
    void fallsBackToUuidWhenHeaderContainsControlCharacters() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        String malicious = "abc\nERROR fake-log-injection";
        record.headers().add(TraceIdMdcFilter.TRACE_ID_HEADER, malicious.getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        String traceId = MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY);
        assertThat(traceId).isNotNull();
        assertThat(traceId).isNotEqualTo(malicious);
        assertThat(traceId.length()).isEqualTo(36);
    }

    @Test
    @DisplayName("헤더 길이가 65자 이상이면 신규 UUID로 폴백한다")
    void fallsBackToUuidWhenHeaderExceedsMaxLength() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        String tooLong = "a".repeat(65);
        record.headers().add(TraceIdMdcFilter.TRACE_ID_HEADER, tooLong.getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        String traceId = MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY);
        assertThat(traceId).isNotNull();
        assertThat(traceId).isNotEqualTo(tooLong);
        assertThat(traceId.length()).isEqualTo(36);
    }
}
