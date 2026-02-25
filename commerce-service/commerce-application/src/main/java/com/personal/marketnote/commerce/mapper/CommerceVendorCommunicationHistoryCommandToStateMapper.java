package com.personal.marketnote.commerce.mapper;

import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistoryCreateState;
import com.personal.marketnote.commerce.port.in.command.vendorcommunication.CommerceVendorCommunicationHistoryCommand;

public class CommerceVendorCommunicationHistoryCommandToStateMapper {
    private CommerceVendorCommunicationHistoryCommandToStateMapper() {
    }

    public static CommerceVendorCommunicationHistoryCreateState mapToCreateState(
            CommerceVendorCommunicationHistoryCommand command
    ) {
        return CommerceVendorCommunicationHistoryCreateState.builder()
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
