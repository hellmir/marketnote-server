package com.personal.marketnote.reward.adapter.out.persistence.servicecommunication.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.reward.domain.servicecommunication.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_service_communication_failure_history")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RewardServiceCommunicationHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 31)
    private RewardServiceCommunicationTargetType targetType;

    @Column(name = "target_id", length = 63)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "communication_type", nullable = false, length = 15)
    private RewardServiceCommunicationType communicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 15)
    private RewardServiceCommunicationSenderType sender;

    @Column(name = "exception", length = 63)
    private String exception;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private JsonNode payloadJson;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    public static RewardServiceCommunicationHistoryJpaEntity from(RewardServiceCommunicationHistory history) {
        return RewardServiceCommunicationHistoryJpaEntity.builder()
                .id(history.getId())
                .targetType(history.getTargetType())
                .targetId(history.getTargetId())
                .communicationType(history.getCommunicationType())
                .sender(history.getSender())
                .exception(history.getException())
                .payload(history.getPayload())
                .payloadJson(history.getPayloadJson())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public RewardServiceCommunicationHistory toDomain() {
        return RewardServiceCommunicationHistory.from(
                RewardServiceCommunicationHistorySnapshotState.builder()
                        .id(id)
                        .targetType(targetType)
                        .targetId(targetId)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(payloadJson)
                        .createdAt(createdAt)
                        .build()
        );
    }
}
