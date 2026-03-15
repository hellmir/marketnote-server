package com.personal.marketnote.common.adapter.in.web.kafka.response;

import com.personal.marketnote.common.configuration.kafka.DltReprocessResult;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;

public record ReprocessDltResponse(
        String originalTopic,
        String dltTopic,
        int reprocessedCount,
        int failedCount
) {
    public static ReprocessDltResponse from(String originalTopic, DltReprocessResult result) {
        return new ReprocessDltResponse(
                originalTopic,
                originalTopic + KafkaTopicConstants.DLT_SUFFIX,
                result.reprocessedCount(),
                result.failedCount()
        );
    }
}
