package com.personal.marketnote.common.logging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KafkaMdcProducerInterceptor")
class KafkaMdcProducerInterceptorTest {

    private final KafkaMdcProducerInterceptor interceptor = new KafkaMdcProducerInterceptor();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("MDC requestId가 존재하면 Producer 레코드 헤더에 X-Request-ID를 추가한다")
    void addsTraceIdHeaderWhenMdcPresent() {
        // given
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "trace-abc");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.REQUEST_ID_HEADER);
        assertThat(header).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("trace-abc");
    }

    @Test
    @DisplayName("MDC requestId가 없으면 헤더를 추가하지 않는다")
    void noOpWhenMdcAbsent() {
        // given
        MDC.clear();
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        assertThat(result.headers().lastHeader(TraceIdMdcFilter.REQUEST_ID_HEADER)).isNull();
    }

    @Test
    @DisplayName("기존 X-Request-ID 헤더가 있으면 덮어쓰지 않는다")
    void doesNotOverwriteExistingHeader() {
        // given
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "new-trace");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");
        record.headers().add(TraceIdMdcFilter.REQUEST_ID_HEADER, "existing-trace".getBytes(StandardCharsets.UTF_8));

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.REQUEST_ID_HEADER);
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("existing-trace");
    }

    @Test
    @DisplayName("MDC에 correlationId가 있으면 X-Correlation-ID 헤더가 부착된다")
    void addsCorrelationIdHeaderWhenMdcPresent() {
        // given
        MDC.put(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY, "corr-1");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER);
        assertThat(header).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("corr-1");
    }

    @Test
    @DisplayName("MDC에 causationId가 있으면 X-Causation-ID 헤더가 부착된다")
    void addsCausationIdHeaderWhenMdcPresent() {
        // given
        MDC.put(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY, "cause-1");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.CAUSATION_ID_HEADER);
        assertThat(header).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("cause-1");
    }

    @Test
    @DisplayName("MDC에 모든 ID가 있으면 requestId/correlationId/causationId 세 헤더가 모두 부착된다")
    void addsAllThreeHeadersWhenAllMdcPresent() {
        // given
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "trace-x");
        MDC.put(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY, "corr-x");
        MDC.put(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY, "cause-x");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        assertThat(new String(result.headers().lastHeader(TraceIdMdcFilter.REQUEST_ID_HEADER).value(), StandardCharsets.UTF_8))
                .isEqualTo("trace-x");
        assertThat(new String(result.headers().lastHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER).value(), StandardCharsets.UTF_8))
                .isEqualTo("corr-x");
        assertThat(new String(result.headers().lastHeader(TraceIdMdcFilter.CAUSATION_ID_HEADER).value(), StandardCharsets.UTF_8))
                .isEqualTo("cause-x");
    }

    @Test
    @DisplayName("MDC에 correlationId가 없으면 X-Correlation-ID 헤더는 부착되지 않는다")
    void doesNotAddCorrelationIdWhenMdcAbsent() {
        // given
        MDC.clear();
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        assertThat(result.headers().lastHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER)).isNull();
    }

    @Test
    @DisplayName("MDC에 causationId가 없으면 X-Causation-ID 헤더는 부착되지 않는다")
    void doesNotAddCausationIdWhenMdcAbsent() {
        // given
        MDC.clear();
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        assertThat(result.headers().lastHeader(TraceIdMdcFilter.CAUSATION_ID_HEADER)).isNull();
    }

    @Test
    @DisplayName("기존 X-Correlation-ID 헤더가 있으면 덮어쓰지 않는다")
    void doesNotOverwriteExistingCorrelationIdHeader() {
        // given
        MDC.put(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY, "new-corr");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");
        record.headers().add(TraceIdMdcFilter.CORRELATION_ID_HEADER, "existing-corr".getBytes(StandardCharsets.UTF_8));

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER);
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("existing-corr");
    }

    @Test
    @DisplayName("기존 X-Causation-ID 헤더가 있으면 덮어쓰지 않는다")
    void doesNotOverwriteExistingCausationIdHeader() {
        // given
        MDC.put(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY, "new-cause");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");
        record.headers().add(TraceIdMdcFilter.CAUSATION_ID_HEADER, "existing-cause".getBytes(StandardCharsets.UTF_8));

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.CAUSATION_ID_HEADER);
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("existing-cause");
    }
}
