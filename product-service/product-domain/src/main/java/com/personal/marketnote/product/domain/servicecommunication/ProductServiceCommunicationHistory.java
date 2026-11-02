package com.personal.marketnote.product.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ProductServiceCommunicationHistory {
    private Long id;
    private ProductServiceCommunicationTargetType targetType;
    private String targetId;
    private ProductServiceCommunicationType communicationType;
    private ProductServiceCommunicationSenderType sender;
    private String exception;
    private String payload;
    private JsonNode payloadJson;
    private LocalDateTime createdAt;

    public static ProductServiceCommunicationHistory from(ProductServiceCommunicationHistoryCreateState state) {
        return ProductServiceCommunicationHistory.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .build();
    }

    public static ProductServiceCommunicationHistory from(ProductServiceCommunicationHistorySnapshotState state) {
        return ProductServiceCommunicationHistory.builder()
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
