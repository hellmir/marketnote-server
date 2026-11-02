package com.personal.marketnote.reward.mapper;

import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationHistoryCreateState;
import com.personal.marketnote.reward.port.in.command.vendorcommunication.RewardVendorCommunicationHistoryCommand;

public class RewardVendorCommunicationHistoryCommandToStateMapper {

    private RewardVendorCommunicationHistoryCommandToStateMapper() {
    }

    public static RewardVendorCommunicationHistoryCreateState mapToCreateState(
            RewardVendorCommunicationHistoryCommand command
    ) {
        return RewardVendorCommunicationHistoryCreateState.builder()
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
