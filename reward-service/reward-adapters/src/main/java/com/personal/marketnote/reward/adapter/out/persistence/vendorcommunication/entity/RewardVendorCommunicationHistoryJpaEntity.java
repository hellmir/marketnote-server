package com.personal.marketnote.reward.adapter.out.persistence.vendorcommunication.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.vendorcommunication.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_vendor_communication_history")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RewardVendorCommunicationHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 31)
    private RewardVendorCommunicationTargetType targetType;

    @Column(name = "target_id", length = 63)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_name", nullable = false, length = 31)
    private RewardVendorName vendorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "communication_type", nullable = false, length = 15)
    private RewardVendorCommunicationType communicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 15)
    private RewardVendorCommunicationSenderType sender;

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

    public static RewardVendorCommunicationHistoryJpaEntity from(RewardVendorCommunicationHistory history) {
        if (FormatValidator.hasNoValue(history)) {
            return null;
        }

        return RewardVendorCommunicationHistoryJpaEntity.builder()
                .id(history.getId())
                .targetType(history.getTargetType())
                .targetId(history.getTargetId())
                .vendorName(history.getVendorName())
                .communicationType(history.getCommunicationType())
                .sender(history.getSender())
                .exception(history.getException())
                .payload(history.getPayload())
                .payloadJson(history.getPayloadJson())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public RewardVendorCommunicationHistory toDomain() {
        return RewardVendorCommunicationHistory.from(
                RewardVendorCommunicationHistorySnapshotState.builder()
                        .id(id)
                        .targetType(targetType)
                        .targetId(targetId)
                        .vendorName(vendorName)
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
