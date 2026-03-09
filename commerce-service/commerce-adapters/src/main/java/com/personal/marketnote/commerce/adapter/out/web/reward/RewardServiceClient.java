package com.personal.marketnote.commerce.adapter.out.web.reward;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
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
    private static final String SOURCE_TYPE_ORDER = "ORDER";
    private static final String CHANGE_TYPE_ACCRUAL = "ACCRUAL";
    private static final String CHANGE_TYPE_DEDUCTION = "DEDUCTION";
    private static final String SHARE_PURCHASE_REASON = "링크 공유 회원 상품 구매";
    private static final String ORDER_POINT_DEDUCTION_REASON = "주문 포인트 사용";
    private static final String ORDER_POINT_REFUND_REASON = "주문 취소 포인트 환불";
    private static final String PRODUCT_ACCUMULATION_REASON = "상품 구매 적립";
    private static final String PENDING_POINT_CONFIRM_REASON = "구매 확정 포인트 적립";
    private static final String PENDING_POINT_CANCEL_REASON = "결제 취소 적립 예정 포인트 회수";
    private static final String PARTIAL_CANCEL_PRODUCT_ACCUMULATION_REASON = "부분 결제 취소 상품 적립 포인트 차감";
    private static final String PARTIAL_CANCEL_SHARED_PURCHASE_REASON = "부분 결제 취소 링크 공유 적립 포인트 차감";

    @Value("${reward-service.base-url}")
    private String rewardServiceBaseUrl;

    @Value("${spring.jwt.admin-access-token}")
    private String adminAccessToken;

    @Value("${reward-service.share-point-rate}")
    private float sharePointRate;

    private final RestTemplate restTemplate;

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

        sendRequestWithRetry(uri, httpEntity, HttpMethod.PATCH, userId, "포인트 변경");
    }

    @Override
    public void refundOrderPoints(Long userId, Long amount, Long orderId) {
        if (FormatValidator.hasNoValue(amount) || amount <= 0) {
            return;
        }

        URI uri = buildUserPointUri(userId);
        HttpHeaders headers = buildHeaders();

        ModifyUserPointRequest request = ModifyUserPointRequest.refund(amount, orderId);
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.PATCH, userId, "포인트 변경");
    }

    @Override
    public void addPendingProductAccumulationPoints(Long userId, Long amount, Long orderId) {
        if (FormatValidator.hasNoValue(amount) || amount <= 0) {
            return;
        }

        URI uri = buildPendingPointUri(userId);
        HttpHeaders headers = buildHeaders();

        ModifyUserPointRequest request = ModifyUserPointRequest.pendingAccrual(amount, orderId, PRODUCT_ACCUMULATION_REASON);
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.PATCH, userId, "포인트 변경");
    }

    @Override
    public void confirmPendingPoints(Long userId, Long orderId) {
        if (FormatValidator.hasNoValue(userId) || FormatValidator.hasNoValue(orderId)) {
            return;
        }

        URI uri = buildPendingPointConfirmUri(userId);
        HttpHeaders headers = buildHeaders();

        ConfirmPendingPointRequest request = new ConfirmPendingPointRequest(
                SOURCE_TYPE_ORDER, orderId, PENDING_POINT_CONFIRM_REASON
        );
        HttpEntity<ConfirmPendingPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.POST, userId, "적립 예정 포인트 확정");
    }

    @Override
    public void revokePendingPoints(Long userId, Long orderId) {
        if (FormatValidator.hasNoValue(userId) || FormatValidator.hasNoValue(orderId)) {
            return;
        }

        URI uri = buildPendingPointCancelUri(userId);
        HttpHeaders headers = buildHeaders();

        CancelPendingPointRequest request = new CancelPendingPointRequest(
                SOURCE_TYPE_ORDER, orderId, PENDING_POINT_CANCEL_REASON
        );
        HttpEntity<CancelPendingPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.POST, userId, "적립 예정 포인트 취소");
    }

    @Override
    public void reducePartialPendingPoints(Long userId, Long amount, Long orderId) {
        if (FormatValidator.hasNoValue(amount) || amount <= 0) {
            return;
        }

        URI uri = buildPendingPointUri(userId);
        HttpHeaders headers = buildHeaders();

        ModifyUserPointRequest request = ModifyUserPointRequest.pendingDeduction(
                amount, orderId, PARTIAL_CANCEL_PRODUCT_ACCUMULATION_REASON
        );
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.PATCH, userId, "부분 취소 적립 예정 포인트 차감");
    }

    @Override
    public void reducePartialPendingSharedPurchasePoints(List<Long> sharerIds, Long paymentAmount, Long cancelAmount, Long orderId) {
        if (FormatValidator.hasNoValue(sharerIds)
                || FormatValidator.hasNoValue(paymentAmount) || paymentAmount <= 0
                || FormatValidator.hasNoValue(cancelAmount) || cancelAmount <= 0) {
            return;
        }

        long originalSharePoint = Math.round(paymentAmount * sharePointRate);
        if (originalSharePoint <= 0) {
            return;
        }

        long numerator = Math.multiplyExact(cancelAmount, originalSharePoint);
        long proportionalPoint = (numerator + paymentAmount / 2) / paymentAmount;
        if (proportionalPoint <= 0) {
            return;
        }

        sharerIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(sharerId -> reduceSharerPartialPendingPoint(sharerId, proportionalPoint, orderId));
    }

    private void reduceSharerPartialPendingPoint(Long sharerId, Long amount, Long orderId) {
        URI uri = buildPendingPointUri(sharerId);
        HttpHeaders headers = buildHeaders();

        ModifyUserPointRequest request = ModifyUserPointRequest.pendingDeduction(
                amount, orderId, PARTIAL_CANCEL_SHARED_PURCHASE_REASON
        );
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.PATCH, sharerId, "부분 취소 공유 적립 예정 포인트 차감");
    }

    @Override
    public void revokePendingSharedPurchasePoints(List<Long> sharerIds, Long orderId) {
        if (FormatValidator.hasNoValue(sharerIds) || FormatValidator.hasNoValue(orderId)) {
            return;
        }

        sharerIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(sharerId -> revokeSharerPendingPoint(sharerId, orderId));
    }

    private void revokeSharerPendingPoint(Long sharerId, Long orderId) {
        URI uri = buildPendingPointCancelUri(sharerId);
        HttpHeaders headers = buildHeaders();

        CancelPendingPointRequest request = new CancelPendingPointRequest(
                SOURCE_TYPE_ORDER, orderId, PENDING_POINT_CANCEL_REASON
        );
        HttpEntity<CancelPendingPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.POST, sharerId, "공유 적립 예정 포인트 취소");
    }

    @Override
    public void addPendingSharedPurchasePoints(List<Long> sharerIds, Long totalAmount, Long orderId) {
        if (FormatValidator.hasNoValue(sharerIds) || FormatValidator.hasNoValue(totalAmount)) {
            return;
        }

        sharerIds.stream()
                .filter(Objects::nonNull)
                .forEach(sharerId -> addPendingSharerPoint(sharerId, totalAmount, orderId));
    }

    private void addPendingSharerPoint(Long sharerId, Long totalAmount, Long orderId) {
        if (totalAmount <= 0) {
            return;
        }

        URI uri = buildPendingPointUri(sharerId);
        HttpHeaders headers = buildHeaders();

        long sharePointAmount = Math.round(totalAmount * sharePointRate);
        ModifyUserPointRequest request = ModifyUserPointRequest.pendingAccrual(sharePointAmount, orderId, SHARE_PURCHASE_REASON);
        HttpEntity<ModifyUserPointRequest> httpEntity = new HttpEntity<>(request, headers);

        sendRequestWithRetry(uri, httpEntity, HttpMethod.PATCH, sharerId, "포인트 변경");
    }

    private URI buildPendingPointCancelUri(Long userId) {
        return UriComponentsBuilder
                .fromUriString(rewardServiceBaseUrl)
                .path("/api/v1/users/{userId}/points/pending/cancel")
                .buildAndExpand(userId)
                .toUri();
    }

    private URI buildPendingPointConfirmUri(Long userId) {
        return UriComponentsBuilder
                .fromUriString(rewardServiceBaseUrl)
                .path("/api/v1/users/{userId}/points/pending/confirm")
                .buildAndExpand(userId)
                .toUri();
    }

    private URI buildPendingPointUri(Long userId) {
        return UriComponentsBuilder
                .fromUriString(rewardServiceBaseUrl)
                .path("/api/v1/users/{userId}/points/pending")
                .buildAndExpand(userId)
                .toUri();
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

    private <T> void sendRequestWithRetry(URI uri, HttpEntity<T> httpEntity, HttpMethod method,
                                          Long userId, String operationName) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<Void> response = restTemplate.exchange(uri, method, httpEntity, Void.class);
                if (FormatValidator.hasValue(response) && response.getStatusCode().is2xxSuccessful()) {
                    return;
                }

                log.warn("{} 비정상 응답 - userId: {}, attempt: {}, status: {}",
                        operationName, userId, attempt,
                        FormatValidator.hasValue(response) ? response.getStatusCode() : "empty");
            } catch (Exception e) {
                log.warn("{} 요청 실패 - userId: {}, attempt: {}, error: {}",
                        operationName, userId, attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    log.error("{} 최종 실패 - userId: {}", operationName, userId);
                    throw new RewardServiceRequestFailedException(new IOException(e));
                }
            }

            sleep(sleepMillis);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("{} 최종 실패 (비정상 응답) - userId: {}", operationName, userId);
        throw new RewardServiceRequestFailedException(new IOException(operationName + " 최종 실패"));
    }

    private record CancelPendingPointRequest(
            String sourceType,
            Long sourceId,
            String reason
    ) {
    }

    private record ConfirmPendingPointRequest(
            String sourceType,
            Long sourceId,
            String reason
    ) {
    }

    private record ModifyUserPointRequest(
            String changeType,
            long amount,
            String sourceType,
            Long sourceId,
            String reason
    ) {
        private static ModifyUserPointRequest deduction(long amount, Long orderId) {
            return new ModifyUserPointRequest(
                    CHANGE_TYPE_DEDUCTION,
                    Math.abs(amount),
                    SOURCE_TYPE_ORDER,
                    orderId,
                    ORDER_POINT_DEDUCTION_REASON
            );
        }

        private static ModifyUserPointRequest refund(long amount, Long orderId) {
            return new ModifyUserPointRequest(
                    CHANGE_TYPE_ACCRUAL,
                    Math.abs(amount),
                    SOURCE_TYPE_ORDER,
                    orderId,
                    ORDER_POINT_REFUND_REASON
            );
        }

        private static ModifyUserPointRequest pendingAccrual(long amount, Long orderId, String reason) {
            return new ModifyUserPointRequest(
                    CHANGE_TYPE_ACCRUAL,
                    Math.abs(amount),
                    SOURCE_TYPE_ORDER,
                    orderId,
                    reason
            );
        }

        private static ModifyUserPointRequest pendingDeduction(long amount, Long orderId, String reason) {
            return new ModifyUserPointRequest(
                    CHANGE_TYPE_DEDUCTION,
                    Math.abs(amount),
                    SOURCE_TYPE_ORDER,
                    orderId,
                    reason
            );
        }
    }
}
