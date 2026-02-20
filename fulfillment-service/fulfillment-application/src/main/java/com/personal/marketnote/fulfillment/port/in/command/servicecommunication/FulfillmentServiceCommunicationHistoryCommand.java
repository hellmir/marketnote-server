package com.personal.marketnote.fulfillment.port.in.command.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationType;
import lombok.Builder;

@Builder
public record FulfillmentServiceCommunicationHistoryCommand(
        FulfillmentServiceCommunicationTargetType targetType,
        String targetId,
        FulfillmentServiceCommunicationType communicationType,
        FulfillmentServiceCommunicationSenderType sender,
        String exception,
        String payload,
        JsonNode payloadJson
) {
}
