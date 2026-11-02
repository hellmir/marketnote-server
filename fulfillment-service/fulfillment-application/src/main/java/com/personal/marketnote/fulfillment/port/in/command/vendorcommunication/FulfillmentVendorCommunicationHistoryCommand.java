package com.personal.marketnote.fulfillment.port.in.command.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import lombok.Builder;

@Builder
public record FulfillmentVendorCommunicationHistoryCommand(
        FulfillmentVendorCommunicationTargetType targetType,
        String targetId,
        FulfillmentVendorName vendorName,
        FulfillmentVendorCommunicationType communicationType,
        FulfillmentVendorCommunicationSenderType sender,
        String exception,
        String payload,
        JsonNode payloadJson
) {
}
