package com.personal.marketnote.common.outbox.adapter.in.web.response;

public record OutboxTopicSummaryResponse(
        String topic,
        long failedCount
) {
}
