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
        String traceId = MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY);
        if (FormatValidator.hasNoValue(traceId)) {
            return record;
        }
        if (FormatValidator.isNotNull(record.headers().lastHeader(TraceIdMdcFilter.TRACE_ID_HEADER))) {
            return record;
        }
        record.headers().add(TraceIdMdcFilter.TRACE_ID_HEADER, traceId.getBytes(StandardCharsets.UTF_8));
        return record;
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
