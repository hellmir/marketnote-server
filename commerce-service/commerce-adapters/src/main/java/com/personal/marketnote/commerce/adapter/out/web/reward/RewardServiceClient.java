package com.personal.marketnote.commerce.adapter.out.web.reward;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationSenderType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationTargetType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationType;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.commerce.utility.ServiceCommunicationRecorder;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.RewardServiceRequestFailedException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class RewardServiceClient implements ModifyUserPointPort {
    private static final String SOURCE_TYPE_USER = "USER";
    private static final String SOURCE_TYPE_ORDER = "ORDER";
    private static final String CHANGE_TYPE_ACCRUAL = "ACCRUAL";
    private static final String CHANGE_TYPE_DEDUCTION = "DEDUCTION";
    private static final String SHARE_PURCHASE_REASON = "링크 공유 회원 상품 구매";
    private static final String ORDER_POINT_DEDUCTION_REASON = "주문 포인트 사용";
    private static final CommerceServiceCommunicationTargetType TARGET_TYPE =
            CommerceServiceCommunicationTargetType.USER_POINT;
    private static final CommerceServiceCommunicationSenderType REQUEST_SENDER =
            CommerceServiceCommunicationSenderType.COMMERCE;
    private static final CommerceServiceCommunicationSenderType RESPONSE_SENDER =
            CommerceServiceCommunicationSenderType.REWARD;

    @Value("${reward-service.base-url}")
    private String rewardServiceBaseUrl;

    @Value("${spring.jwt.admin-access-token}")
    private String adminAccessToken;

    @Value("${reward-service.share-point-amount:5000}")
    private long sharePointAmount;

    private final RestTemplate restTemplate;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @Override
    public Long getAvailablePoints(Long userId) {
        URI uri = buildUserPointUri(userId);
        HttpHeaders headers = buildHeaders();

        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception lastError = null;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        uri, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class
                );
                if (FormatValidator.hasValue(response) && response.getStatusCode().is2xxSuccessful()
                        && FormatValidator.hasValue(response.getBody())) {
                    return response.getBody().path("content").path("amount").asLong(0L);
                }

                log.warn("포인트 잔액 조회 비정상 응답 - userId: {}, attempt: {}", userId, attempt);
            } catch (Exception e) {
                lastError = e;
                log.warn("포인트 잔액 조회 실패 - userId: {}, attempt: {}, error: {}",
                        userId, attempt, e.getMessage(), e);
            }

            sleep(sleepMillis);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("포인트 잔액 조회 최종 실패 - userId: {}", userId);
        throw new RewardServiceRequestFailedException(
                FormatValidator.hasValue(lastError) ? new IOException(lastError) : new IOException("포인트 잔액 조회 실패")
        );
    }

    @Override
    public void deductOrderPoints(Long userId, Long amount, Long orderId) {
        if (FormatValidator.hasNoValue(amount) || amount <= 0) {
            return;
        }

        URI uri = buildUserPointUri(userId);
        HttpHeaders headers = buildHeaders();

        ModifyUserPointRequest request = ModifyUserPointRequest.deduction(amount, orderId);
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendDeductionRequest(uri, httpEntity, userId);
    }

    @Override
    public void accrueSharedPurchasePoints(List<Long> sharerIds) {
        if (FormatValidator.hasNoValue(sharerIds)) {
            return;
        }

        sharerIds.stream()
                .filter(Objects::nonNull)
                .forEach(this::accrueSharerPoint);
    }

    private void accrueSharerPoint(Long sharerId) {
        if (sharePointAmount <= 0) {
            return;
        }

        URI uri = buildUserPointUri(sharerId);
        HttpHeaders headers = buildHeaders();

        ensureUserPointExists(uri, headers, sharerId);

        ModifyUserPointRequest request = ModifyUserPointRequest.of(sharePointAmount, sharerId);
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequest(uri, httpEntity, sharerId);
    }

    private void ensureUserPointExists(URI uri, HttpHeaders headers, Long userId) {
        try {
            restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
        } catch (Exception e) {
            String exception = e.getClass().getSimpleName();
            JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                    HttpMethod.POST,
                    uri,
                    null,
                    1
            );
            String requestPayload = requestPayloadJson.toString();
            JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                    exception,
                    e.getMessage(),
                    1
            );
            String responsePayload = responsePayloadJson.toString();
            recordCommunication(
                    TARGET_TYPE,
                    String.valueOf(userId),
                    CommerceServiceCommunicationType.REQUEST,
                    requestPayload,
                    requestPayloadJson,
                    exception
            );
            recordCommunication(
                    TARGET_TYPE,
                    String.valueOf(userId),
                    CommerceServiceCommunicationType.RESPONSE,
                    responsePayload,
                    responsePayloadJson,
                    exception
            );
            log.info("User point registration skipped: userId={}, message={}", userId, e.getMessage());
        }
    }

    private void sendRequest(URI uri, HttpEntity<ModifyUserPointRequest> httpEntity, Long userId) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity, Void.class);
                if (FormatValidator.hasValue(response) && response.getStatusCode().is2xxSuccessful()) {
                    return;
                }

                String exception = FormatValidator.hasValue(response)
                        ? response.getStatusCode().toString()
                        : "EmptyResponse";
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.PATCH,
                        uri,
                        httpEntity.getBody(),
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson =
                        serviceCommunicationPayloadGenerator.buildResponsePayloadJson(response, attempt);
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        CommerceServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        CommerceServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(
                        "Reward service responded with non-2xx status for userId={}, status={}",
                        userId, response.getStatusCode()
                );
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.PATCH,
                        uri,
                        httpEntity.getBody(),
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                        exception,
                        e.getMessage(),
                        attempt
                );
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        CommerceServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        CommerceServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(
                        "Failed to accrue user point on reward-service: userId={}, attempt={}, message={}",
                        userId, i + 1, e.getMessage(), e
                );
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }
            }

            sleep(sleepMillis);
            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to accrue user point: {} with error: {}", uri, error.getMessage(), error);
        throw new RewardServiceRequestFailedException(new IOException());
    }

    private URI buildUserPointUri(Long userId) {
        return UriComponentsBuilder
                .fromUriString(rewardServiceBaseUrl)
                .path("/api/v1/users/{userId}/points")
                .buildAndExpand(userId)
                .toUri();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    private void sleep(long millis) {
        try {
            // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
            long jitteredSleepMillis = ThreadLocalRandom.current()
                    .nextLong(Math.max(1L, millis) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendDeductionRequest(URI uri, HttpEntity<ModifyUserPointRequest> httpEntity, Long userId) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity, Void.class);
                if (FormatValidator.hasValue(response) && response.getStatusCode().is2xxSuccessful()) {
                    return;
                }

                log.warn("포인트 차감 비정상 응답 - userId: {}, attempt: {}, status: {}",
                        userId, attempt,
                        FormatValidator.hasValue(response) ? response.getStatusCode() : "empty");
            } catch (Exception e) {
                log.warn("포인트 차감 요청 실패 - userId: {}, attempt: {}, error: {}",
                        userId, attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    log.error("포인트 차감 최종 실패 - userId: {}", userId);
                    throw new RewardServiceRequestFailedException(new IOException(e));
                }
            }

            sleep(sleepMillis);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }
    }

    private record ModifyUserPointRequest(
            String changeType,
            long amount,
            String sourceType,
            Long sourceId,
            String reason
    ) {
        private static ModifyUserPointRequest of(long amount, Long sourceId) {
            return new ModifyUserPointRequest(
                    CHANGE_TYPE_ACCRUAL,
                    Math.abs(amount),
                    SOURCE_TYPE_USER,
                    sourceId,
                    SHARE_PURCHASE_REASON
            );
        }

        private static ModifyUserPointRequest deduction(long amount, Long orderId) {
            return new ModifyUserPointRequest(
                    CHANGE_TYPE_DEDUCTION,
                    Math.abs(amount),
                    SOURCE_TYPE_ORDER,
                    orderId,
                    ORDER_POINT_DEDUCTION_REASON
            );
        }
    }

    private void recordCommunication(
            CommerceServiceCommunicationTargetType targetType,
            String targetId,
            CommerceServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        CommerceServiceCommunicationSenderType sender =
                communicationType == CommerceServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
        serviceCommunicationRecorder.record(
                targetType,
                communicationType,
                sender,
                targetId,
                payload,
                payloadJson,
                exception
        );
    }
}
