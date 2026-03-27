package com.personal.marketnote.commerce.adapter.out.web.user;

import com.personal.marketnote.commerce.adapter.out.web.user.response.ShippingAddressInfoResponse;
import com.personal.marketnote.commerce.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.commerce.port.out.result.user.ShippingAddressInfoResult;
import com.personal.marketnote.commerce.port.out.user.FindUserShippingAddressPort;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
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

import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_MAX_REQUEST_COUNT;

/**
 * 회원 서비스의 배송지 조회 클라이언트
 *
 * @Author 성효빈
 * @Date 2026-03-27
 * @Description 커머스 서비스에서 회원 서비스의 배송지를 조회합니다.
 */
@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class UserShippingAddressServiceClient implements FindUserShippingAddressPort {

    private static final ParameterizedTypeReference<BaseResponse<ShippingAddressInfoResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Value("${user-service.base-url:http://localhost:8080}")
    private String userServiceBaseUrl;

    private final RestTemplate restTemplate;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @Override
    public ShippingAddressInfoResult findByIdAndUserId(Long shippingAddressId, Long userId) {
        String path = "/api/v1/internal/shipping-addresses/" + shippingAddressId;

        URI uri = UriComponentsBuilder.fromUriString(userServiceBaseUrl)
                .path(path)
                .queryParam("userId", userId)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", path);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        return sendRequest(uri, request, shippingAddressId);
    }

    private ShippingAddressInfoResult sendRequest(URI uri, HttpEntity<Void> request, Long shippingAddressId) {
        Exception lastError = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            try {
                ResponseEntity<BaseResponse<ShippingAddressInfoResponse>> response = restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        request,
                        RESPONSE_TYPE
                );

                return parseResponse(response, shippingAddressId);
            } catch (Exception e) {
                log.warn("배송지 조회 실패 (attempt {}/{}): {}", i + 1, INTER_SERVER_MAX_REQUEST_COUNT, e.getMessage());
                lastError = e;

                if (i < INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    sleepWithJitter();
                }
            }
        }

        log.error("배송지 조회 최종 실패 - URI: {}, error: {}", uri, lastError.getMessage(), lastError);
        throw new ShippingAddressNotFoundException(shippingAddressId);
    }

    private ShippingAddressInfoResult parseResponse(
            ResponseEntity<BaseResponse<ShippingAddressInfoResponse>> response,
            Long shippingAddressId
    ) {
        BaseResponse<ShippingAddressInfoResponse> body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            throw new ShippingAddressNotFoundException(shippingAddressId);
        }

        ShippingAddressInfoResponse content = body.getContent();
        if (FormatValidator.hasNoValue(content)) {
            throw new ShippingAddressNotFoundException(shippingAddressId);
        }

        return new ShippingAddressInfoResult(
                content.recipientName(),
                content.recipientPhoneNumber(),
                content.address(),
                content.addressDetail()
        );
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
