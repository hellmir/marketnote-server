package com.personal.marketnote.file.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileServiceCommunicationHistoryCreateState {
    private final FileServiceCommunicationTargetType targetType;
    private final String targetId;
    private final FileServiceCommunicationType communicationType;
    private final FileServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
