package com.personal.marketnote.product.mapper;

import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistoryCreateState;
import com.personal.marketnote.product.port.in.command.servicecommunication.ProductServiceCommunicationHistoryCommand;

public class ProductServiceCommunicationHistoryCommandToStateMapper {
    private ProductServiceCommunicationHistoryCommandToStateMapper() {
    }

    public static ProductServiceCommunicationHistoryCreateState mapToCreateState(
            ProductServiceCommunicationHistoryCommand command
    ) {
        return ProductServiceCommunicationHistoryCreateState.builder()
                .targetType(command.getTargetType())
                .targetId(command.getTargetId())
                .communicationType(command.getCommunicationType())
                .sender(command.getSender())
                .exception(command.getException())
                .payload(command.getPayload())
                .payloadJson(command.getPayloadJson())
                .build();
    }
}
