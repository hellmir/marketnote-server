package com.personal.marketnote.reward.domain.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RewardVendorCommunicationHistory {
    private Long id;
    private RewardVendorCommunicationTargetType targetType;
    private String targetId;
    private RewardVendorName vendorName;
    private RewardVendorCommunicationType communicationType;
    private RewardVendorCommunicationSenderType sender;
    private String exception;
    private String payload;
    private JsonNode payloadJson;
    private LocalDateTime createdAt;

    public static RewardVendorCommunicationHistory from(RewardVendorCommunicationHistoryCreateState state) {
        return RewardVendorCommunicationHistory.builder()
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

    public static RewardVendorCommunicationHistory from(RewardVendorCommunicationHistorySnapshotState state) {
        return RewardVendorCommunicationHistory.builder()
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
