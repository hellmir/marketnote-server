package com.personal.marketnote.fulfillment.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FulfillmentServiceCommunicationHistorySnapshotState {
    private final Long id;
    private final FulfillmentServiceCommunicationTargetType targetType;
    private final String targetId;
    private final FulfillmentServiceCommunicationType communicationType;
    private final FulfillmentServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
    private final LocalDateTime createdAt;
}
