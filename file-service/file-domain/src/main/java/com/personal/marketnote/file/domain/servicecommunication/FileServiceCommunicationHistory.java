package com.personal.marketnote.file.domain.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FileServiceCommunicationHistory {
    private Long id;
    private FileServiceCommunicationTargetType targetType;
    private String targetId;
    private FileServiceCommunicationType communicationType;
    private FileServiceCommunicationSenderType sender;
    private String exception;
    private String payload;
    private JsonNode payloadJson;
    private LocalDateTime createdAt;

    public static FileServiceCommunicationHistory from(FileServiceCommunicationHistoryCreateState state) {
        return FileServiceCommunicationHistory.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .build();
    }

    public static FileServiceCommunicationHistory from(FileServiceCommunicationHistorySnapshotState state) {
        return FileServiceCommunicationHistory.builder()
                .id(state.getId())
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .communicationType(state.getCommunicationType())
                .sender(state.getSender())
                .exception(state.getException())
                .payload(state.getPayload())
                .payloadJson(state.getPayloadJson())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
