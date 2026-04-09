package com.personal.marketnote.community.adapter.out.web.commerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
import com.personal.marketnote.community.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.community.port.out.order.FindOrderProductPort;
import com.personal.marketnote.community.port.out.order.VerifyOrderOwnershipPort;
import com.personal.marketnote.community.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.community.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.*;

/**
 * 커머스 서비스 HTTP 클라이언트
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description HMAC 인증 기반 커머스 서비스 통신 어댑터
 */
@ServiceAdapter
@Slf4j
public class CommerceServiceClient implements VerifyOrderOwnershipPort, FindOrderProductPort {
    private static final CommunityServiceCommunicationTargetType TARGET_TYPE =
            CommunityServiceCommunicationTargetType.ORDER_OWNERSHIP;
    private static final CommunityServiceCommunicationSenderType REQUEST_SENDER =
            CommunityServiceCommunicationSenderType.COMMUNITY;
    private static final CommunityServiceCommunicationSenderType RESPONSE_SENDER =
            CommunityServiceCommunicationSenderType.COMMERCE;

    private final RestClient restClient;
    private final String commerceServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    public CommerceServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${commerce-service.base-url}") String commerceServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ServiceCommunicationRecorder serviceCommunicationRecorder,
            ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator
    ) {
        this.restClient = restClientBuilder.build();
        this.commerceServiceBaseUrl = commerceServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.serviceCommunicationRecorder = serviceCommunicationRecorder;
        this.serviceCommunicationPayloadGenerator = serviceCommunicationPayloadGenerator;
    }

    @Override
    public void verifyOrderOwnership(Long orderId, Long buyerId) {
        URI uri = UriComponentsBuilder
                .fromUriString(commerceServiceBaseUrl)
                .path("/api/v1/internal/orders/{orderId}/ownership")
                .queryParam("buyerId", buyerId)
                .buildAndExpand(orderId)
                .toUri();

        sendVerifyRequest(uri, orderId);
    }

    private void sendVerifyRequest(URI uri, Long orderId) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        String targetId = String.valueOf(orderId);

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                restClient.get()
                        .uri(uri)
                        .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath()))
                        .retrieve()
                        .toBodilessEntity();

                return;
            } catch (HttpClientErrorException hce) {
                if (hce.getStatusCode().value() == 403) {
                    throw new UnauthorizedOrderAccessException();
                }

                // 4xx 클라이언트 에러는 재시도해도 결과가 동일하므로 즉시 실패
                recordError(targetId, uri, attempt, hce);
                log.warn("커머스 서비스 주문 소유권 검증 클라이언트 에러 - orderId: {}, status: {}",
                        orderId, hce.getStatusCode().value(), hce);
                throw hce;
            } catch (Exception e) {
                recordError(targetId, uri, attempt, e);
                log.warn("커머스 서비스 주문 소유권 검증 통신 오류 - orderId: {}, attempt: {}",
                        orderId, attempt, e);

                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    throw e;
                }

                sleepWithJitter(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }
    }

    private void recordError(String targetId, URI uri, int attempt, Exception e) {
        String exception = e.getClass().getSimpleName();
        JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                HttpMethod.GET, uri, null, attempt
        );
        JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                exception, e.getMessage(), attempt
        );

        serviceCommunicationRecorder.record(
                TARGET_TYPE,
                CommunityServiceCommunicationType.REQUEST,
                REQUEST_SENDER,
                targetId,
                requestPayloadJson.toString(),
                requestPayloadJson,
                exception
        );
        serviceCommunicationRecorder.record(
                TARGET_TYPE,
                CommunityServiceCommunicationType.RESPONSE,
                RESPONSE_SENDER,
                targetId,
                responsePayloadJson.toString(),
                responsePayloadJson,
                exception
        );
    }

    @Override
    public Optional<Long> findUnitAmountByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId) {
        URI uri = UriComponentsBuilder
                .fromUriString(commerceServiceBaseUrl)
                .path("/api/v1/internal/orders/{orderId}/order-products/{pricePolicyId}")
                .buildAndExpand(orderId, pricePolicyId)
                .toUri();

        String targetId = orderId + ":" + pricePolicyId;

        try {
            JsonNode responseBody = restClient.get()
                    .uri(uri)
                    .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath()))
                    .retrieve()
                    .body(JsonNode.class);

            if (responseBody == null || !responseBody.has("content")) {
                return Optional.empty();
            }

            JsonNode content = responseBody.get("content");
            if (content.isNull() || !content.has("unitAmount")) {
                return Optional.empty();
            }

            return Optional.of(content.get("unitAmount").asLong());
        } catch (Exception e) {
            recordOrderProductError(targetId, uri, e);
            log.warn("커머스 서비스 주문 상품 조회 실패 - orderId: {}, pricePolicyId: {}", orderId, pricePolicyId, e);
            return Optional.empty();
        }
    }

    private void recordOrderProductError(String targetId, URI uri, Exception e) {
        String exception = e.getClass().getSimpleName();
        JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                HttpMethod.GET, uri, null, 1
        );
        JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                exception, e.getMessage(), 1
        );

        serviceCommunicationRecorder.record(
                CommunityServiceCommunicationTargetType.ORDER_PRODUCT,
                CommunityServiceCommunicationType.REQUEST,
                REQUEST_SENDER,
                targetId,
                requestPayloadJson.toString(),
                requestPayloadJson,
                exception
        );
        serviceCommunicationRecorder.record(
                CommunityServiceCommunicationTargetType.ORDER_PRODUCT,
                CommunityServiceCommunicationType.RESPONSE,
                RESPONSE_SENDER,
                targetId,
                responsePayloadJson.toString(),
                responsePayloadJson,
                exception
        );
    }

    private void sleepWithJitter(long sleepMillis) {
        try {
            long jitteredSleepMillis = ThreadLocalRandom.current()
                    .nextLong(Math.max(1L, sleepMillis) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
