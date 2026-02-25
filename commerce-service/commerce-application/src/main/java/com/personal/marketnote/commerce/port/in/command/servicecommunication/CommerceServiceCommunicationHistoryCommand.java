package com.personal.marketnote.commerce.port.in.command.servicecommunication;

import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationSenderType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationTargetType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommerceServiceCommunicationHistoryCommand {
    private final CommerceServiceCommunicationTargetType targetType;
    private final String targetId;
    private final CommerceServiceCommunicationType communicationType;
    private final CommerceServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final String payloadJson;
}
