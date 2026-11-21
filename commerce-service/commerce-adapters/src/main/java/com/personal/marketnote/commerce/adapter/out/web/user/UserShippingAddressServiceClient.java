package com.personal.marketnote.commerce.adapter.out.web.user;

import com.personal.marketnote.commerce.port.out.user.UpdateUserShippingAddressDeliveryRequestPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 회원 서비스의 배송지 조회 클라이언트
 *
 * @Author 성효빈
 * @Date 2026-03-27
 * @Description 커머스 서비스에서 회원 서비스의 배송지를 조회합니다.
 */
@ServiceAdapter
@Slf4j
public class UserShippingAddressServiceClient implements UpdateUserShippingAddressDeliveryRequestPort {

    private static final String OPERATION_UPDATE_DELIVERY_REQUEST = "shipping.update_delivery_request";

    private final RestClient restClient;
    private final String userServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ExternalCallLogger externalCallLogger;

    public UserShippingAddressServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${user-service.base-url:http://localhost:8080}") String userServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ExternalCallLogger externalCallLogger
    ) {
        this.restClient = restClientBuilder.build();
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.externalCallLogger = externalCallLogger;
    }

    @Override
    public void updateDeliveryRequest(
            Long shippingAddressId, Long userId, DeliveryRequestType deliveryRequestType, String deliveryRequestMessage
    ) {
        String path = "/api/v1/internal/shipping-addresses/" + shippingAddressId + "/delivery-request";

        URI uri = UriComponentsBuilder.fromUriString(userServiceBaseUrl)
                .path(path)
                .queryParam("userId", userId)
                .build()
                .toUri();

        Map<String, String> body = new HashMap<>();
        body.put("deliveryRequestType", deliveryRequestType.name());
        if (FormatValidator.hasValue(deliveryRequestMessage)) {
            body.put("deliveryRequestMessage", deliveryRequestMessage);
        }

        Map<String, String> tags = Map.of(
                "shippingAddressId", String.valueOf(shippingAddressId),
                "userId", String.valueOf(userId)
        );

        try {
            externalCallLogger.recordVoid(
                    ExternalSystem.USER, OPERATION_UPDATE_DELIVERY_REQUEST, tags,
                    () -> sendUpdateDeliveryRequest(uri, path, body)
            );
        } catch (RestClientException e) {
            log.warn(
                    "fallback applied: 배송 요청사항 업데이트 실패 — 무시. shippingAddressId={}, userId={}",
                    shippingAddressId, userId, e
            );
        }
    }

    private void sendUpdateDeliveryRequest(URI uri, String path, Map<String, String> body) {
        restClient.patch()
                .uri(uri)
                .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "PATCH", path))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

}
