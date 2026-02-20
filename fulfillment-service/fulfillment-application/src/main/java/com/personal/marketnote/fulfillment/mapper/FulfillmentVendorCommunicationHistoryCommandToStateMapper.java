package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistoryCreateState;
import com.personal.marketnote.fulfillment.port.in.command.vendorcommunication.FulfillmentVendorCommunicationHistoryCommand;

public class FulfillmentVendorCommunicationHistoryCommandToStateMapper {
    private FulfillmentVendorCommunicationHistoryCommandToStateMapper() {
    }

    public static FulfillmentVendorCommunicationHistoryCreateState mapToCreateState(
            FulfillmentVendorCommunicationHistoryCommand command
    ) {
        return FulfillmentVendorCommunicationHistoryCreateState.builder()
                .targetType(command.targetType())
                .targetId(command.targetId())
                .vendorName(command.vendorName())
                .communicationType(command.communicationType())
                .sender(command.sender())
                .exception(command.exception())
                .payload(command.payload())
                .payloadJson(command.payloadJson())
                .build();
    }
}
