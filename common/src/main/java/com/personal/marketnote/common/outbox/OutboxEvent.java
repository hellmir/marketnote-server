package com.personal.marketnote.common.outbox;

import lombok.*;

import java.time.Clock;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OutboxEvent {
    private static final int DEFAULT_MAX_RETRIES = 5;

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

    public void incrementRetry() {
        this.retryCount++;
        if (this.retryCount >= this.maxRetries) {
            this.status = OutboxEventStatus.FAILED;
        }
    }

    public boolean isExhausted() {
        return this.retryCount >= this.maxRetries;
    }
}
