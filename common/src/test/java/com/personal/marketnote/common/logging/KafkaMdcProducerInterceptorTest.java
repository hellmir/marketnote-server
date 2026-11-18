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
    @DisplayName("MDC traceId가 존재하면 Producer 레코드 헤더에 X-Request-ID를 추가한다")
    void addsTraceIdHeaderWhenMdcPresent() {
        // given
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, "trace-abc");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.TRACE_ID_HEADER);
        assertThat(header).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("trace-abc");
    }

    @Test
    @DisplayName("MDC traceId가 없으면 헤더를 추가하지 않는다")
    void noOpWhenMdcAbsent() {
        // given
        MDC.clear();
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        assertThat(result.headers().lastHeader(TraceIdMdcFilter.TRACE_ID_HEADER)).isNull();
    }

    @Test
    @DisplayName("기존 X-Request-ID 헤더가 있으면 덮어쓰지 않는다")
    void doesNotOverwriteExistingHeader() {
        // given
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, "new-trace");
        ProducerRecord<String, Object> record = new ProducerRecord<>("topic", "key", "value");
        record.headers().add(TraceIdMdcFilter.TRACE_ID_HEADER, "existing-trace".getBytes(StandardCharsets.UTF_8));

        // when
        ProducerRecord<String, Object> result = interceptor.onSend(record);

        // then
        Header header = result.headers().lastHeader(TraceIdMdcFilter.TRACE_ID_HEADER);
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("existing-trace");
    }
}
