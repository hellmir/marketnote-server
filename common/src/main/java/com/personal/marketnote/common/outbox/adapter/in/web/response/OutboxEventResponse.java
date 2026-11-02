package com.personal.marketnote.common.outbox.adapter.in.web.response;

import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;

import java.time.LocalDateTime;

public record OutboxEventResponse(
        Long id,
        String eventId,
        String topic,
        String partitionKey,
        String eventType,
        String source,
        int retryCount,
        int maxRetries,
        LocalDateTime createdAt,
        LocalDateTime failedAt,
        String lastErrorMessage
) {
    public static OutboxEventResponse from(OutboxEventJpaEntity entity) {
        return new OutboxEventResponse(
                entity.getId(),
                entity.getEventId(),
                entity.getTopic(),
                entity.getPartitionKey(),
                entity.getEventType(),
                entity.getSource(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getCreatedAt(),
                entity.getFailedAt(),
                entity.getLastErrorMessage()
        );
    }
}
