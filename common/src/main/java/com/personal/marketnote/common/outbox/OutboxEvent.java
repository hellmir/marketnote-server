package com.personal.marketnote.common.outbox;

import com.personal.marketnote.common.outbox.exception.InvalidOutboxEventStatusTransitionException;
import lombok.*;

import java.time.Clock;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OutboxEvent {
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private Long id;
    private String eventId;
    private String topic;
    private String partitionKey;
    private String eventType;
    private String source;
    private String payload;
    private OutboxEventStatus status;
    private int retryCount;
    private int maxRetries;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime failedAt;
    private String lastErrorMessage;
    private String discardReason;
    private LocalDateTime discardedAt;

    public static OutboxEvent of(String eventId, String topic, String partitionKey,
                                 String eventType, String source, String payload,
                                 Clock clock) {
        return OutboxEvent.builder()
                .eventId(eventId)
                .topic(topic)
                .partitionKey(partitionKey)
                .eventType(eventType)
                .source(source)
                .payload(payload)
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .maxRetries(DEFAULT_MAX_RETRIES)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }

    public void markPublished(Clock clock) {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now(clock);
    }

    public void incrementRetry(String errorMessage, Clock clock) {
        this.retryCount++;
        this.lastErrorMessage = truncate(errorMessage, MAX_ERROR_MESSAGE_LENGTH);
        if (this.retryCount >= this.maxRetries) {
            this.status = OutboxEventStatus.FAILED;
            this.failedAt = LocalDateTime.now(clock);
        }
    }

    public void resetForRetry() {
        if (!this.status.isFailed()) {
            throw new InvalidOutboxEventStatusTransitionException(this.status);
        }
        this.status = OutboxEventStatus.PENDING;
        this.retryCount = 0;
        this.failedAt = null;
        this.lastErrorMessage = null;
    }

    public void discard(String reason, Clock clock) {
        if (!this.status.isFailed()) {
            throw new InvalidOutboxEventStatusTransitionException(this.status);
        }
        this.status = OutboxEventStatus.DISCARDED;
        this.discardReason = reason;
        this.discardedAt = LocalDateTime.now(clock);
    }

    public boolean isExhausted() {
        return this.retryCount >= this.maxRetries;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
