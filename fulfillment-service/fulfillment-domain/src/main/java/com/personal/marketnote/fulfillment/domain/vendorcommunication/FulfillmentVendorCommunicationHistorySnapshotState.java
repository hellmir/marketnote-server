package com.personal.marketnote.fulfillment.domain.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FulfillmentVendorCommunicationHistorySnapshotState {
    private final Long id;
    private final FulfillmentVendorCommunicationTargetType targetType;
    private final String targetId;
    private final FulfillmentVendorName vendorName;
    private final FulfillmentVendorCommunicationType communicationType;
    private final FulfillmentVendorCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
    private final LocalDateTime createdAt;
}
