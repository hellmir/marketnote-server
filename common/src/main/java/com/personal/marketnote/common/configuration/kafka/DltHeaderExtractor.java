package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.utility.FormatValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;

public final class DltHeaderExtractor {

    private static final String HEADER_ORIGINAL_TOPIC = "kafka_dlt-original-topic";
    private static final String HEADER_EXCEPTION_FQCN = "kafka_dlt-exception-fqcn";
    private static final String HEADER_EXCEPTION_MESSAGE = "kafka_dlt-exception-message";
    private static final String UNKNOWN = "UNKNOWN";

    private DltHeaderExtractor() {
    }

    public static String extractOriginalTopic(ConsumerRecord<?, ?> record) {
        return extractHeader(record, HEADER_ORIGINAL_TOPIC);
    }

    public static String extractExceptionFqcn(ConsumerRecord<?, ?> record) {
        return extractHeader(record, HEADER_EXCEPTION_FQCN);
    }

    public static String extractExceptionMessage(ConsumerRecord<?, ?> record) {
        return extractHeader(record, HEADER_EXCEPTION_MESSAGE);
    }

    private static String extractHeader(ConsumerRecord<?, ?> record, String headerKey) {
        Header header = record.headers().lastHeader(headerKey);
        if (FormatValidator.hasNoValue(header)) {
            return UNKNOWN;
        }
        byte[] value = header.value();
        if (FormatValidator.hasNoValue(value)) {
            return UNKNOWN;
        }
        return new String(value, StandardCharsets.UTF_8);
    }
}
