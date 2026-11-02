package com.personal.marketnote.file.mapper;

import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistoryCreateState;
import com.personal.marketnote.file.port.in.command.servicecommunication.FileServiceCommunicationHistoryCommand;

public class FileServiceCommunicationHistoryCommandToStateMapper {
    private FileServiceCommunicationHistoryCommandToStateMapper() {
    }

    public static FileServiceCommunicationHistoryCreateState mapToCreateState(
            FileServiceCommunicationHistoryCommand command
    ) {
        return FileServiceCommunicationHistoryCreateState.builder()
                .targetType(command.getTargetType())
                .targetId(command.getTargetId())
                .communicationType(command.getCommunicationType())
                .sender(command.getSender())
                .exception(command.getException())
                .payload(command.getPayload())
                .payloadJson(command.getPayloadJson())
                .build();
    }
}
