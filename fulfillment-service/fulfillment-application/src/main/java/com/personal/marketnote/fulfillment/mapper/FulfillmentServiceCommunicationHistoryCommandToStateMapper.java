package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistoryCreateState;
import com.personal.marketnote.fulfillment.port.in.command.servicecommunication.FulfillmentServiceCommunicationHistoryCommand;

public class FulfillmentServiceCommunicationHistoryCommandToStateMapper {
    private FulfillmentServiceCommunicationHistoryCommandToStateMapper() {
    }

    public static FulfillmentServiceCommunicationHistoryCreateState mapToCreateState(
            FulfillmentServiceCommunicationHistoryCommand command
    ) {
        return FulfillmentServiceCommunicationHistoryCreateState.builder()
                .targetType(command.targetType())
                .targetId(command.targetId())
                .communicationType(command.communicationType())
                .sender(command.sender())
                .exception(command.exception())
                .payload(command.payload())
                .payloadJson(command.payloadJson())
                .build();
    }
}
