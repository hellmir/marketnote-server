package com.personal.marketnote.fulfillment.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VendorCommunicationFailureHandler {
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;

    public void handleFailure(
            FulfillmentVendorCommunicationTargetType targetType,
            FulfillmentVendorName vendorName,
            String requestPayload,
            JsonNode requestPayloadJson,
            Object errorPayload,
            Exception e
    ) {
        handleFailure(targetType, null, vendorName, requestPayload, requestPayloadJson, errorPayload, e);
    }

    public void handleFailure(
            FulfillmentVendorCommunicationTargetType targetType,
            String targetId,
            FulfillmentVendorName vendorName,
            String requestPayload,
            JsonNode requestPayloadJson,
            Object errorPayload,
            Exception e
    ) {
        String exceptionName = e.getClass().getSimpleName();
        log.error("Exception occured while communicating with vendor: {}", e.getMessage(), e);

        vendorCommunicationRecorder.record(
                targetType,
                FulfillmentVendorCommunicationType.REQUEST,
                FulfillmentVendorCommunicationSenderType.SERVER,
                targetId,
                vendorName,
                requestPayload,
                requestPayloadJson,
                exceptionName
        );

        JsonNode responsePayloadJson = vendorCommunicationPayloadGenerator.buildPayloadJson(errorPayload);
        String responsePayload = responsePayloadJson.toString();

        vendorCommunicationRecorder.record(
                targetType,
                FulfillmentVendorCommunicationType.RESPONSE,
                FulfillmentVendorCommunicationSenderType.VENDOR,
                targetId,
                vendorName,
                responsePayload,
                responsePayloadJson,
                exceptionName
        );
    }
}
