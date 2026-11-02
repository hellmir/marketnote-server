package com.personal.marketnote.community.port.in.command.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityServiceCommunicationHistoryCommand {
    private final CommunityServiceCommunicationTargetType targetType;
    private final String targetId;
    private final CommunityServiceCommunicationType communicationType;
    private final CommunityServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
