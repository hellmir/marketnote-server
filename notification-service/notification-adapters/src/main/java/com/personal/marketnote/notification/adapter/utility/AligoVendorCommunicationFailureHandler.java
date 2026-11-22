package com.personal.marketnote.notification.adapter.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aligo HTTP 호출 실패 시 통신 이력 영속화를 위임받는 핸들러.
 *
 * <p>HTTP 4xx/5xx, 응답 파싱 실패 등의 경우 REQUEST + 실패 RESPONSE 두 건을 record한다.
 * sensitive path인 경우 응답 body를 마스킹 메타로 대체한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AligoVendorCommunicationFailureHandler {

    private final AligoVendorCommunicationRecorder recorder;
    private final VendorCommunicationPayloadGenerator payloadGenerator;

    public void handleHttpFailure(
            NotificationVendorCommunicationTargetType targetType,
            String requestPayload,
            JsonNode requestPayloadJson,
            int statusCode,
            String responseBody,
            boolean masked
    ) {
        String exception = "HTTP_" + statusCode;

        recorder.record(
                targetType,
                NotificationVendorCommunicationType.REQUEST,
                NotificationVendorCommunicationSenderType.SERVER,
                NotificationVendorName.ALIGO,
                requestPayload,
                requestPayloadJson,
                exception
        );

        Map<String, Object> responsePayloadMap = buildResponsePayloadMap(statusCode, responseBody, masked);
        JsonNode responsePayloadJson = payloadGenerator.buildPayloadJson(responsePayloadMap);
        String responsePayload = responsePayloadJson.toString();

        recorder.record(
                targetType,
                NotificationVendorCommunicationType.RESPONSE,
                NotificationVendorCommunicationSenderType.VENDOR,
                NotificationVendorName.ALIGO,
                responsePayload,
                responsePayloadJson,
                exception
        );
    }

    public void handleParseFailure(
            NotificationVendorCommunicationTargetType targetType,
            String requestPayload,
            JsonNode requestPayloadJson,
            String responseBody,
            String exceptionName,
            boolean masked
    ) {
        recorder.record(
                targetType,
                NotificationVendorCommunicationType.REQUEST,
                NotificationVendorCommunicationSenderType.SERVER,
                NotificationVendorName.ALIGO,
                requestPayload,
                requestPayloadJson,
                exceptionName
        );

        Map<String, Object> responsePayloadMap = buildParseFailurePayloadMap(responseBody, exceptionName, masked);
        JsonNode responsePayloadJson = payloadGenerator.buildPayloadJson(responsePayloadMap);
        String responsePayload = responsePayloadJson.toString();

        recorder.record(
                targetType,
                NotificationVendorCommunicationType.RESPONSE,
                NotificationVendorCommunicationSenderType.VENDOR,
                NotificationVendorName.ALIGO,
                responsePayload,
                responsePayloadJson,
                exceptionName
        );
    }

    private Map<String, Object> buildResponsePayloadMap(int statusCode, String responseBody, boolean masked) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (masked) {
            map.put("masked", true);
            map.put("status", statusCode);
            return map;
        }
        map.put("status", statusCode);
        if (FormatValidator.isNotNull(responseBody)) {
            map.put("body", responseBody);
        }
        return map;
    }

    private Map<String, Object> buildParseFailurePayloadMap(String responseBody, String exceptionName, boolean masked) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (masked) {
            map.put("masked", true);
            map.put("exception", exceptionName);
            return map;
        }
        map.put("exception", exceptionName);
        if (FormatValidator.isNotNull(responseBody)) {
            map.put("body", responseBody);
        }
        return map;
    }
}
