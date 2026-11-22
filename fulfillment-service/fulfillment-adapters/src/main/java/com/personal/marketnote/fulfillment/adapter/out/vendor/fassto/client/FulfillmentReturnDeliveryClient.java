package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.common.utility.http.client.CommunicationFailureHandler;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper.FasstoReturnDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.exception.FulfillmentClientInvalidRequestException;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentReturnGodDetailFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentReturnDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.*;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentReturnGodDetailPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.fulfillment.utility.Sleeper;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@VendorAdapter
@Slf4j
public class FulfillmentReturnDeliveryClient implements RegisterFulfillmentReturnDeliveryPort, GetFulfillmentReturnGodDetailPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";
    private static final long RETRY_JITTER_MAX_MILLIS = 500L;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FulfillmentAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;
    private final ExternalCallLogger externalCallLogger;
    private final Sleeper sleeper;

    public FulfillmentReturnDeliveryClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            FulfillmentAuthProperties properties,
            VendorCommunicationRecorder vendorCommunicationRecorder,
            VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator,
            VendorCommunicationFailureHandler vendorCommunicationFailureHandler,
            ExternalCallLogger externalCallLogger,
            Sleeper sleeper
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.vendorCommunicationRecorder = vendorCommunicationRecorder;
        this.vendorCommunicationPayloadGenerator = vendorCommunicationPayloadGenerator;
        this.vendorCommunicationFailureHandler = vendorCommunicationFailureHandler;
        this.externalCallLogger = externalCallLogger;
        this.sleeper = sleeper;
    }

    @Override
    public RegisterFulfillmentDeliveryResult registerReturnDelivery(RegisterFulfillmentReturnDeliveryCommand command) {
        String path = properties.getReturnDeliveryPath();
        return recordCall(path, "registerReturnDelivery", () -> doRegisterReturnDelivery(command));
    }

    private RegisterFulfillmentDeliveryResult doRegisterReturnDelivery(RegisterFulfillmentReturnDeliveryCommand command) {
        FulfillmentReturnDeliveryMapper request = FasstoReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command);
        RegisterFulfillmentDeliveryResponse response = executeReturnDeliveryMutation(request, "REGISTER_RETURN");
        return mapDeliveryResult(response);
    }

    @Override
    public GetFulfillmentReturnGodDetailResult getReturnGodDetail(GetFulfillmentReturnGodDetailCommand command) {
        String path = properties.getReturnGodDetailPath();
        return recordCall(path, "getReturnGodDetail", () -> doGetReturnGodDetail(command));
    }

    private <T> T recordCall(String path, String operation, Supplier<T> call) {
        if (isSensitivePath(path)) {
            return externalCallLogger.recordSensitive(ExternalSystem.FASSTO, operation, Map.of(), call);
        }
        return externalCallLogger.record(ExternalSystem.FASSTO, operation, Map.of(), call);
    }

    private GetFulfillmentReturnGodDetailResult doGetReturnGodDetail(GetFulfillmentReturnGodDetailCommand command) {
        FulfillmentReturnGodDetailQuery query = FasstoReturnDeliveryCommandToRequestMapper.mapToReturnGodDetailQuery(command);
        if (FormatValidator.hasNoValue(query)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment return god detail query is required.");
        }

        URI uri = buildReturnGodDetailUri(query);
        String path = properties.getReturnGodDetailPath();

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
            } catch (RestClientException e) {
                Map<String, Object> errorPayload = new LinkedHashMap<>();
                errorPayload.put("error", e.getClass().getSimpleName());
                errorPayload.put("message", maskExceptionMessage(path, e));
                errorPayload.put("attempt", attempt);

                vendorCommunicationFailureHandler.handleFailure(
                        targetType,
                        vendorName,
                        requestPayload,
                        requestPayloadJson,
                        errorPayload,
                        maskExceptionForHandler(path, e)
                );

                String vendorMessage = resolveVendorMessageFromException(e);
                if (FormatValidator.hasValue(vendorMessage)) {
                    failureMessage = vendorMessage;
                    error = new Exception(vendorMessage);
                }

                logVendorFailureWarn("Failed to get Fulfillment return god detail", attempt, path, e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleeper.sleepWithBaseAndJitter(sleepMillis, RETRY_JITTER_MAX_MILLIS);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentReturnGodDetailListResponse parsedResponse = parseReturnGodDetailResponse(response);
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

            log.warn("Fulfillment return god detail request failed: attempt={}, status={}, exception={}",
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleeper.sleepWithBaseAndJitter(sleepMillis, RETRY_JITTER_MAX_MILLIS);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        logVendorFailureError("Failed to get Fulfillment return god detail", uri, path, error);
        throw new GetFulfillmentReturnGodDetailFailedException(failureMessage, wrapCauseIfSensitive(path, error));
    }

    // ── 반품 등록 (POST) ──

    private RegisterFulfillmentDeliveryResponse executeReturnDeliveryMutation(
            FulfillmentReturnDeliveryMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment return delivery request is required.");
        }

        URI uri = buildReturnDeliveryUri(request.getCustomerCode());
        String path = properties.getReturnDeliveryPath();

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
            } catch (RestClientException e) {
                Map<String, Object> errorPayload = new LinkedHashMap<>();
                errorPayload.put("error", e.getClass().getSimpleName());
                errorPayload.put("message", maskExceptionMessage(path, e));
                errorPayload.put("attempt", attempt);

                vendorCommunicationFailureHandler.handleFailure(
                        targetType,
                        vendorName,
                        requestPayload,
                        requestPayloadJson,
                        errorPayload,
                        maskExceptionForHandler(path, e)
                );

                String vendorMessage = resolveVendorMessageFromException(e);
                if (FormatValidator.hasValue(vendorMessage)) {
                    failureMessage = vendorMessage;
                    error = new Exception(vendorMessage);
                }

                logVendorFailureWarn(
                        "Failed to " + action.toLowerCase() + " Fulfillment return delivery",
                        attempt, path, e
                );
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleeper.sleepWithBaseAndJitter(sleepMillis, RETRY_JITTER_MAX_MILLIS);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            RegisterFulfillmentDeliveryResponse parsedResponse = parseResponseBody(response);
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

            FasstoInvalidAccessDetector.throwIfInvalidAccess(parsedResponse, response);

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment return delivery {} failed: attempt={}, status={}, exception={}",
                    action.toLowerCase(),
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleeper.sleepWithBaseAndJitter(sleepMillis, RETRY_JITTER_MAX_MILLIS);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        logVendorFailureError(
                "Failed to " + action.toLowerCase() + " Fulfillment return delivery request",
                uri, path, error
        );

        throw new RegisterFulfillmentReturnDeliveryFailedException(failureMessage, wrapCauseIfSensitive(path, error));
    }

    // ── URI 빌드 ──

    private URI buildReturnDeliveryUri(String customerCode) {
        validateReturnDeliveryProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getReturnDeliveryPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildReturnGodDetailUri(FulfillmentReturnGodDetailQuery query) {
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
            throw new FulfillmentConfigurationException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getReturnDeliveryPath())) {
            throw new FulfillmentConfigurationException("Fulfillment return delivery path is required.");
        }
        if (!properties.getReturnDeliveryPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new FulfillmentConfigurationException("Fulfillment return delivery path must include {customerCode}.");
        }
    }

    private void validateReturnGodDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new FulfillmentConfigurationException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getReturnGodDetailPath())) {
            throw new FulfillmentConfigurationException("Fulfillment return god detail path is required.");
        }
        if (!properties.getReturnGodDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new FulfillmentConfigurationException("Fulfillment return god detail path must include {customerCode}.");
        }
    }

    // ── 반품 등록 응답 파싱/매핑 ──

    private RegisterFulfillmentDeliveryResponse parseResponseBody(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, RegisterFulfillmentDeliveryResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment return delivery 응답 파싱 실패, null 반환", e);
            return null;
        }
    }

    private boolean isSuccessResponse(ResponseEntity<String> response, RegisterFulfillmentDeliveryResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private String resolveResponseException(ResponseEntity<String> response, RegisterFulfillmentDeliveryResponse parsedResponse) {
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

    private RegisterFulfillmentDeliveryResult mapDeliveryResult(RegisterFulfillmentDeliveryResponse response) {
        List<RegisterFulfillmentDeliveryItemResult> deliveries = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return RegisterFulfillmentDeliveryResult.of(dataCount, deliveries);
    }

    private RegisterFulfillmentDeliveryItemResult mapDeliveryItem(
            RegisterFulfillmentDeliveryItemResponse item
    ) {
        return RegisterFulfillmentDeliveryItemResult.builder()
                .fulfillmentSlipNumber(item.fmsSlipNo())
                .orderNumber(item.orderNo())
                .message(item.msg())
                .code(item.code())
                .outOfStockGoodsDetail(item.outOfStockGoodsDetail())
                .build();
    }

    // ── 반품 상품 상세 조회 응답 파싱/매핑 ──

    private FulfillmentReturnGodDetailListResponse parseReturnGodDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentReturnGodDetailListResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment return god detail 응답 파싱 실패, null 반환", e);
            return null;
        }
    }

    private boolean isReturnGodDetailSuccess(ResponseEntity<String> response, FulfillmentReturnGodDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private String resolveReturnGodDetailException(ResponseEntity<String> response, FulfillmentReturnGodDetailListResponse parsedResponse) {
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

    private GetFulfillmentReturnGodDetailResult mapReturnGodDetailResult(FulfillmentReturnGodDetailListResponse response) {
        List<FulfillmentReturnGodDetailInfoResult> returnGodInfos = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapReturnGodDetailItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentReturnGodDetailResult.of(dataCount, returnGodInfos);
    }

    private FulfillmentReturnGodDetailInfoResult mapReturnGodDetailItem(FulfillmentReturnGodDetailItemResponse item) {
        List<FulfillmentReturnGodDetailGoodsResult> godList = FormatValidator.hasValue(item.godList())
                ? item.godList().stream().map(this::mapReturnGodDetailGoods).toList()
                : List.of();

        return FulfillmentReturnGodDetailInfoResult.builder()
                .orderNumber(item.ordNo())
                .supplierCode(item.supCd())
                .inboundSlipNumber(item.inSlipNo())
                .customerCode(item.cstCd())
                .warehouseCode(item.whCd())
                .inboundOrderSlipNumber(item.inOrdSlipNo())
                .inboundOrderDate(item.inOrdDt())
                .releaseOrderSlipNumber(item.outOrdSlipNo())
                .supplierName(item.supNm())
                .recipientName(item.custNm())
                .releaseInvoiceNumber(item.outInvoiceNo())
                .returnInvoiceNumber(item.rtnInvoiceNo())
                .returnShippingPaymentCode(item.inRtnPayCd())
                .returnShippingPaymentName(item.inRtnPayNm())
                .returnShippingPayment(item.inRtnPay())
                .returnMismatchYn(item.rtnMisYn())
                .returnType(item.rtnType())
                .returnTypeName(item.rtnTypeNm())
                .recipientPhoneNumber(item.custTelNo())
                .customerMemo(item.cstMemo())
                .returnReason(item.rtnReason())
                .returnReasonName(item.rtnReasonNm())
                .returnDetailReason(item.rtnDetailReason())
                .returnClassification(item.rtnGubun())
                .returnClassificationName(item.rtnGubunNm())
                .products(godList)
                .build();
    }

    private FulfillmentReturnGodDetailGoodsResult mapReturnGodDetailGoods(FulfillmentReturnGodDetailGoodsResponse goods) {
        return FulfillmentReturnGodDetailGoodsResult.builder()
                .customerProductCode(goods.cstGodCd())
                .productName(goods.godNm())
                .manufacturingDate(goods.makeDt())
                .expirationDate(goods.distTermDt())
                .inboundQuantity(goods.inQty())
                .remark(goods.remark())
                .returnProductCheckStatus(goods.rtnGodCheckStat())
                .returnProductCheckStatusName(goods.rtnGodCheckStatNm())
                .build();
    }

    private String resolveReturnGodDetailVendorMessage(FulfillmentReturnGodDetailListResponse parsedResponse, String rawBody) {
        if (FormatValidator.hasValue(parsedResponse)) {
            String message = parsedResponse.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }

        if (FormatValidator.hasValue(rawBody)) {
            try {
                FulfillmentErrorResponse errorResponse = objectMapper.readValue(rawBody, FulfillmentErrorResponse.class);
                return errorResponse.resolveErrorMessage();
            } catch (JsonProcessingException ignored) {
                // 벤더 에러 응답 파싱 실패는 폴백 (parsedResponse 메시지 우선, 본문 미해석 시 null 반환)
            }
        }

        return null;
    }

    // ── 페이로드 빌드 ──

    private JsonNode buildRequestPayloadJson(
            FulfillmentReturnDeliveryMapper request,
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
            FulfillmentReturnGodDetailQuery query,
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

    private String resolveVendorMessage(RegisterFulfillmentDeliveryResponse parsedResponse, String rawBody) {
        if (FormatValidator.hasValue(parsedResponse)) {
            String message = parsedResponse.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }

        if (FormatValidator.hasValue(rawBody)) {
            try {
                FulfillmentErrorResponse errorResponse = objectMapper.readValue(rawBody, FulfillmentErrorResponse.class);
                return errorResponse.resolveErrorMessage();
            } catch (JsonProcessingException ignored) {
                // 벤더 에러 응답 파싱 실패는 폴백 (parsedResponse 메시지 우선, 본문 미해석 시 null 반환)
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
                FulfillmentErrorResponse parsedResponse = objectMapper.readValue(body, FulfillmentErrorResponse.class);
                return parsedResponse.resolveErrorMessage();
            } catch (JsonProcessingException ignored) {
                // RestClientResponseException 본문 파싱 실패는 폴백 (메시지 미해석 시 null 반환)
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

    private boolean isSensitivePath(String path) {
        return properties.getSensitivePaths().contains(path);
    }

    private String maskExceptionMessage(String path, Exception e) {
        if (isSensitivePath(path)) {
            return e.getClass().getSimpleName();
        }
        return e.getMessage();
    }

    private void logVendorFailureWarn(String message, int attempt, String path, Exception e) {
        if (isSensitivePath(path)) {
            log.warn(
                    "{}: attempt={}, exception={} (민감 데이터 포함 응답 — 메시지/스택 로깅 생략)",
                    message, attempt, e.getClass().getSimpleName()
            );
            return;
        }
        log.warn("{}: attempt={}, message={}", message, attempt, e.getMessage(), e);
    }

    private void logVendorFailureError(String message, Object uri, String path, Throwable error) {
        if (isSensitivePath(path)) {
            log.error(
                    "{}: {} with exception={} (민감 데이터 포함 응답 — 메시지/스택 로깅 생략)",
                    message, uri, error.getClass().getSimpleName()
            );
            return;
        }
        log.error("{}: {} with error: {}", message, uri, error.getMessage(), error);
    }

    private IOException wrapCauseIfSensitive(String path, Throwable error) {
        if (isSensitivePath(path)) {
            return new IOException(error.getClass().getSimpleName());
        }
        return new IOException(error);
    }

    private Exception maskExceptionForHandler(String path, Exception e) {
        if (isSensitivePath(path)) {
            return new IOException(e.getClass().getSimpleName());
        }
        return e;
    }

}
