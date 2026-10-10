package com.personal.marketnote.notification.domain.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class NotificationVendorCommunicationHistory {
    private Long id;
    private NotificationVendorCommunicationTargetType targetType;
    private String targetId;
    private NotificationVendorName vendorName;
    private NotificationVendorCommunicationType communicationType;
    private NotificationVendorCommunicationSenderType sender;
    private String exception;
    private String payload;
    private JsonNode payloadJson;
    private LocalDateTime createdAt;

    public static NotificationVendorCommunicationHistory from(NotificationVendorCommunicationHistoryCreateState state) {
        return NotificationVendorCommunicationHistory.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .vendorName(state.getVendorName())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .build();
    }

    public static NotificationVendorCommunicationHistory from(NotificationVendorCommunicationHistorySnapshotState state) {
        return NotificationVendorCommunicationHistory.builder()
                .id(state.getId())
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .vendorName(state.getVendorName())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .createdAt(state.getCreatedAt())
                .build();
    }

    public boolean isSuccess() {
        return FormatValidator.hasNoValue(exception);
    }

    public boolean isFailure() {
        return FormatValidator.hasValue(exception);
    }
}
