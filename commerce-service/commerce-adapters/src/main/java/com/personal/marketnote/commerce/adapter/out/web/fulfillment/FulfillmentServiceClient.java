package com.personal.marketnote.commerce.adapter.out.web.fulfillment;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.commerce.port.out.fulfillment.GetFulfillmentWorkStatusPort;
import com.personal.marketnote.commerce.port.out.fulfillment.GetReturnInspectionResultPort;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult.ReturnInspectionGoodsItem;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult.ReturnInspectionResultItem;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryResult;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ServiceAdapter
@Slf4j
public class FulfillmentServiceClient implements GetFulfillmentWorkStatusPort, CancelFulfillmentReleasePort, RegisterFulfillmentReturnDeliveryPort, GetReturnInspectionResultPort {
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

    @Override
    public RegisterFulfillmentReturnDeliveryResult registerReturnDelivery(RegisterFulfillmentReturnDeliveryCommand command) {
        URI uri = buildReturnDeliveryUri();

        Map<String, Object> body = buildReturnDeliveryBody(command);

        try {
            ResponseEntity<JsonNode> responseEntity = restClient.post()
                    .uri(uri)
                    .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "POST", uri.getPath()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(JsonNode.class);

            if (responseEntity.getStatusCode().isError()) {
                throw new FulfillmentServiceRequestFailedException(
                        new IOException("Fulfillment service returned error: " + responseEntity.getStatusCode()));
            }

            if (responseEntity.getStatusCode().is2xxSuccessful()
                    && FormatValidator.hasValue(responseEntity.getBody())) {
                JsonNode content = responseEntity.getBody().path("content");
                return RegisterFulfillmentReturnDeliveryResult.of(
                        content.path("orderId").asLong(),
                        content.path("returnSlipNumber").asText(null),
                        content.path("registered").asBoolean(),
                        content.path("message").asText(null)
                );
            }

            throw new FulfillmentServiceRequestFailedException(
                    new IOException("풀필먼트 반품 등록 비정상 응답 - orderId: " + command.orderId()));
        } catch (FulfillmentServiceRequestFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("풀필먼트 반품 등록 실패 - orderId: {}", command.orderId(), e);
            throw new FulfillmentServiceRequestFailedException(new IOException(e));
        }
    }

    @Override
    public ReturnInspectionResult getReturnGodDetail(String returnSlipNumbers) {
        URI uri = buildReturnGodDetailUri(returnSlipNumbers);

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
                return parseReturnGodDetailResponse(responseEntity.getBody().path("content"));
            }

            throw new FulfillmentServiceRequestFailedException(
                    new IOException("풀필먼트 반품 검수 상태 조회 비정상 응답"));
        } catch (FulfillmentServiceRequestFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("풀필먼트 반품 검수 상태 조회 실패 - returnSlipNumbers: {}", returnSlipNumbers, e);
            throw new FulfillmentServiceRequestFailedException(new IOException(e));
        }
    }

    private ReturnInspectionResult parseReturnGodDetailResponse(JsonNode content) {
        Integer dataCount = content.has("dataCount") ? content.path("dataCount").asInt() : null;
        JsonNode returnGodInfosNode = content.path("returnGodInfos");

        List<ReturnInspectionResultItem> items = new ArrayList<>();
        if (returnGodInfosNode.isArray()) {
            for (JsonNode infoNode : returnGodInfosNode) {
                List<ReturnInspectionGoodsItem> goods = new ArrayList<>();
                JsonNode productsNode = infoNode.path("products");
                if (productsNode.isArray()) {
                    for (JsonNode goodsNode : productsNode) {
                        goods.add(new ReturnInspectionGoodsItem(
                                goodsNode.path("customerProductCode").asText(null),
                                goodsNode.path("productName").asText(null),
                                goodsNode.path("returnProductCheckStatus").asText(null),
                                goodsNode.path("returnProductCheckStatusName").asText(null)
                        ));
                    }
                }
                items.add(new ReturnInspectionResultItem(
                        infoNode.path("orderNumber").asText(null),
                        infoNode.path("inboundOrderSlipNumber").asText(null),
                        goods
                ));
            }
        }

        return new ReturnInspectionResult(dataCount, items);
    }

    private URI buildReturnGodDetailUri(String returnSlipNumbers) {
        return UriComponentsBuilder.fromHttpUrl(fulfillmentServiceBaseUrl)
                .path("/api/v1/internal/fulfillment/deliveries/return-god-detail")
                .queryParam("return-slip-numbers", returnSlipNumbers)
                .build()
                .toUri();
    }

    private Map<String, Object> buildReturnDeliveryBody(RegisterFulfillmentReturnDeliveryCommand command) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderId", command.orderId());
        body.put("orderDate", command.orderDate());
        body.put("recipientName", command.recipientName());
        body.put("recipientPhoneNumber", command.recipientPhoneNumber());
        body.put("recipientAddress", command.recipientAddress());
        body.put("pickupRecipientName", command.pickupRecipientName());
        body.put("pickupRecipientPhoneNumber", command.pickupRecipientPhoneNumber());
        body.put("pickupZipCode", command.pickupZipCode());
        body.put("pickupAddress", command.pickupAddress());
        body.put("pickupAddressDetail", command.pickupAddressDetail());
        body.put("returnReason", command.returnReason());
        body.put("returnDetailReason", command.returnDetailReason());
        body.put("returnShippingRequest", command.returnShippingRequest());

        if (FormatValidator.hasValue(command.products())) {
            List<Map<String, Object>> products = command.products().stream()
                    .map(item -> {
                        Map<String, Object> productMap = new LinkedHashMap<>();
                        productMap.put("productCode", item.productCode());
                        productMap.put("quantity", item.quantity());
                        return productMap;
                    })
                    .toList();
            body.put("products", products);
        }

        return body;
    }

    private URI buildReturnDeliveryUri() {
        return UriComponentsBuilder.fromHttpUrl(fulfillmentServiceBaseUrl)
                .path("/api/v1/internal/fulfillment/deliveries/return")
                .build()
                .toUri();
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
