package com.personal.marketnote.commerce.domain.vendorcommunication;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CommerceVendorCommunicationHistory {
    private Long id;
    private CommerceVendorCommunicationTargetType targetType;
    private String targetId;
    private CommerceVendorName vendorName;
    private CommerceVendorCommunicationType communicationType;
    private CommerceVendorCommunicationSenderType sender;
    private String exception;
    private String payload;
    private String payloadJson;
    private LocalDateTime createdAt;

    public static CommerceVendorCommunicationHistory from(CommerceVendorCommunicationHistoryCreateState state) {
        return CommerceVendorCommunicationHistory.builder()
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

    public static CommerceVendorCommunicationHistory from(CommerceVendorCommunicationHistorySnapshotState state) {
        return CommerceVendorCommunicationHistory.builder()
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
}
