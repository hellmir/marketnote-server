package com.personal.marketnote.notification.adapter.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.adapter.out.vendor.aligo.AttemptResult;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aligo HTTP 호출 실패 시 통신 이력 영속화를 위임받는 핸들러.
 *
 * <p>HTTP 4xx/5xx, 응답 파싱 실패 등의 경우 REQUEST + 실패 RESPONSE 두 건을 record한다.
 * sensitive path인 경우 응답 body를 마스킹 메타로 대체한다.
 * retry 정책 적용 후 최종 실패 1회에만 호출되며, 시도별 outcome은 attempts 메타로 함께 저장된다.</p>
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
            boolean masked,
            int attemptCount,
            List<AttemptResult> attempts
    ) {
        String exception = "HTTP_" + statusCode;

        recorder.record(
                targetType,
                NotificationVendorCommunicationType.REQUEST,
                NotificationVendorCommunicationSenderType.SERVER,
                NotificationVendorName.ALIGO,
                requestPayload,
                requestPayloadJson,
                exception,
                attemptCount
        );

        Map<String, Object> responsePayloadMap = buildResponsePayloadMap(
                statusCode, responseBody, masked, attemptCount, attempts
        );
        JsonNode responsePayloadJson = payloadGenerator.buildPayloadJson(responsePayloadMap);
        String responsePayload = responsePayloadJson.toString();

        recorder.record(
                targetType,
                NotificationVendorCommunicationType.RESPONSE,
                NotificationVendorCommunicationSenderType.VENDOR,
                NotificationVendorName.ALIGO,
                responsePayload,
                responsePayloadJson,
                exception,
                attemptCount
        );
    }

    public void handleParseFailure(
            NotificationVendorCommunicationTargetType targetType,
            String requestPayload,
            JsonNode requestPayloadJson,
            String responseBody,
            String exceptionName,
            boolean masked,
            int attemptCount,
            List<AttemptResult> attempts
    ) {
        recorder.record(
                targetType,
                NotificationVendorCommunicationType.REQUEST,
                NotificationVendorCommunicationSenderType.SERVER,
                NotificationVendorName.ALIGO,
                requestPayload,
                requestPayloadJson,
                exceptionName,
                attemptCount
        );

        Map<String, Object> responsePayloadMap = buildParseFailurePayloadMap(
                responseBody, exceptionName, masked, attemptCount, attempts
        );
        JsonNode responsePayloadJson = payloadGenerator.buildPayloadJson(responsePayloadMap);
        String responsePayload = responsePayloadJson.toString();

        recorder.record(
                targetType,
                NotificationVendorCommunicationType.RESPONSE,
                NotificationVendorCommunicationSenderType.VENDOR,
                NotificationVendorName.ALIGO,
                responsePayload,
                responsePayloadJson,
                exceptionName,
                attemptCount
        );
    }

    private Map<String, Object> buildResponsePayloadMap(
            int statusCode, String responseBody, boolean masked, int attemptCount, List<AttemptResult> attempts
    ) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (masked) {
            map.put("masked", true);
            map.put("status", statusCode);
            map.put("attemptCount", attemptCount);
            map.put("attempts", attempts);
            return map;
        }
        map.put("status", statusCode);
        if (FormatValidator.isNotNull(responseBody)) {
            map.put("body", responseBody);
        }
        map.put("attemptCount", attemptCount);
        map.put("attempts", attempts);
        return map;
    }

    private Map<String, Object> buildParseFailurePayloadMap(
            String responseBody, String exceptionName, boolean masked, int attemptCount, List<AttemptResult> attempts
    ) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (masked) {
            map.put("masked", true);
            map.put("exception", exceptionName);
            map.put("attemptCount", attemptCount);
            map.put("attempts", attempts);
            return map;
        }
        map.put("exception", exceptionName);
        if (FormatValidator.isNotNull(responseBody)) {
            map.put("body", responseBody);
        }
        map.put("attemptCount", attemptCount);
        map.put("attempts", attempts);
        return map;
    }
}
