package com.personal.marketnote.product.adapter.out.web.commerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.request.RegisterInventoryRequest;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.CommerceServiceRequestFailedException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.response.GetInventoriesResponse;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.out.inventory.FindStockPort;
import com.personal.marketnote.product.port.out.inventory.RegisterInventoryPort;
import com.personal.marketnote.product.port.out.result.GetInventoryResult;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class CommerceServiceClient implements RegisterInventoryPort, FindStockPort {
    private static final ProductServiceCommunicationTargetType TARGET_TYPE =
            ProductServiceCommunicationTargetType.INVENTORY;
    private static final ProductServiceCommunicationSenderType REQUEST_SENDER =
            ProductServiceCommunicationSenderType.PRODUCT;
    private static final ProductServiceCommunicationSenderType RESPONSE_SENDER =
            ProductServiceCommunicationSenderType.COMMERCE;

    @Value("${commerce-service.base-url}")
    private String commerceServiceBaseUrl;

    @Value("${spring.jwt.admin-access-token}")
    private String adminAccessToken;

    private final RestTemplate restTemplate;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @Override
    public void registerInventory(Long productId, Long pricePolicyId) {
        URI uri = UriComponentsBuilder
                .fromUriString(commerceServiceBaseUrl)
                .path("/api/v1/inventories")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);
        HttpEntity<RegisterInventoryRequest> httpEntity
                = new HttpEntity<>(new RegisterInventoryRequest(productId, pricePolicyId), headers);

        sendRequest(uri, httpEntity, pricePolicyId);
    }

    public void sendRequest(URI uri, HttpEntity<RegisterInventoryRequest> httpEntity, Long pricePolicyId) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();
        String targetId = FormatValidator.hasValue(pricePolicyId) ? String.valueOf(pricePolicyId) : null;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                restTemplate.postForEntity(uri, httpEntity, Void.class);
                return;
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.POST,
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
                        targetId,
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        targetId,
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to register inventory: {} with error: {}", uri, error.getMessage(), error);
        throw new CommerceServiceRequestFailedException(new IOException());
    }

    @Override
    public Set<GetInventoryResult> findByPricePolicyIds(List<Long> pricePolicyIds) {
        URI uri = UriComponentsBuilder
                .fromUriString(commerceServiceBaseUrl)
                .path("/api/v1/inventories")
                .queryParam("pricePolicyIds", pricePolicyIds)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);

        try {
            return sendRequest(uri, headers).inventories();
        } catch (CommerceServiceRequestFailedException csrfe) {
            return pricePolicyIds.stream()
                    .map(GetInventoryResult::generateResultWithoutStock)
                    .collect(Collectors.toSet());
        }
    }

    public GetInventoriesResponse sendRequest(URI uri, HttpHeaders headers) {
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<GetInventoriesResponse>> responseEntity =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<BaseResponse<GetInventoriesResponse>>() {
                                }
                        );
                BaseResponse<GetInventoriesResponse> response = responseEntity.getBody();

                if (FormatValidator.hasNoValue(response)) {
                    throw new CommerceServiceRequestFailedException(new IOException());
                }

                GetInventoriesResponse getInventoriesResponse = response.getContent();
                return FormatValidator.hasValue(getInventoriesResponse)
                        ? getInventoriesResponse
                        : new GetInventoriesResponse(new HashSet<>());
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.GET,
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
                        null,
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        null,
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("Failed to get inventories: {} with error: {}", uri, error.getMessage(), error);
        throw new CommerceServiceRequestFailedException(new IOException());
    }

    private void recordCommunication(
            ProductServiceCommunicationTargetType targetType,
            String targetId,
            ProductServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        ProductServiceCommunicationSenderType sender =
                communicationType == ProductServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
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
