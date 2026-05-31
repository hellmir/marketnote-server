package com.personal.marketnote.commerce.adapter.out.web.fulfillment;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.commerce.port.out.fulfillment.GetFulfillmentWorkStatusPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@ServiceAdapter
@Slf4j
public class FulfillmentServiceClient implements GetFulfillmentWorkStatusPort, CancelFulfillmentReleasePort {
    private final RestClient restClient;
    private final String fulfillmentServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    public FulfillmentServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${fulfillment-service.base-url}") String fulfillmentServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder
    ) {
        this.restClient = restClientBuilder.build();
        this.fulfillmentServiceBaseUrl = fulfillmentServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
    }

    @Override
    public String getWorkStatus(Long orderId) {
        URI uri = buildWorkStatusUri(orderId);

        try {
            ResponseEntity<JsonNode> responseEntity = restClient.get()
                    .uri(uri)
                    .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath()))
                    .retrieve()
                    .toEntity(JsonNode.class);

            if (responseEntity.getStatusCode().isError()) {
                throw new FulfillmentServiceRequestFailedException(
                        new IOException("Fulfillment service returned error: " + responseEntity.getStatusCode()));
            }

            if (responseEntity.getStatusCode().is2xxSuccessful()
                    && FormatValidator.hasValue(responseEntity.getBody())) {
                return responseEntity.getBody().path("content").path("workStatus").asText();
            }

            throw new FulfillmentServiceRequestFailedException(
                    new IOException("풀필먼트 작업 상태 조회 비정상 응답 - orderId: " + orderId));
        } catch (FulfillmentServiceRequestFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("풀필먼트 작업 상태 조회 실패 - orderId: {}", orderId, e);
            throw new FulfillmentServiceRequestFailedException(new IOException(e));
        }
    }

    @Override
    public CancelFulfillmentReleaseResult cancelRelease(Long orderId) {
        URI uri = buildCancelReleaseUri();

        try {
            ResponseEntity<JsonNode> responseEntity = restClient.post()
                    .uri(uri)
                    .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "POST", uri.getPath()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("orderId", orderId))
                    .retrieve()
                    .toEntity(JsonNode.class);

            if (responseEntity.getStatusCode().isError()) {
                throw new FulfillmentServiceRequestFailedException(
                        new IOException("Fulfillment service returned error: " + responseEntity.getStatusCode()));
            }

            if (responseEntity.getStatusCode().is2xxSuccessful()
                    && FormatValidator.hasValue(responseEntity.getBody())) {
                JsonNode content = responseEntity.getBody().path("content");
                return new CancelFulfillmentReleaseResult(
                        content.path("orderId").asLong(),
                        content.path("cancelled").asBoolean(),
                        content.path("message").asText()
                );
            }

            throw new FulfillmentServiceRequestFailedException(
                    new IOException("풀필먼트 출고 취소 비정상 응답 - orderId: " + orderId));
        } catch (FulfillmentServiceRequestFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("풀필먼트 출고 취소 실패 - orderId: {}", orderId, e);
            throw new FulfillmentServiceRequestFailedException(new IOException(e));
        }
    }

    private URI buildWorkStatusUri(Long orderId) {
        return UriComponentsBuilder.fromHttpUrl(fulfillmentServiceBaseUrl)
                .path("/api/v1/internal/fulfillment/deliveries/work-status")
                .queryParam("order-id", orderId)
                .build()
                .toUri();
    }

    private URI buildCancelReleaseUri() {
        return UriComponentsBuilder.fromHttpUrl(fulfillmentServiceBaseUrl)
                .path("/api/v1/internal/fulfillment/deliveries/cancel")
                .build()
                .toUri();
    }
}
