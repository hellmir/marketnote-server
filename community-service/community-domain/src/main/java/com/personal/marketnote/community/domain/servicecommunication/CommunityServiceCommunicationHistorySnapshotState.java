package com.personal.marketnote.community.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommunityServiceCommunicationHistorySnapshotState {
    private final Long id;
    private final CommunityServiceCommunicationTargetType targetType;
    private final String targetId;
    private final CommunityServiceCommunicationType communicationType;
    private final CommunityServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
    private final LocalDateTime createdAt;
}
