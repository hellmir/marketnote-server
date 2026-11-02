package com.personal.marketnote.reward.port.in.command.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationSenderType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationTargetType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RewardServiceCommunicationHistoryCommand {
    private final RewardServiceCommunicationTargetType targetType;
    private final String targetId;
    private final RewardServiceCommunicationType communicationType;
    private final RewardServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
