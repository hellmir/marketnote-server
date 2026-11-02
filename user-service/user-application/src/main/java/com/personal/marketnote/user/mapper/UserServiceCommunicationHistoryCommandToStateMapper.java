package com.personal.marketnote.user.mapper;

import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistoryCreateState;
import com.personal.marketnote.user.port.in.command.servicecommunication.UserServiceCommunicationHistoryCommand;

public class UserServiceCommunicationHistoryCommandToStateMapper {
    private UserServiceCommunicationHistoryCommandToStateMapper() {
    }

    public static UserServiceCommunicationHistoryCreateState mapToCreateState(
            UserServiceCommunicationHistoryCommand command
    ) {
        return UserServiceCommunicationHistoryCreateState.builder()
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
