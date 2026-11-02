package com.personal.marketnote.reward.mapper;

import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistoryCreateState;
import com.personal.marketnote.reward.port.in.command.servicecommunication.RewardServiceCommunicationHistoryCommand;

public class RewardServiceCommunicationHistoryCommandToStateMapper {
    private RewardServiceCommunicationHistoryCommandToStateMapper() {
    }

    public static RewardServiceCommunicationHistoryCreateState mapToCreateState(
            RewardServiceCommunicationHistoryCommand command
    ) {
        return RewardServiceCommunicationHistoryCreateState.builder()
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
