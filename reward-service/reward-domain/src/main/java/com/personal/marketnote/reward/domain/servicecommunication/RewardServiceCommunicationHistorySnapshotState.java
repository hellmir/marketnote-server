package com.personal.marketnote.reward.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RewardServiceCommunicationHistorySnapshotState {
    private final Long id;
    private final RewardServiceCommunicationTargetType targetType;
    private final String targetId;
    private final RewardServiceCommunicationType communicationType;
    private final RewardServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
    private final LocalDateTime createdAt;
}
