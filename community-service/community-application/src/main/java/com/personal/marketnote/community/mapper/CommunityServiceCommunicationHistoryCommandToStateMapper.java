package com.personal.marketnote.community.mapper;

import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistoryCreateState;
import com.personal.marketnote.community.port.in.command.servicecommunication.CommunityServiceCommunicationHistoryCommand;

public class CommunityServiceCommunicationHistoryCommandToStateMapper {
    private CommunityServiceCommunicationHistoryCommandToStateMapper() {
    }

    public static CommunityServiceCommunicationHistoryCreateState mapToCreateState(
            CommunityServiceCommunicationHistoryCommand command
    ) {
        return CommunityServiceCommunicationHistoryCreateState.builder()
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
