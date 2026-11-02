package com.personal.marketnote.fulfillment.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentServiceCommunicationHistory {
    private Long id;
    private FulfillmentServiceCommunicationTargetType targetType;
    private String targetId;
    private FulfillmentServiceCommunicationType communicationType;
    private FulfillmentServiceCommunicationSenderType sender;
    private String exception;
    private String payload;
    private JsonNode payloadJson;
    private LocalDateTime createdAt;

    public static FulfillmentServiceCommunicationHistory from(FulfillmentServiceCommunicationHistoryCreateState state) {
        return FulfillmentServiceCommunicationHistory.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .build();
    }

    public static FulfillmentServiceCommunicationHistory from(FulfillmentServiceCommunicationHistorySnapshotState state) {
        return FulfillmentServiceCommunicationHistory.builder()
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
