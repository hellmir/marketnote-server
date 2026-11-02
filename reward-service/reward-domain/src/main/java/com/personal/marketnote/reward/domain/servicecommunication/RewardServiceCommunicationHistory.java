package com.personal.marketnote.reward.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RewardServiceCommunicationHistory {
    private Long id;
    private RewardServiceCommunicationTargetType targetType;
    private String targetId;
    private RewardServiceCommunicationType communicationType;
    private RewardServiceCommunicationSenderType sender;
    private String exception;
    private String payload;
    private JsonNode payloadJson;
    private LocalDateTime createdAt;

    public static RewardServiceCommunicationHistory from(RewardServiceCommunicationHistoryCreateState state) {
        return RewardServiceCommunicationHistory.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .build();
    }

    public static RewardServiceCommunicationHistory from(RewardServiceCommunicationHistorySnapshotState state) {
        return RewardServiceCommunicationHistory.builder()
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
