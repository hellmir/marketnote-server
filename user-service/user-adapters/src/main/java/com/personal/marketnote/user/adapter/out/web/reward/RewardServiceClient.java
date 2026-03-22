package com.personal.marketnote.user.adapter.out.web.reward;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.RewardServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationSenderType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationTargetType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationType;
import com.personal.marketnote.user.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.user.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.user.utility.ServiceCommunicationRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRED_USER_POINT_AMOUNT;
import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRER_USER_POINT_AMOUNT;
import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class RewardServiceClient implements ModifyUserPointPort {
    private static final UserServiceCommunicationTargetType TARGET_TYPE =
            UserServiceCommunicationTargetType.USER_POINT;
    private static final UserServiceCommunicationSenderType REQUEST_SENDER =
            UserServiceCommunicationSenderType.USER;
    private static final UserServiceCommunicationSenderType RESPONSE_SENDER =
            UserServiceCommunicationSenderType.REWARD;

    @Value("${reward-service.base-url}")
    private String rewardServiceBaseUrl;

    private final RestTemplate restTemplate;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @Override
    public void registerUserPoint(Long userId, String userKey) {
        if (FormatValidator.hasNoValue(userId) || FormatValidator.hasNoValue(userKey)) {
            return;
        }

        URI uri = buildRegisterUserPointUri(userId, userKey);
        HttpHeaders headers = buildHeaders("POST", uri.getPath());
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, Void.class);

                if (FormatValidator.hasValue(response) && response.getStatusCode().is2xxSuccessful()) {
                    return;
                }

                HttpStatusCode statusCode = null;
                if (FormatValidator.hasValue(response)) {
                    statusCode = response.getStatusCode();
                }

                String exception = FormatValidator.hasValue(statusCode) ? statusCode.toString() : "EmptyResponse";
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.POST,
                        uri,
                        null,
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson =
                        serviceCommunicationPayloadGenerator.buildResponsePayloadJson(response, attempt);
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        UserServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        UserServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(
                        "Reward service responded with non-2xx status for userId={}, status={}",
                        userId, statusCode
                );
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.POST,
                        uri,
                        null,
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
                        UserServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        UserServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(
                        "Failed to register user point on reward-service: userId={}, attempt={}, message={}",
                        userId, i + 1, e.getMessage(), e
                );
            }

            sleep(1000);
        }

        throw new RewardServiceRequestFailedException(new IOException());
    }

    @Override
    public void accrueReferralPoints(Long referrerUserId, Long referredUserId) {
        accrueUserPoint(
                referrerUserId,
                REFERRER_USER_POINT_AMOUNT,
                "추천인 코드 등록 적립"
        );

        accrueUserPoint(
                referredUserId,
                REFERRED_USER_POINT_AMOUNT,
                "신규 회원 초대 적립"
        );
    }

    private void accrueUserPoint(Long userId, long amount, String reason) {
        if (FormatValidator.hasNoValue(userId) || amount <= 0) {
            return;
        }

        URI uri = buildUserPointUri(userId);
        HttpHeaders postHeaders = buildHeaders("POST", uri.getPath());

        ensureUserPointExists(uri, postHeaders, userId);

        HttpHeaders patchHeaders = buildHeaders("PATCH", uri.getPath());
        ModifyUserPointRequest request = ModifyUserPointRequest.from(userId, amount, reason);
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, patchHeaders);

        sendRequest(uri, httpEntity, userId, amount);
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
                    UserServiceCommunicationType.REQUEST,
                    requestPayload,
                    requestPayloadJson,
                    exception
            );
            recordCommunication(
                    TARGET_TYPE,
                    String.valueOf(userId),
                    UserServiceCommunicationType.RESPONSE,
                    responsePayload,
                    responsePayloadJson,
                    exception
            );
            log.info("User point registration skipped: userId={}, message={}", userId, e.getMessage());
        }
    }

    private void sendRequest(
            URI uri,
            HttpEntity<ModifyUserPointRequest> httpEntity,
            Long userId,
            long amount
    ) {
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
                        UserServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        UserServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(
                        "Reward service responded with non-2xx status for userId={}, amount={}, status={}",
                        userId, amount, response.getStatusCode()
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
                        UserServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(userId),
                        UserServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(
                        "Failed to accrue user point on reward-service: userId={}, amount={}, attempt={}, message={}",
                        userId, amount, i + 1, e.getMessage(), e
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

    private URI buildRegisterUserPointUri(Long userId, String userKey) {
        return UriComponentsBuilder
                .fromUriString(rewardServiceBaseUrl)
                .path("/api/v1/users/{userId}/points")
                .queryParam("userKey", userKey)
                .buildAndExpand(userId)
                .toUri();
    }

    private HttpHeaders buildHeaders(String httpMethod, String requestPath) {
        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, httpMethod, requestPath);
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

    private record ModifyUserPointRequest(
            String changeType,
            long amount,
            String sourceType,
            Long sourceId,
            String reason
    ) {
        private static final String CHANGE_TYPE_ACCRUAL = "ACCRUAL";
        private static final String SOURCE_TYPE_USER = "USER";

        private static ModifyUserPointRequest from(Long userId, long amount, String reason) {
            return new ModifyUserPointRequest(
                    CHANGE_TYPE_ACCRUAL,
                    Math.abs(amount),
                    SOURCE_TYPE_USER,
                    userId,
                    reason
            );
        }
    }

    private void recordCommunication(
            UserServiceCommunicationTargetType targetType,
            String targetId,
            UserServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        UserServiceCommunicationSenderType sender =
                communicationType == UserServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
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
