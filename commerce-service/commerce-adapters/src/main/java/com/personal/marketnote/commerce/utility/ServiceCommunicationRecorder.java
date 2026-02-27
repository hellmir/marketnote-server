package com.personal.marketnote.commerce.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationSenderType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationTargetType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationType;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.servicecommunication.RecordCommerceServiceCommunicationHistoryUseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceCommunicationRecorder {
    private final RecordCommerceServiceCommunicationHistoryUseCase recordServiceCommunicationHistoryUseCase;

    public void record(
            CommerceServiceCommunicationTargetType targetType,
            CommerceServiceCommunicationType communicationType,
            CommerceServiceCommunicationSenderType sender,
            String payload,
            JsonNode payloadJson
    ) {
        recordServiceCommunicationHistoryUseCase.record(
                CommerceServiceCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .targetId(null)
                        .communicationType(communicationType)
                        .sender(sender)
                        .payload(payload)
                        .payloadJson(FormatValidator.hasValue(payloadJson) ? payloadJson.toString() : null)
                        .build()
        );
    }

    public void record(
            CommerceServiceCommunicationTargetType targetType,
            CommerceServiceCommunicationType communicationType,
            CommerceServiceCommunicationSenderType sender,
            String targetId,
            String payload,
            JsonNode payloadJson
    ) {
        recordServiceCommunicationHistoryUseCase.record(
                CommerceServiceCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .targetId(targetId)
                        .communicationType(communicationType)
                        .sender(sender)
                        .payload(payload)
                        .payloadJson(FormatValidator.hasValue(payloadJson) ? payloadJson.toString() : null)
                        .build()
        );
    }

    public void record(
            CommerceServiceCommunicationTargetType targetType,
            CommerceServiceCommunicationType communicationType,
            CommerceServiceCommunicationSenderType sender,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        recordServiceCommunicationHistoryUseCase.record(
                CommerceServiceCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(FormatValidator.hasValue(payloadJson) ? payloadJson.toString() : null)
                        .build()
        );
    }

    public void record(
            CommerceServiceCommunicationTargetType targetType,
            CommerceServiceCommunicationType communicationType,
            CommerceServiceCommunicationSenderType sender,
            String targetId,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        recordServiceCommunicationHistoryUseCase.record(
                CommerceServiceCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .targetId(targetId)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(FormatValidator.hasValue(payloadJson) ? payloadJson.toString() : null)
                        .build()
        );
    }
}
