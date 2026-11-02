package com.personal.marketnote.commerce.mapper;

import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistoryCreateState;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;

public class CommerceServiceCommunicationHistoryCommandToStateMapper {
    private CommerceServiceCommunicationHistoryCommandToStateMapper() {
    }

    public static CommerceServiceCommunicationHistoryCreateState mapToCreateState(
            CommerceServiceCommunicationHistoryCommand command
    ) {
        return CommerceServiceCommunicationHistoryCreateState.builder()
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
