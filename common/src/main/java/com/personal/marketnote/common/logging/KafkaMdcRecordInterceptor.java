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
        String traceId = resolveTraceId(record);
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, traceId);
        return record;
    }

    @Override
    public void afterRecord(
            @NonNull ConsumerRecord<String, Object> record, @NonNull Consumer<String, Object> consumer
    ) {
        MDC.remove(TraceIdMdcFilter.TRACE_ID_MDC_KEY);
    }

    private static String resolveTraceId(ConsumerRecord<String, Object> record) {
        Header header = record.headers().lastHeader(TraceIdMdcFilter.TRACE_ID_HEADER);
        if (FormatValidator.isNull(header)) {
            return TraceIdValidator.validateOrFallback(null);
        }
        byte[] value = header.value();
        if (FormatValidator.isNull(value) || value.length == 0) {
            return TraceIdValidator.validateOrFallback(null);
        }
        String headerString = new String(value, StandardCharsets.UTF_8);
        return TraceIdValidator.validateOrFallback(headerString);
    }
}
