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
    @DisplayName("Consumer 레코드 헤더의 X-Request-ID를 MDC requestId로 설정한다")
    void putsHeaderTraceIdIntoMdc() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.REQUEST_ID_HEADER, "header-trace".getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY)).isEqualTo("header-trace");
    }

    @Test
    @DisplayName("헤더에 X-Request-ID가 없으면 신규 UUID를 MDC에 생성한다")
    void generatesNewUuidWhenHeaderAbsent() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");

        // when
        interceptor.intercept(record, consumer);

        // then
        String traceId = MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY);
        assertThat(traceId).isNotNull();
        assertThat(traceId.length()).isEqualTo(36);
    }

    @Test
    @DisplayName("afterRecord 콜백 실행 후 MDC requestId가 정리된다")
    void clearsMdcAfterAfterRecord() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.REQUEST_ID_HEADER, "trace-z".getBytes(StandardCharsets.UTF_8));
        interceptor.intercept(record, consumer);

        // when
        interceptor.afterRecord(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("헤더에 개행/제어문자가 포함되면 신규 UUID로 폴백한다")
    void fallsBackToUuidWhenHeaderContainsControlCharacters() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        String malicious = "abc\nERROR fake-log-injection";
        record.headers().add(TraceIdMdcFilter.REQUEST_ID_HEADER, malicious.getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        String traceId = MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY);
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
        record.headers().add(TraceIdMdcFilter.REQUEST_ID_HEADER, tooLong.getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        String traceId = MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY);
        assertThat(traceId).isNotNull();
        assertThat(traceId).isNotEqualTo(tooLong);
        assertThat(traceId.length()).isEqualTo(36);
    }

    @Test
    @DisplayName("X-Correlation-ID 헤더가 있으면 MDC correlationId에 주입된다")
    void putsHeaderCorrelationIdIntoMdc() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.CORRELATION_ID_HEADER, "corr-header".getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY)).isEqualTo("corr-header");
    }

    @Test
    @DisplayName("X-Causation-ID 헤더가 있으면 MDC causationId에 주입된다")
    void putsHeaderCausationIdIntoMdc() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.CAUSATION_ID_HEADER, "cause-header".getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY)).isEqualTo("cause-header");
    }

    @Test
    @DisplayName("X-Correlation-ID 헤더가 없으면 requestId 값으로 fallback된다")
    void fallsBackToTraceIdWhenCorrelationIdHeaderMissing() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.REQUEST_ID_HEADER, "trace-only".getBytes(StandardCharsets.UTF_8));

        // when
        interceptor.intercept(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY)).isEqualTo("trace-only");
    }

    @Test
    @DisplayName("X-Causation-ID 헤더가 없으면 MDC causationId는 set되지 않는다")
    void doesNotSetCausationIdWhenHeaderMissing() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");

        // when
        interceptor.intercept(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("afterRecord 호출 시 requestId/correlationId/causationId가 모두 remove된다")
    void clearsAllMdcKeysAfterAfterRecord() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.REQUEST_ID_HEADER, "t".getBytes(StandardCharsets.UTF_8));
        record.headers().add(TraceIdMdcFilter.CORRELATION_ID_HEADER, "c".getBytes(StandardCharsets.UTF_8));
        record.headers().add(TraceIdMdcFilter.CAUSATION_ID_HEADER, "u".getBytes(StandardCharsets.UTF_8));
        interceptor.intercept(record, consumer);

        // when
        interceptor.afterRecord(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY)).isNull();
        assertThat(MDC.get(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY)).isNull();
        assertThat(MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("X-Causation-ID 헤더 byte 배열이 비어있으면 MDC causationId는 set되지 않는다")
    void doesNotSetCausationIdWhenHeaderValueEmpty() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        record.headers().add(TraceIdMdcFilter.CAUSATION_ID_HEADER, new byte[0]);

        // when
        interceptor.intercept(record, consumer);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY)).isNull();
    }
}
