package com.personal.marketnote.commerce.domain.servicecommunication;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommerceServiceCommunicationHistoryCreateState {
    private final CommerceServiceCommunicationTargetType targetType;
    private final String targetId;
    private final CommerceServiceCommunicationType communicationType;
    private final CommerceServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final String payloadJson;
}
