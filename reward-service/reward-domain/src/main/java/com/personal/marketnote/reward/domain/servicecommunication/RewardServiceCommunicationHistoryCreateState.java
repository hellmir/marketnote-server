package com.personal.marketnote.reward.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RewardServiceCommunicationHistoryCreateState {
    private final RewardServiceCommunicationTargetType targetType;
    private final String targetId;
    private final RewardServiceCommunicationType communicationType;
    private final RewardServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
