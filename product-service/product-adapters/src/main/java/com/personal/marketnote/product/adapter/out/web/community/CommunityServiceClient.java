package com.personal.marketnote.product.adapter.out.web.community;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.CommunityServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.response.GetProductReviewAggregatesResponse;
import com.personal.marketnote.product.adapter.out.response.ProductReviewAggregateItemResponse;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;
import com.personal.marketnote.product.port.out.review.FindProductReviewAggregatesPort;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
@Slf4j
public class CommunityServiceClient implements FindProductReviewAggregatesPort {
    private static final ProductServiceCommunicationTargetType TARGET_TYPE =
            ProductServiceCommunicationTargetType.PRODUCT_REVIEW_AGGREGATE;
    private static final ProductServiceCommunicationSenderType REQUEST_SENDER =
            ProductServiceCommunicationSenderType.PRODUCT;
    private static final ProductServiceCommunicationSenderType RESPONSE_SENDER =
            ProductServiceCommunicationSenderType.COMMUNITY;

    private final RestClient restClient;
    private final String communityServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    public CommunityServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${community-service.base-url}") String communityServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ServiceCommunicationRecorder serviceCommunicationRecorder,
            ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator
    ) {
        this.restClient = restClientBuilder.build();
        this.communityServiceBaseUrl = communityServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.serviceCommunicationRecorder = serviceCommunicationRecorder;
        this.serviceCommunicationPayloadGenerator = serviceCommunicationPayloadGenerator;
    }

    @Override
    public Map<Long, ProductReviewAggregateResult> findByProductIds(List<Long> productIds) {
        if (FormatValidator.hasNoValue(productIds)) {
            return Map.of();
        }

        URI uri = UriComponentsBuilder
                .fromUriString(communityServiceBaseUrl)
                .path("/api/v1/products/review-aggregates")
                .queryParam("productIds", productIds)
                .build()
                .toUri();

        return sendRequest(uri);
    }

    private Map<Long, ProductReviewAggregateResult> sendRequest(URI uri) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<GetProductReviewAggregatesResponse>> responseEntity =
                        restClient.get()
                                .uri(uri)
                                .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath()))
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });

                if (responseEntity.getStatusCode().isError()) {
                    throw new CommunityServiceRequestFailedException(new IOException("Community service returned error: " + responseEntity.getStatusCode()));
                }

                BaseResponse<GetProductReviewAggregatesResponse> response = responseEntity.getBody();

                if (FormatValidator.hasNoValue(response) || FormatValidator.hasNoValue(response.getContent())) {
                    return Map.of();
                }

                List<ProductReviewAggregateItemResponse> reviewAggregates
                        = response.getContent().reviewAggregates();
                if (FormatValidator.hasNoValue(reviewAggregates)) {
                    return Map.of();
                }

                return reviewAggregates.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> FormatValidator.hasValue(item.productId()))
                        .collect(Collectors.toMap(
                                ProductReviewAggregateItemResponse::productId,
                                item -> new ProductReviewAggregateResult(
                                        item.productId(),
                                        item.totalCount(),
                                        item.averageRating()
                                ),
                                (existing, replacement) -> existing
                        ));
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
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return Map.of();
                }
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to get product review aggregates: {}", uri);
        return Map.of();
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
