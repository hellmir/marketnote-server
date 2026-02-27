package com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.commerce.domain.vendorcommunication.*;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "commerce_vendor_communication_history")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CommerceVendorCommunicationHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 31)
    private CommerceVendorCommunicationTargetType targetType;

    @Column(name = "target_id", length = 63)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_name", nullable = false, length = 31)
    private CommerceVendorName vendorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "communication_type", nullable = false, length = 15)
    private CommerceVendorCommunicationType communicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 15)
    private CommerceVendorCommunicationSenderType sender;

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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static CommerceVendorCommunicationHistoryJpaEntity from(CommerceVendorCommunicationHistory history) {
        return CommerceVendorCommunicationHistoryJpaEntity.builder()
                .id(history.getId())
                .targetType(history.getTargetType())
                .targetId(history.getTargetId())
                .vendorName(history.getVendorName())
                .communicationType(history.getCommunicationType())
                .sender(history.getSender())
                .exception(history.getException())
                .payload(history.getPayload())
                .payloadJson(parseJsonNode(history.getPayloadJson()))
                .createdAt(history.getCreatedAt())
                .build();
    }

    public CommerceVendorCommunicationHistory toDomain() {
        return CommerceVendorCommunicationHistory.from(
                CommerceVendorCommunicationHistorySnapshotState.builder()
                        .id(id)
                        .targetType(targetType)
                        .targetId(targetId)
                        .vendorName(vendorName)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(FormatValidator.hasValue(payloadJson) ? payloadJson.toString() : null)
                        .createdAt(createdAt)
                        .build()
        );
    }

    private static JsonNode parseJsonNode(String jsonString) {
        if (FormatValidator.hasNoValue(jsonString)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(jsonString);
        } catch (Exception e) {
            return null;
        }
    }
}
