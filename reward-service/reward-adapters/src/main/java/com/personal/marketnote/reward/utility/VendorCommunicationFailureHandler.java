package com.personal.marketnote.reward.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationSenderType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VendorCommunicationFailureHandler {
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final OfferwallVendorCommunicationPayloadGenerator offerwallVendorCommunicationPayloadGenerator;

    public ResponseEntity<String> handleFailure(
            RewardVendorCommunicationTargetType targetType,
            RewardVendorName vendorName,
            String requestPayload,
            JsonNode requestPayloadJson,
            Exception e,
            int resultCode,
            String resultMessage
    ) {
        log.error("Exception occured while handling adpopcorn reward: {}", e.getMessage());
        String exceptionName = e.getClass().getSimpleName();
        vendorCommunicationRecorder.record(
                targetType,
                RewardVendorCommunicationType.REQUEST,
                RewardVendorCommunicationSenderType.VENDOR,
                vendorName,
                requestPayload,
                requestPayloadJson,
                exceptionName
        );

        JsonNode responsePayloadJson = offerwallVendorCommunicationPayloadGenerator.buildResponsePayloadJson(
                false, resultCode, resultMessage
        );
        String responsePayload = responsePayloadJson.toString();

        vendorCommunicationRecorder.record(
                targetType,
                RewardVendorCommunicationType.RESPONSE,
                RewardVendorCommunicationSenderType.SERVER,
                vendorName,
                responsePayload,
                responsePayloadJson,
                exceptionName
        );

        return ResponseEntity.ok(responsePayload);
    }
}
