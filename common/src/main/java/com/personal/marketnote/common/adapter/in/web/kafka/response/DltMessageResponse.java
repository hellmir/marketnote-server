package com.personal.marketnote.common.adapter.in.web.kafka.response;

import com.personal.marketnote.common.configuration.kafka.DltHeaderExtractor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public record DltMessageResponse(
        String dltTopic,
        int partition,
        long offset,
        String key,
        String originalTopic,
        String errorFqcn,
        String errorMessage,
        long timestamp,
        String resolution
) {
    private static final int MAX_ERROR_MESSAGE_LENGTH = 200;

    public static DltMessageResponse from(ConsumerRecord<String, Object> record, String resolution) {
        String fqcn = DltHeaderExtractor.extractExceptionFqcn(record);
        String simpleName = fqcn.contains(".") ? fqcn.substring(fqcn.lastIndexOf('.') + 1) : fqcn;
        String message = DltHeaderExtractor.extractExceptionMessage(record);
        String truncatedMessage = message.length() > MAX_ERROR_MESSAGE_LENGTH
                ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "..."
                : message;

        return new DltMessageResponse(
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                DltHeaderExtractor.extractOriginalTopic(record),
                simpleName,
                truncatedMessage,
                record.timestamp(),
                resolution
        );
    }
}
