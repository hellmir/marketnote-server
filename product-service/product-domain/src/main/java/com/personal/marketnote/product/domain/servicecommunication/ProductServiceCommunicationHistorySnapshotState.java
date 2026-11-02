package com.personal.marketnote.product.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductServiceCommunicationHistorySnapshotState {
    private final Long id;
    private final ProductServiceCommunicationTargetType targetType;
    private final String targetId;
    private final ProductServiceCommunicationType communicationType;
    private final ProductServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
    private final LocalDateTime createdAt;
}
