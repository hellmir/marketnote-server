package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationFailureHandler;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationRecorder;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiException;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class AligoHttpClient {

    private static final Logger log = LoggerFactory.getLogger(AligoHttpClient.class);
    private static final Logger aligoCallLog = LoggerFactory.getLogger(ExternalSystem.ALIGO.loggerName());
    private static final String TAG_PATH = "path";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AligoProperties properties;
    private final ExternalCallLogger externalCallLogger;
    private final AligoVendorCommunicationRecorder vendorCommunicationRecorder;
    private final AligoVendorCommunicationFailureHandler vendorCommunicationFailureHandler;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final AligoOperationTargetTypeResolver operationTargetTypeResolver;

    public AligoHttpClient(
            RestClient restClient, ObjectMapper objectMapper, AligoProperties properties,
            ExternalCallLogger externalCallLogger,
            AligoVendorCommunicationRecorder vendorCommunicationRecorder,
            AligoVendorCommunicationFailureHandler vendorCommunicationFailureHandler,
            VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator,
            AligoOperationTargetTypeResolver operationTargetTypeResolver
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.externalCallLogger = externalCallLogger;
        this.vendorCommunicationRecorder = vendorCommunicationRecorder;
        this.vendorCommunicationFailureHandler = vendorCommunicationFailureHandler;
        this.vendorCommunicationPayloadGenerator = vendorCommunicationPayloadGenerator;
        this.operationTargetTypeResolver = operationTargetTypeResolver;
    }

    public <T> T post(String operation, String path, MultiValueMap<String, String> params, Class<T> responseType) {
        MultiValueMap<String, String> mergedParams = mergeCommonParams(params);
        if (isSensitivePath(path)) {
            return externalCallLogger.recordSensitive(
                    ExternalSystem.ALIGO, operation, Map.of(TAG_PATH, path),
                    () -> executeWithRetry(
                            () -> executeRequest(
                                    operation, path, mergedParams, responseType, MediaType.APPLICATION_FORM_URLENCODED
                            )
                    )
            );
        }
        return externalCallLogger.record(
                ExternalSystem.ALIGO, operation, Map.of(TAG_PATH, path),
                () -> executeWithRetry(
                        () -> executeRequest(
                                operation, path, mergedParams, responseType, MediaType.APPLICATION_FORM_URLENCODED
                        )
                )
        );
    }

    public <T> T postMultipart(
            String operation, String path, MultiValueMap<String, Object> params, Class<T> responseType
    ) {
        MultiValueMap<String, Object> mergedParams = mergeCommonParams(params);
        if (isSensitivePath(path)) {
            return externalCallLogger.recordSensitive(
                    ExternalSystem.ALIGO, operation, Map.of(TAG_PATH, path),
                    () -> executeWithRetry(
                            () -> executeRequest(
                                    operation, path, mergedParams, responseType, MediaType.MULTIPART_FORM_DATA
                            )
                    )
            );
        }
        return externalCallLogger.record(
                ExternalSystem.ALIGO, operation, Map.of(TAG_PATH, path),
                () -> executeWithRetry(
                        () -> executeRequest(
                                operation, path, mergedParams, responseType, MediaType.MULTIPART_FORM_DATA
                        )
                )
        );
    }

    private <T> T executeWithRetry(Supplier<T> action) {
        int maxAttempts = properties.getRetry().getMaxAttempts();
        long backoffMs = properties.getRetry().getInitialBackoffMs();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (AligoApiRequestFailedException e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                log.warn("알리고 API 호출 실패 (attempt {}/{}), 재시도 예정", attempt, maxAttempts, e);
                sleep(backoffMs);
                backoffMs = backoffMs * 2;
            }
        }
        throw new AligoApiRequestFailedException("알리고 API 호출 최대 재시도 횟수를 초과했습니다.");
    }

    @SuppressWarnings("unchecked")
    private <T> T executeRequest(
            String operation, String path, Object body, Class<T> responseType, MediaType contentType
    ) {
        boolean sensitive = isSensitivePath(path);
        NotificationVendorCommunicationTargetType targetType = operationTargetTypeResolver.resolve(operation);

        JsonNode requestPayloadJson = buildRequestPayloadJson(operation, path, contentType, body);
        String requestPayload = requestPayloadJson.toString();

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(path)
                    .contentType(contentType)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException re) {
            recordHttpFailureSafely(targetType, requestPayload, requestPayloadJson, re, sensitive);
            throw wrapHttpFailure(path, re);
        }

        try {
            AligoApiResponse baseResponse = objectMapper.readValue(responseBody, AligoApiResponse.class);
            logResultCode(operation, path, baseResponse.resultCode(), baseResponse.message());

            String exception = resolveResponseException(baseResponse);
            recordSuccessSafely(
                    targetType, requestPayload, requestPayloadJson, responseBody, baseResponse, sensitive, exception
            );

            AligoApiErrorMapper.throwIfError(baseResponse.resultCode(), baseResponse.message());

            if (responseType == AligoApiResponse.class) {
                return (T) baseResponse;
            }
            return objectMapper.readValue(responseBody, responseType);
        } catch (AligoApiException e) {
            throw e;
        } catch (Exception e) {
            recordParseFailureSafely(targetType, requestPayload, requestPayloadJson, responseBody, e, sensitive);
            throw wrapParseFailure(path, e);
        }
    }

    private String resolveResponseException(AligoApiResponse baseResponse) {
        if (baseResponse.resultCode() == 0) {
            return null;
        }
        return "RESULT_CODE_" + baseResponse.resultCode();
    }

    private void recordSuccessSafely(
            NotificationVendorCommunicationTargetType targetType,
            String requestPayload,
            JsonNode requestPayloadJson,
            String responseBody,
            AligoApiResponse baseResponse,
            boolean sensitive,
            String exception
    ) {
        try {
            vendorCommunicationRecorder.record(
                    targetType,
                    NotificationVendorCommunicationType.REQUEST,
                    NotificationVendorCommunicationSenderType.SERVER,
                    NotificationVendorName.ALIGO,
                    requestPayload,
                    requestPayloadJson,
                    exception
            );

            JsonNode responsePayloadJson = buildResponsePayloadJson(responseBody, baseResponse, sensitive);
            String responsePayload = responsePayloadJson.toString();

            vendorCommunicationRecorder.record(
                    targetType,
                    NotificationVendorCommunicationType.RESPONSE,
                    NotificationVendorCommunicationSenderType.VENDOR,
                    NotificationVendorName.ALIGO,
                    responsePayload,
                    responsePayloadJson,
                    exception
            );
        } catch (Exception ignored) {
            log.error(
                    "Aligo 통신 이력 기록 실패 (정상 응답 흐름): targetType={}, exception={}",
                    targetType, ignored.getClass().getSimpleName(), ignored
            );
        }
    }

    private void recordHttpFailureSafely(
            NotificationVendorCommunicationTargetType targetType,
            String requestPayload,
            JsonNode requestPayloadJson,
            RestClientResponseException re,
            boolean sensitive
    ) {
        try {
            int statusCode = re.getStatusCode().value();
            String responseBody = sensitive ? null : re.getResponseBodyAsString();
            vendorCommunicationFailureHandler.handleHttpFailure(
                    targetType, requestPayload, requestPayloadJson, statusCode, responseBody, sensitive
            );
        } catch (Exception ignored) {
            log.error(
                    "Aligo 통신 이력 기록 실패 (HTTP 실패 흐름): targetType={}, exception={}",
                    targetType, ignored.getClass().getSimpleName(), ignored
            );
        }
    }

    private void recordParseFailureSafely(
            NotificationVendorCommunicationTargetType targetType,
            String requestPayload,
            JsonNode requestPayloadJson,
            String responseBody,
            Exception parseException,
            boolean sensitive
    ) {
        try {
            String exceptionName = parseException.getClass().getSimpleName();
            String safeBody = sensitive ? null : responseBody;
            vendorCommunicationFailureHandler.handleParseFailure(
                    targetType, requestPayload, requestPayloadJson, safeBody, exceptionName, sensitive
            );
        } catch (Exception ignored) {
            log.error(
                    "Aligo 통신 이력 기록 실패 (파싱 실패 흐름): targetType={}, exception={}",
                    targetType, ignored.getClass().getSimpleName(), ignored
            );
        }
    }

    private JsonNode buildRequestPayloadJson(
            String operation, String path, MediaType contentType, Object body
    ) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("operation", operation);
        map.put("path", path);
        map.put("contentType", contentType.toString());
        map.put("params", maskRequestBody(body));
        return vendorCommunicationPayloadGenerator.buildPayloadJson(map);
    }

    private Object maskRequestBody(Object body) {
        if (FormatValidator.hasNoValue(body)) {
            return Map.of();
        }
        if (!(body instanceof MultiValueMap<?, ?> multiValueMap)) {
            return body;
        }
        Map<String, Object> masked = new LinkedHashMap<>();
        multiValueMap.forEach((k, v) -> {
            String key = String.valueOf(k);
            Object value = sanitizeMultiValueEntry(key, v);
            masked.put(key, value);
        });
        return masked;
    }

    private Object sanitizeMultiValueEntry(String key, Object rawValue) {
        if ("apikey".equals(key) || "userid".equals(key)) {
            return maskValue(FormatValidator.isNull(rawValue) ? null : rawValue.toString());
        }
        if (!(rawValue instanceof List<?> list)) {
            return summarizeValue(rawValue);
        }
        List<Object> sanitized = new ArrayList<>(list.size());
        for (Object element : list) {
            sanitized.add(summarizeValue(element));
        }
        return sanitized;
    }

    private Object summarizeValue(Object value) {
        if (FormatValidator.hasNoValue(value)) {
            return null;
        }
        if (value instanceof Resource resource) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("type", "Resource");
            meta.put("filename", resource.getFilename());
            try {
                meta.put("contentLength", resource.contentLength());
            } catch (Exception ignored) {
                // contentLength 계산 실패는 메타에 표기 생략
            }
            return meta;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return value.toString();
    }

    private String maskValue(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return value;
        }
        int visible = Math.min(4, value.length());
        int maskedLength = value.length() - visible;
        if (maskedLength <= 0) {
            return value;
        }
        return "*".repeat(maskedLength) + value.substring(value.length() - visible);
    }

    private JsonNode buildResponsePayloadJson(String responseBody, AligoApiResponse baseResponse, boolean sensitive) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (sensitive) {
            map.put("masked", true);
            map.put("resultCode", baseResponse.resultCode());
            return vendorCommunicationPayloadGenerator.buildPayloadJson(map);
        }
        map.put("resultCode", baseResponse.resultCode());
        map.put("message", baseResponse.message());
        if (FormatValidator.isNotNull(responseBody)) {
            map.put("body", responseBody);
        }
        return vendorCommunicationPayloadGenerator.buildPayloadJson(map);
    }

    private AligoApiRequestFailedException wrapParseFailure(String path, Exception e) {
        if (isSensitivePath(path)) {
            log.error(
                    "알리고 API 응답 처리 실패: path={}, exception={} (민감 데이터 포함 응답 — 메시지/스택 로깅 생략)",
                    path, e.getClass().getSimpleName()
            );
            return new AligoApiRequestFailedException("알리고 API 응답 처리 실패. path=" + path);
        }
        log.error("알리고 API 응답 처리 실패: path={}", path, e);
        return new AligoApiRequestFailedException("알리고 API 응답 처리 실패. path=" + path, e);
    }

    private AligoApiRequestFailedException wrapHttpFailure(String path, RestClientResponseException re) {
        int statusCode = re.getStatusCode().value();
        if (isSensitivePath(path)) {
            log.error(
                    "알리고 API HTTP 응답 실패: path={}, status={}, exception={} (민감 데이터 포함 응답 — 메시지/스택 로깅 생략)",
                    path, statusCode, re.getClass().getSimpleName()
            );
            return new AligoApiRequestFailedException(
                    "알리고 API HTTP 응답 실패. path=" + path + ", status=" + statusCode
            );
        }
        log.error("알리고 API HTTP 응답 실패: path={}, status={}", path, statusCode, re);
        return new AligoApiRequestFailedException(
                "알리고 API HTTP 응답 실패. path=" + path + ", status=" + statusCode, re
        );
    }

    private boolean isSensitivePath(String path) {
        return properties.getApi().getSensitivePaths().contains(path);
    }

    private void logResultCode(String operation, String path, int resultCode, String message) {
        if (resultCode == 0) {
            if (aligoCallLog.isDebugEnabled()) {
                aligoCallLog.debug(
                        "Aligo response: operation={}, path={}, resultCode={}, message={}",
                        operation, path, resultCode, message
                );
            }
            return;
        }
        aligoCallLog.error(
                "Aligo response error: operation={}, path={}, resultCode={}, message={}",
                operation, path, resultCode, message
        );
    }

    private <V> MultiValueMap<String, V> mergeCommonParams(MultiValueMap<String, V> params) {
        MultiValueMap<String, V> merged = new LinkedMultiValueMap<>(params);
        @SuppressWarnings("unchecked")
        V apiKey = (V) properties.getApiKey();
        @SuppressWarnings("unchecked")
        V userId = (V) properties.getUserId();
        merged.add("apikey", apiKey);
        merged.add("userid", userId);
        if (properties.isTestMode()) {
            @SuppressWarnings("unchecked")
            V testModeYn = (V) "Y";
            merged.add("testmode_yn", testModeYn);
        }
        return merged;
    }

    private void sleep(long millis) {
        try {
            long jitteredSleepMs = ThreadLocalRandom.current().nextLong(Math.max(1L, millis) + 1);
            Thread.sleep(jitteredSleepMs);
        } catch (InterruptedException ignored) {
            // 인터럽트는 호출자 흐름에서 다시 감지하도록 interrupt 플래그만 복원
            Thread.currentThread().interrupt();
        }
    }
}
