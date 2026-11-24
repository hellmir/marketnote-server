package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class KafkaMdcProducerInterceptor implements ProducerInterceptor<String, Object> {

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
        attachHeaderIfAbsent(record, TraceIdMdcFilter.TRACE_ID_HEADER, MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY));
        attachHeaderIfAbsent(
                record, TraceIdMdcFilter.CORRELATION_ID_HEADER, MDC.get(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY)
        );
        attachHeaderIfAbsent(
                record, TraceIdMdcFilter.CAUSATION_ID_HEADER, MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY)
        );
        return record;
    }

    private static void attachHeaderIfAbsent(ProducerRecord<String, Object> record, String headerName, String value) {
        if (FormatValidator.hasNoValue(value)) {
            return;
        }
        if (FormatValidator.isNotNull(record.headers().lastHeader(headerName))) {
            return;
        }
        record.headers().add(headerName, value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // no-op
    }
}
