package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.http.client.CommunicationFailureHandler;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FasstoAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.exception.GetFasstoReturnGodDetailFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFasstoReturnDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.result.vendor.*;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFasstoReturnGodDetailPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoReturnDeliveryPort;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@VendorAdapter
@Slf4j
public class FasstoReturnDeliveryClient implements RegisterFasstoReturnDeliveryPort, GetFasstoReturnGodDetailPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FasstoAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;

    public FasstoReturnDeliveryClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            FasstoAuthProperties properties,
            VendorCommunicationRecorder vendorCommunicationRecorder,
            VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator,
            VendorCommunicationFailureHandler vendorCommunicationFailureHandler
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.vendorCommunicationRecorder = vendorCommunicationRecorder;
        this.vendorCommunicationPayloadGenerator = vendorCommunicationPayloadGenerator;
        this.vendorCommunicationFailureHandler = vendorCommunicationFailureHandler;
    }

    @Override
    public RegisterFasstoDeliveryResult registerReturnDelivery(FasstoReturnDeliveryMapper request) {
        RegisterFasstoDeliveryResponse response = executeReturnDeliveryMutation(request, "REGISTER_RETURN");
        return mapDeliveryResult(response);
    }

    @Override
    public GetFasstoReturnGodDetailResult getReturnGodDetail(FasstoReturnGodDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto return god detail query is required.");
        }

        URI uri = buildReturnGodDetailUri(query);

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.RETURN_DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildReturnGodDetailRequestPayloadJson(query, uri, attempt);
            String requestPayload = requestPayloadJson.toString();

            ResponseEntity<String> response;
            try {
                response = restClient.get()
                        .uri(uri)
                        .headers(h -> h.addAll(buildHeaders(query.getAccessToken(), false)))
                        .retrieve()
                        .toEntity(String.class);
            } catch (Exception e) {
                Map<String, Object> errorPayload = new LinkedHashMap<>();
                errorPayload.put("error", e.getClass().getSimpleName());
                errorPayload.put("message", e.getMessage());
                errorPayload.put("attempt", attempt);

                vendorCommunicationFailureHandler.handleFailure(
                        targetType,
                        vendorName,
                        requestPayload,
                        requestPayloadJson,
                        errorPayload,
                        e
                );

                String vendorMessage = resolveVendorMessageFromException(e);
                if (FormatValidator.hasValue(vendorMessage)) {
                    failureMessage = vendorMessage;
                    error = new Exception(vendorMessage);
                }

                log.warn("Failed to get Fassto return god detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoReturnGodDetailListResponse parsedResponse = parseReturnGodDetailResponse(response);
            boolean isSuccess = isReturnGodDetailSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveReturnGodDetailException(response, parsedResponse);

            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.REQUEST,
                    FulfillmentVendorCommunicationSenderType.SERVER,
                    vendorName,
                    requestPayload,
                    requestPayloadJson,
                    exception
            );
            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.RESPONSE,
                    FulfillmentVendorCommunicationSenderType.VENDOR,
                    vendorName,
                    responsePayload,
                    responsePayloadJson,
                    exception
            );

            if (isSuccess) {
                return mapReturnGodDetailResult(parsedResponse);
            }

            String vendorMessage = resolveReturnGodDetailVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fassto return god detail request failed: attempt={}, status={}, exception={}",
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleep(sleepMillis);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to get Fassto return god detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoReturnGodDetailFailedException(failureMessage, new IOException(error));
    }

    // ── 반품 등록 (POST) ──

    private RegisterFasstoDeliveryResponse executeReturnDeliveryMutation(
            FasstoReturnDeliveryMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fassto return delivery request is required.");
        }

        URI uri = buildReturnDeliveryUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.RETURN_DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildRequestPayloadJson(request, uri, attempt, action);
            String requestPayload = requestPayloadJson.toString();

            ResponseEntity<String> response;
            try {
                response = restClient.post()
                        .uri(uri)
                        .headers(h -> h.addAll(buildHeaders(request.getAccessToken())))
                        .body(request.toPayload())
                        .retrieve()
                        .toEntity(String.class);
            } catch (Exception e) {
                Map<String, Object> errorPayload = new LinkedHashMap<>();
                errorPayload.put("error", e.getClass().getSimpleName());
                errorPayload.put("message", e.getMessage());
                errorPayload.put("attempt", attempt);

                vendorCommunicationFailureHandler.handleFailure(
                        targetType,
                        vendorName,
                        requestPayload,
                        requestPayloadJson,
                        errorPayload,
                        e
                );

                String vendorMessage = resolveVendorMessageFromException(e);
                if (FormatValidator.hasValue(vendorMessage)) {
                    failureMessage = vendorMessage;
                    error = new Exception(vendorMessage);
                }

                log.warn("Failed to {} Fassto return delivery: attempt={}, message={}",
                        action.toLowerCase(),
                        attempt,
                        e.getMessage(),
                        e
                );
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            RegisterFasstoDeliveryResponse parsedResponse = parseResponseBody(response);
            boolean isSuccess = isSuccessResponse(response, parsedResponse);
            String exception = isSuccess
                    ? null
                    : resolveResponseException(response, parsedResponse);

            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.REQUEST,
                    FulfillmentVendorCommunicationSenderType.SERVER,
                    vendorName,
                    requestPayload,
                    requestPayloadJson,
                    exception
            );
            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.RESPONSE,
                    FulfillmentVendorCommunicationSenderType.VENDOR,
                    vendorName,
                    responsePayload,
                    responsePayloadJson,
                    exception
            );

            if (isSuccess) {
                return parsedResponse;
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fassto return delivery {} failed: attempt={}, status={}, exception={}",
                    action.toLowerCase(),
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleep(sleepMillis);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to {} Fassto return delivery request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        throw new RegisterFasstoReturnDeliveryFailedException(failureMessage, new IOException(error));
    }

    // ── URI 빌드 ──

    private URI buildReturnDeliveryUri(String customerCode) {
        validateReturnDeliveryProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getReturnDeliveryPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildReturnGodDetailUri(FasstoReturnGodDetailQuery query) {
        validateReturnGodDetailProperties();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getReturnGodDetailPath());
        if (FormatValidator.hasValue(query.getStartDate())) {
            builder.queryParam("strDt", query.getStartDate());
        }
        if (FormatValidator.hasValue(query.getEndDate())) {
            builder.queryParam("endDt", query.getEndDate());
        }
        if (FormatValidator.hasValue(query.getRtnSlipNoList())) {
            builder.queryParam("rtnSlipNoList", query.getRtnSlipNoList());
        }
        if (FormatValidator.hasValue(query.getWhCd())) {
            builder.queryParam("whCd", query.getWhCd());
        }
        return builder
                .buildAndExpand(query.getCustomerCode())
                .toUri();
    }

    // ── 헤더 빌드 ──

    private HttpHeaders buildHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(ACCESS_TOKEN_HEADER, accessToken);
        return headers;
    }

    private HttpHeaders buildHeaders(String accessToken, boolean withContentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (withContentType) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        headers.add(ACCESS_TOKEN_HEADER, accessToken);
        return headers;
    }

    // ── 설정 검증 ──

    private void validateReturnDeliveryProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getReturnDeliveryPath())) {
            throw new IllegalStateException("Fassto return delivery path is required.");
        }
        if (!properties.getReturnDeliveryPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto return delivery path must include {customerCode}.");
        }
    }

    private void validateReturnGodDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getReturnGodDetailPath())) {
            throw new IllegalStateException("Fassto return god detail path is required.");
        }
        if (!properties.getReturnGodDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto return god detail path must include {customerCode}.");
        }
    }

    // ── 반품 등록 응답 파싱/매핑 ──

    private RegisterFasstoDeliveryResponse parseResponseBody(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, RegisterFasstoDeliveryResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto return delivery response: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isSuccessResponse(ResponseEntity<String> response, RegisterFasstoDeliveryResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private String resolveResponseException(ResponseEntity<String> response, RegisterFasstoDeliveryResponse parsedResponse) {
        if (FormatValidator.hasNoValue(response)) {
            return "NO_RESPONSE";
        }
        if (response.getStatusCode().value() != 200) {
            return "HTTP_" + response.getStatusCode().value();
        }
        if (FormatValidator.hasNoValue(parsedResponse)) {
            return "INVALID_RESPONSE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.header()) || !parsedResponse.header().isSuccess()) {
            return "HEADER_FAILURE";
        }
        return "UNKNOWN_FAILURE";
    }

    private RegisterFasstoDeliveryResult mapDeliveryResult(RegisterFasstoDeliveryResponse response) {
        List<RegisterFasstoDeliveryItemResult> deliveries = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return RegisterFasstoDeliveryResult.of(dataCount, deliveries);
    }

    private RegisterFasstoDeliveryItemResult mapDeliveryItem(
            RegisterFasstoDeliveryItemResponse item
    ) {
        return RegisterFasstoDeliveryItemResult.of(
                item.fmsSlipNo(),
                item.orderNo(),
                item.msg(),
                item.code(),
                item.outOfStockGoodsDetail()
        );
    }

    // ── 반품 상품 상세 조회 응답 파싱/매핑 ──

    private FasstoReturnGodDetailListResponse parseReturnGodDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoReturnGodDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto return god detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isReturnGodDetailSuccess(ResponseEntity<String> response, FasstoReturnGodDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private String resolveReturnGodDetailException(ResponseEntity<String> response, FasstoReturnGodDetailListResponse parsedResponse) {
        if (FormatValidator.hasNoValue(response)) {
            return "NO_RESPONSE";
        }
        if (response.getStatusCode().value() != 200) {
            return "HTTP_" + response.getStatusCode().value();
        }
        if (FormatValidator.hasNoValue(parsedResponse)) {
            return "INVALID_RESPONSE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.header()) || !parsedResponse.header().isSuccess()) {
            return "HEADER_FAILURE";
        }
        return "UNKNOWN_FAILURE";
    }

    private GetFasstoReturnGodDetailResult mapReturnGodDetailResult(FasstoReturnGodDetailListResponse response) {
        List<FasstoReturnGodDetailInfoResult> returnGodInfos = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapReturnGodDetailItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoReturnGodDetailResult.of(dataCount, returnGodInfos);
    }

    private FasstoReturnGodDetailInfoResult mapReturnGodDetailItem(FasstoReturnGodDetailItemResponse item) {
        List<FasstoReturnGodDetailGoodsResult> godList = FormatValidator.hasValue(item.godList())
                ? item.godList().stream().map(this::mapReturnGodDetailGoods).toList()
                : List.of();

        return FasstoReturnGodDetailInfoResult.of(
                item.ordNo(),
                item.supCd(),
                item.inSlipNo(),
                item.cstCd(),
                item.whCd(),
                item.inOrdSlipNo(),
                item.inOrdDt(),
                item.outOrdSlipNo(),
                item.supNm(),
                item.custNm(),
                item.outInvoiceNo(),
                item.rtnInvoiceNo(),
                item.inRtnPayCd(),
                item.inRtnPayNm(),
                item.inRtnPay(),
                item.rtnMisYn(),
                item.rtnType(),
                item.rtnTypeNm(),
                item.custTelNo(),
                item.cstMemo(),
                item.rtnReason(),
                item.rtnReasonNm(),
                item.rtnDetailReason(),
                item.rtnGubun(),
                item.rtnGubunNm(),
                godList
        );
    }

    private FasstoReturnGodDetailGoodsResult mapReturnGodDetailGoods(FasstoReturnGodDetailGoodsResponse goods) {
        return FasstoReturnGodDetailGoodsResult.of(
                goods.cstGodCd(),
                goods.godNm(),
                goods.makeDt(),
                goods.distTermDt(),
                goods.inQty(),
                goods.remark(),
                goods.rtnGodCheckStat(),
                goods.rtnGodCheckStatNm()
        );
    }

    private String resolveReturnGodDetailVendorMessage(FasstoReturnGodDetailListResponse parsedResponse, String rawBody) {
        if (FormatValidator.hasValue(parsedResponse)) {
            String message = parsedResponse.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }

        if (FormatValidator.hasValue(rawBody)) {
            try {
                FasstoErrorResponse errorResponse = objectMapper.readValue(rawBody, FasstoErrorResponse.class);
                return errorResponse.resolveErrorMessage();
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    // ── 페이로드 빌드 ──

    private JsonNode buildRequestPayloadJson(
            FasstoReturnDeliveryMapper request,
            URI uri,
            int attempt,
            String action
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.POST.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", request.getCustomerCode());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(request.getAccessToken()));
        payload.put("body", request.toPayload());
        payload.put("action", action);
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildReturnGodDetailRequestPayloadJson(
            FasstoReturnGodDetailQuery query,
            URI uri,
            int attempt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        if (FormatValidator.hasValue(query.getStartDate())) {
            payload.put("strDt", query.getStartDate());
        }
        if (FormatValidator.hasValue(query.getEndDate())) {
            payload.put("endDt", query.getEndDate());
        }
        if (FormatValidator.hasValue(query.getRtnSlipNoList())) {
            payload.put("rtnSlipNoList", query.getRtnSlipNoList());
        }
        if (FormatValidator.hasValue(query.getWhCd())) {
            payload.put("whCd", query.getWhCd());
        }
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildResponsePayloadJson(ResponseEntity<String> response, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (FormatValidator.hasValue(response)) {
            payload.put("status", response.getStatusCode().value());
            payload.put("headers", toSingleValueMap(response.getHeaders()));

            String body = response.getBody();
            if (FormatValidator.hasValue(body)) {
                payload.put("body", body);
            }
        }
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    // ── 유틸리티 ──

    private Map<String, String> toSingleValueMap(HttpHeaders headers) {
        if (FormatValidator.hasNoValue(headers)) {
            return Map.of();
        }
        return headers.toSingleValueMap();
    }

    private String resolveVendorMessage(RegisterFasstoDeliveryResponse parsedResponse, String rawBody) {
        if (FormatValidator.hasValue(parsedResponse)) {
            String message = parsedResponse.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }

        if (FormatValidator.hasValue(rawBody)) {
            try {
                FasstoErrorResponse errorResponse = objectMapper.readValue(rawBody, FasstoErrorResponse.class);
                return errorResponse.resolveErrorMessage();
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String resolveVendorMessageFromException(Exception e) {
        if (e instanceof RestClientResponseException responseException) {
            String body = responseException.getResponseBodyAsString();
            if (FormatValidator.hasNoValue(body)) {
                return null;
            }
            try {
                FasstoErrorResponse parsedResponse = objectMapper.readValue(body, FasstoErrorResponse.class);
                return parsedResponse.resolveErrorMessage();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String maskValue(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 4) + "****";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis + ThreadLocalRandom.current().nextLong(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
