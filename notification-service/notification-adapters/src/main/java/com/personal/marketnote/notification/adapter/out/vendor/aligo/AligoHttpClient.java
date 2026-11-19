package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiException;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

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

    public AligoHttpClient(
            RestClient restClient, ObjectMapper objectMapper, AligoProperties properties,
            ExternalCallLogger externalCallLogger
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.externalCallLogger = externalCallLogger;
    }

    public <T> T post(String operation, String path, MultiValueMap<String, String> params, Class<T> responseType) {
        MultiValueMap<String, String> mergedParams = mergeCommonParams(params);
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
                log.warn("알리고 API 호출 실패 (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage());
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
        try {
            String responseBody = restClient.post()
                    .uri(path)
                    .contentType(contentType)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            AligoApiResponse baseResponse = objectMapper.readValue(responseBody, AligoApiResponse.class);
            logResultCode(operation, path, baseResponse.resultCode(), baseResponse.message());
            AligoApiErrorMapper.throwIfError(baseResponse.resultCode(), baseResponse.message());

            if (responseType == AligoApiResponse.class) {
                return (T) baseResponse;
            }
            return objectMapper.readValue(responseBody, responseType);
        } catch (AligoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new AligoApiRequestFailedException("알리고 API 요청 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
