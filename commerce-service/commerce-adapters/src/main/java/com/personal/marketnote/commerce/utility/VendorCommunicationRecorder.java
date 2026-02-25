package com.personal.marketnote.commerce.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.port.in.command.vendorcommunication.CommerceVendorCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.vendorcommunication.RecordCommerceVendorCommunicationHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VendorCommunicationRecorder {
    private final RecordCommerceVendorCommunicationHistoryUseCase recordVendorCommunicationHistoryUseCase;

    public void record(
            CommerceVendorCommunicationTargetType targetType,
            CommerceVendorCommunicationType communicationType,
            CommerceVendorCommunicationSenderType sender,
            CommerceVendorName vendorName,
            String payload,
            JsonNode payloadJson
    ) {
        recordVendorCommunicationHistoryUseCase.record(
                CommerceVendorCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .targetId(null)
                        .vendorName(vendorName)
                        .communicationType(communicationType)
                        .sender(sender)
                        .payload(payload)
                        .payloadJson(payloadJson != null ? payloadJson.toString() : null)
                        .build()
        );
    }

    public void record(
            CommerceVendorCommunicationTargetType targetType,
            CommerceVendorCommunicationType communicationType,
            CommerceVendorCommunicationSenderType sender,
            String targetId,
            CommerceVendorName vendorName,
            String payload,
            JsonNode payloadJson
    ) {
        recordVendorCommunicationHistoryUseCase.record(
                CommerceVendorCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .targetId(targetId)
                        .vendorName(vendorName)
                        .communicationType(communicationType)
                        .sender(sender)
                        .payload(payload)
                        .payloadJson(payloadJson != null ? payloadJson.toString() : null)
                        .build()
        );
    }

    public void record(
            CommerceVendorCommunicationTargetType targetType,
            CommerceVendorCommunicationType communicationType,
            CommerceVendorCommunicationSenderType sender,
            CommerceVendorName vendorName,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        recordVendorCommunicationHistoryUseCase.record(
                CommerceVendorCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .vendorName(vendorName)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(payloadJson != null ? payloadJson.toString() : null)
                        .build()
        );
    }

    public void record(
            CommerceVendorCommunicationTargetType targetType,
            CommerceVendorCommunicationType communicationType,
            CommerceVendorCommunicationSenderType sender,
            String targetId,
            CommerceVendorName vendorName,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        recordVendorCommunicationHistoryUseCase.record(
                CommerceVendorCommunicationHistoryCommand.builder()
                        .targetType(targetType)
                        .targetId(targetId)
                        .vendorName(vendorName)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(payloadJson != null ? payloadJson.toString() : null)
                        .build()
        );
    }
}
