package com.personal.marketnote.commerce.adapter.out.web.product;

import com.personal.marketnote.commerce.adapter.out.web.product.response.ShippingPoliciesBySellerIdsResponse;
import com.personal.marketnote.commerce.adapter.out.web.product.response.ShippingPoliciesBySellerIdsResponse.ShippingPolicyBySellerResponse;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.ProductServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

/**
 * 상품 서비스의 배송비 정책 배치 조회 클라이언트
 *
 * @Author 성효빈
 * @Date 2026-03-19
 * @Description 커머스 서비스에서 상품 서비스의 배송비 정책을 조회합니다.
 */
@ServiceAdapter
@Slf4j
public class ShippingPolicyServiceClient implements FindShippingPolicyBySellerIdsPort {

    private static final ParameterizedTypeReference<BaseResponse<ShippingPoliciesBySellerIdsResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final String productServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    public ShippingPolicyServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${product-service.base-url:http://localhost:8081}") String productServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder
    ) {
        this.restClient = restClientBuilder.build();
        this.productServiceBaseUrl = productServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
    }

    @Override
    public Map<Long, ShippingPolicyInfoResult> findBySellerIds(List<Long> sellerIds) {
        if (FormatValidator.hasNoValue(sellerIds)) {
            return Map.of();
        }

        URI uri = UriComponentsBuilder.fromUriString(productServiceBaseUrl)
                .path("/api/v1/internal/shipping-policies/sellers")
                .queryParam("sellerIds", sellerIds.toArray())
                .build()
                .toUri();

        return sendRequest(uri);
    }

    private Map<Long, ShippingPolicyInfoResult> sendRequest(URI uri) {
        Exception lastError = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            try {
                ResponseEntity<BaseResponse<ShippingPoliciesBySellerIdsResponse>> responseEntity = restClient.get()
                        .uri(uri)
                        .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath()))
                        .retrieve()
                        .toEntity(RESPONSE_TYPE);

                if (responseEntity.getStatusCode().isError()) {
                    throw new ProductServiceRequestFailedException(
                            new IOException("Product service returned error: " + responseEntity.getStatusCode()));
                }

                return parseResponse(responseEntity);
            } catch (Exception e) {
                log.warn("배송비 정책 조회 실패 (attempt {}/{}): {}", i + 1, INTER_SERVER_MAX_REQUEST_COUNT, e.getMessage());
                lastError = e;

                if (i < INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    sleepWithJitter();
                }
            }
        }

        log.error("배송비 정책 조회 최종 실패 - URI: {}, error: {}", uri, lastError.getMessage(), lastError);
        return Map.of();
    }

    private Map<Long, ShippingPolicyInfoResult> parseResponse(
            ResponseEntity<BaseResponse<ShippingPoliciesBySellerIdsResponse>> responseEntity
    ) {
        BaseResponse<ShippingPoliciesBySellerIdsResponse> body = responseEntity.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return Map.of();
        }

        ShippingPoliciesBySellerIdsResponse content = body.getContent();
        if (FormatValidator.hasNoValue(content)) {
            return Map.of();
        }

        List<ShippingPolicyBySellerResponse> policies = content.shippingPolicies();
        if (FormatValidator.hasNoValue(policies)) {
            return Map.of();
        }

        Map<Long, ShippingPolicyInfoResult> result = new HashMap<>();
        for (ShippingPolicyBySellerResponse policy : policies) {
            if (FormatValidator.hasNoValue(policy.sellerId())) {
                continue;
            }
            result.put(policy.sellerId(), new ShippingPolicyInfoResult(
                    policy.sellerId(),
                    policy.shippingFee(),
                    policy.freeShippingThreshold()
            ));
        }

        return result;
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
}
