package com.personal.marketnote.file.port.in.command.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationSenderType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationTargetType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileServiceCommunicationHistoryCommand {
    private final FileServiceCommunicationTargetType targetType;
    private final String targetId;
    private final FileServiceCommunicationType communicationType;
    private final FileServiceCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
