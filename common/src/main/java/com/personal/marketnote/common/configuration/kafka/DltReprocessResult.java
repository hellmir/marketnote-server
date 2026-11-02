package com.personal.marketnote.common.configuration.kafka;

public record DltReprocessResult(
        int reprocessedCount,
        int failedCount,
        int skippedCount
) {
}
