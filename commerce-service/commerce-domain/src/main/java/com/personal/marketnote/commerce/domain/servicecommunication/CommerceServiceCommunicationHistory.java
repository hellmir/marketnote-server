package com.personal.marketnote.commerce.domain.servicecommunication;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CommerceServiceCommunicationHistory {
    private Long id;
    private CommerceServiceCommunicationTargetType targetType;
    private String targetId;
    private CommerceServiceCommunicationType communicationType;
    private CommerceServiceCommunicationSenderType sender;
    private String exception;
    private String payload;
    private String payloadJson;
    private LocalDateTime createdAt;

    public static CommerceServiceCommunicationHistory from(CommerceServiceCommunicationHistoryCreateState state) {
        return CommerceServiceCommunicationHistory.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .build();
    }

    public static CommerceServiceCommunicationHistory from(CommerceServiceCommunicationHistorySnapshotState state) {
        return CommerceServiceCommunicationHistory.builder()
                .id(state.getId())
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
