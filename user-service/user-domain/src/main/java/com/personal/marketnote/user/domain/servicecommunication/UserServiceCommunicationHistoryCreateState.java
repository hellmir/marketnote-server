package com.personal.marketnote.user.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserServiceCommunicationHistoryCreateState {
    private final UserServiceCommunicationTargetType targetType;
    private final String targetId;
    private final UserServiceCommunicationType communicationType;
    private final UserServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
