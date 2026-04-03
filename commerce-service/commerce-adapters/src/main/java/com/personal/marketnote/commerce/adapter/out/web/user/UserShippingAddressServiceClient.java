package com.personal.marketnote.commerce.adapter.out.web.user;

import com.personal.marketnote.commerce.port.out.user.UpdateUserShippingAddressDeliveryRequestPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
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

    private final RestClient restClient;
    private final String userServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    public UserShippingAddressServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${user-service.base-url:http://localhost:8080}") String userServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder
    ) {
        this.restClient = restClientBuilder.build();
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
    }

    @Override
    public void updateDeliveryRequest(Long shippingAddressId, Long userId, DeliveryRequestType deliveryRequestType, String deliveryRequestMessage) {
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

        try {
            ResponseEntity<Void> responseEntity = restClient.patch()
                    .uri(uri)
                    .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "PATCH", path))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            if (responseEntity.getStatusCode().isError()) {
                log.warn("배송 요청사항 업데이트 실패 - shippingAddressId: {}, userId: {}, status: {}",
                        shippingAddressId, userId, responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("배송 요청사항 업데이트 실패 - shippingAddressId: {}, userId: {}, error: {}",
                    shippingAddressId, userId, e.getMessage());
        }
    }

}
