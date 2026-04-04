package com.personal.marketnote.common.outbox.entity;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.exception.InvalidOutboxEventStatusTransitionException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "outbox_event",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_outbox_event_id",
                columnNames = {"event_id"}
        ),
        indexes = @Index(
                name = "idx_outbox_status_created",
                columnList = "status, created_at"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OutboxEventJpaEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "partition_key", nullable = false, length = 100)
    private String partitionKey;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Column(name = "discard_reason", length = 500)
    private String discardReason;

    @Column(name = "discarded_at")
    private LocalDateTime discardedAt;

    private OutboxEventJpaEntity(String eventId, String topic, String partitionKey,
                                 String eventType, String source, String payload,
                                 OutboxEventStatus status, int retryCount, int maxRetries,
                                 LocalDateTime createdAt) {
        this.eventId = eventId;
        this.topic = topic;
        this.partitionKey = partitionKey;
        this.eventType = eventType;
        this.source = source;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.createdAt = createdAt;
    }

    public static OutboxEventJpaEntity from(com.personal.marketnote.common.outbox.OutboxEvent event) {
        return new OutboxEventJpaEntity(
                event.getEventId(),
                event.getTopic(),
                event.getPartitionKey(),
                event.getEventType(),
                event.getSource(),
                event.getPayload(),
                event.getStatus(),
                event.getRetryCount(),
                event.getMaxRetries(),
                event.getCreatedAt()
        );
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = publishedAt;
    }

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    public void incrementRetry(String errorMessage, LocalDateTime now) {
        this.retryCount++;
        this.lastErrorMessage = truncate(errorMessage, MAX_ERROR_MESSAGE_LENGTH);
        if (this.retryCount >= this.maxRetries) {
            this.status = OutboxEventStatus.FAILED;
            this.failedAt = now;
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

    public void discard(String reason, LocalDateTime discardedAt) {
        if (!this.status.isFailed()) {
            throw new InvalidOutboxEventStatusTransitionException(this.status);
        }
        this.status = OutboxEventStatus.DISCARDED;
        this.discardReason = reason;
        this.discardedAt = discardedAt;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
