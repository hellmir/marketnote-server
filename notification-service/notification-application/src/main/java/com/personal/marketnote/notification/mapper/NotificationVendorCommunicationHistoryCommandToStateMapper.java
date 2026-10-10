package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistoryCreateState;
import com.personal.marketnote.notification.port.in.command.vendorcommunication.NotificationVendorCommunicationHistoryCommand;

public class NotificationVendorCommunicationHistoryCommandToStateMapper {

    private NotificationVendorCommunicationHistoryCommandToStateMapper() {
    }

    public static NotificationVendorCommunicationHistoryCreateState mapToCreateState(
            NotificationVendorCommunicationHistoryCommand command
    ) {
        return NotificationVendorCommunicationHistoryCreateState.builder()
                .targetType(command.getTargetType())
                .targetId(command.getTargetId())
                .vendorName(command.getVendorName())
                .communicationType(command.getCommunicationType())
                .sender(command.getSender())
                .exception(command.getException())
                .payload(command.getPayload())
                .payloadJson(command.getPayloadJson())
                .build();
    }
}
