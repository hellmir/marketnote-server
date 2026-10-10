package com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistory;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistorySnapshotState;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_vendor_communication_history")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class NotificationVendorCommunicationHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 31)
    private NotificationVendorCommunicationTargetType targetType;

    @Column(name = "target_id", length = 63)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_name", nullable = false, length = 31)
    private NotificationVendorName vendorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "communication_type", nullable = false, length = 15)
    private NotificationVendorCommunicationType communicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 15)
    private NotificationVendorCommunicationSenderType sender;

    @Column(name = "exception", columnDefinition = "TEXT")
    private String exception;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private JsonNode payloadJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static NotificationVendorCommunicationHistoryJpaEntity from(NotificationVendorCommunicationHistory history) {
        if (FormatValidator.hasNoValue(history)) {
            return null;
        }

        return NotificationVendorCommunicationHistoryJpaEntity.builder()
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

    public NotificationVendorCommunicationHistory toDomain() {
        return NotificationVendorCommunicationHistory.from(
                NotificationVendorCommunicationHistorySnapshotState.builder()
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
