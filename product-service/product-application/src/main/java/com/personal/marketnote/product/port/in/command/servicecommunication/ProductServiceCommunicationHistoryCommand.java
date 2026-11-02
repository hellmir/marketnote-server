package com.personal.marketnote.product.port.in.command.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductServiceCommunicationHistoryCommand {
    private final ProductServiceCommunicationTargetType targetType;
    private final String targetId;
    private final ProductServiceCommunicationType communicationType;
    private final ProductServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
