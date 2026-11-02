package com.personal.marketnote.common.adapter.in.web.kafka.response;

public record DltTopicSummaryResponse(
        String originalTopic,
        String dltTopic,
        long messageCount
) {
}
