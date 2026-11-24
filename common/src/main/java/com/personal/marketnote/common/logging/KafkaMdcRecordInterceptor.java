package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaMdcRecordInterceptor implements RecordInterceptor<String, Object> {

    // Spring Kafka 인터페이스 시그니처는 @Nullable 반환을 허용하나 본 구현은 항상 non-null record를 반환한다 (MDC 전파만 수행, 레코드 변경 없음).
    @Override
    public ConsumerRecord<String, Object> intercept(
            @NonNull ConsumerRecord<String, Object> record, @NonNull Consumer<String, Object> consumer
    ) {
        String requestId = TraceIdValidator.validateOrFallback(
                resolveHeaderValue(record, TraceIdMdcFilter.REQUEST_ID_HEADER)
        );
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, requestId);

        String correlationCandidate = resolveHeaderValue(record, TraceIdMdcFilter.CORRELATION_ID_HEADER);
        String correlationId = resolveCorrelationId(correlationCandidate, requestId);
        MDC.put(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY, correlationId);

        String causationId = resolveOptionalId(
                resolveHeaderValue(record, TraceIdMdcFilter.CAUSATION_ID_HEADER)
        );
        if (FormatValidator.hasValue(causationId)) {
            MDC.put(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY, causationId);
        }
        return record;
    }

    @Override
    public void afterRecord(
            @NonNull ConsumerRecord<String, Object> record, @NonNull Consumer<String, Object> consumer
    ) {
        MDC.remove(TraceIdMdcFilter.REQUEST_ID_MDC_KEY);
        MDC.remove(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY);
        MDC.remove(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY);
    }

    private static String resolveHeaderValue(ConsumerRecord<String, Object> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (FormatValidator.isNull(header)) {
            return null;
        }
        byte[] value = header.value();
        if (FormatValidator.isNull(value) || value.length == 0) {
            return null;
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    private static String resolveCorrelationId(String candidate, String requestId) {
        if (FormatValidator.hasNoValue(candidate)) {
            return requestId;
        }
        if (!TraceIdValidator.TRACE_ID_PATTERN.matcher(candidate).matches()) {
            return requestId;
        }
        return candidate;
    }

    private static String resolveOptionalId(String candidate) {
        if (FormatValidator.hasNoValue(candidate)) {
            return null;
        }
        if (!TraceIdValidator.TRACE_ID_PATTERN.matcher(candidate).matches()) {
            return null;
        }
        return candidate;
    }
}
