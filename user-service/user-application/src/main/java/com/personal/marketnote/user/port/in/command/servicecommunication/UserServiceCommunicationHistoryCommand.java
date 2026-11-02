package com.personal.marketnote.user.port.in.command.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationSenderType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationTargetType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserServiceCommunicationHistoryCommand {
    private final UserServiceCommunicationTargetType targetType;
    private final String targetId;
    private final UserServiceCommunicationType communicationType;
    private final UserServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
