package com.personal.marketnote.community.adapter.out.web.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.ProductServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.web.product.response.OrderedProductsResponse;
import com.personal.marketnote.community.adapter.out.web.product.response.ProductsInfoResponse;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.community.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.community.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_MAX_REQUEST_COUNT;

@ServiceAdapter
@Slf4j
public class ProductServiceClient implements FindProductByPricePolicyPort {
    private static final CommunityServiceCommunicationTargetType TARGET_TYPE =
            CommunityServiceCommunicationTargetType.PRODUCT_INFO;
    private static final CommunityServiceCommunicationSenderType REQUEST_SENDER =
            CommunityServiceCommunicationSenderType.COMMUNITY;
    private static final CommunityServiceCommunicationSenderType RESPONSE_SENDER =
            CommunityServiceCommunicationSenderType.PRODUCT;
    private static final ParameterizedTypeReference<BaseResponse<OrderedProductsResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final String productServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    public ProductServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${product-service.base-url:http://localhost:8081}") String productServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ServiceCommunicationRecorder serviceCommunicationRecorder,
            ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator
    ) {
        this.restClient = restClientBuilder.build();
        this.productServiceBaseUrl = productServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.serviceCommunicationRecorder = serviceCommunicationRecorder;
        this.serviceCommunicationPayloadGenerator = serviceCommunicationPayloadGenerator;
    }

    @Override
    public Map<Long, ProductInfoResult> findByPricePolicyIds(List<Long> pricePolicyIds) {
        if (FormatValidator.hasNoValue(pricePolicyIds)) {
            return Map.of();
        }

        URI uri = UriComponentsBuilder.fromUriString(productServiceBaseUrl)
                .path("/api/v1/products")
                .queryParam("pricePolicyIds", pricePolicyIds.toArray())
                .queryParam("pageSize", pricePolicyIds.size())
                .build()
                .toUri();

        return sendRequest(uri);
    }

    private Map<Long, ProductInfoResult> sendRequest(URI uri) {
        Exception lastError = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<OrderedProductsResponse>> responseEntity = restClient.get()
                        .uri(uri)
                        .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath()))
                        .retrieve()
                        .toEntity(RESPONSE_TYPE);

                if (responseEntity.getStatusCode().isError()) {
                    throw new ProductServiceRequestFailedException(
                            new IOException("Product service returned error: " + responseEntity.getStatusCode()));
                }

                Map<Long, ProductInfoResult> productInfoResultsByPricePolicyId = new HashMap<>();
                List<ProductsInfoResponse> productsInfo = unboxResponse(responseEntity);
                generateResult(productInfoResultsByPricePolicyId, productsInfo);

                return productInfoResultsByPricePolicyId;
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
                        CommunityServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        null,
                        CommunityServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );

                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    lastError = e;
                }

                if (i < INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    sleepWithJitter();
                }
            }
        }

        log.error("Failed to fetch product info from product-service: {} with error: {}", uri, lastError.getMessage(), lastError);
        return Map.of();
    }

    private List<ProductsInfoResponse> unboxResponse(ResponseEntity<BaseResponse<OrderedProductsResponse>> responseEntity) {
        BaseResponse<OrderedProductsResponse> body = responseEntity.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return List.of();
        }

        OrderedProductsResponse content = body.getContent();
        if (FormatValidator.hasNoValue(content)) {
            return List.of();
        }

        if (FormatValidator.hasNoValue(content.products())) {
            return List.of();
        }

        return content.products().items();
    }

    private void generateResult(Map<Long, ProductInfoResult> productInfoResult, List<ProductsInfoResponse> productsInfo) {
        for (ProductsInfoResponse productInfo : productsInfo) {
            if (FormatValidator.hasNoValue(productInfo) || FormatValidator.hasNoValue(productInfo.pricePolicy())) {
                continue;
            }

            Long policyId = productInfo.getPricePolicyId();
            if (FormatValidator.hasNoValue(policyId) || productInfoResult.containsKey(policyId)) {
                continue;
            }

            productInfoResult.put(
                    policyId,
                    new ProductInfoResult(
                            productInfo.sellerId(),
                            productInfo.name(),
                            productInfo.brandName(),
                            productInfo.pricePolicy(),
                            productInfo.selectedOptions(),
                            productInfo.catalogImage()
                    )
            );
        }
    }

    private void sleepWithJitter() {
        try {
            long jitteredSleepMillis = ThreadLocalRandom.current()
                    .nextLong(Math.max(1L, INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void recordCommunication(
            CommunityServiceCommunicationTargetType targetType,
            String targetId,
            CommunityServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        CommunityServiceCommunicationSenderType sender =
                communicationType == CommunityServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
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
