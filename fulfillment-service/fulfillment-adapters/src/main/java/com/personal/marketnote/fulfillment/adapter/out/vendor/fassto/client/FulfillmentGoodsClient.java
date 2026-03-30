package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.http.client.CommunicationFailureHandler;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendor.goods.FulfillmentGoodsDetailQuery;
import com.personal.marketnote.fulfillment.domain.vendor.goods.FulfillmentGoodsElementQuery;
import com.personal.marketnote.fulfillment.domain.vendor.goods.FulfillmentGoodsMapper;
import com.personal.marketnote.fulfillment.domain.vendor.goods.FulfillmentGoodsQuery;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.exception.*;
import com.personal.marketnote.fulfillment.port.in.result.vendor.*;
import com.personal.marketnote.fulfillment.port.out.vendor.*;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class FulfillmentGoodsClient implements RegisterFulfillmentGoodsPort, GetFulfillmentGoodsPort, GetFulfillmentGoodsDetailPort, UpdateFulfillmentGoodsPort, GetFulfillmentGoodsElementsPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FulfillmentAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;

    public FulfillmentGoodsClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            FulfillmentAuthProperties properties,
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
    public RegisterFulfillmentGoodsResult registerGoods(FulfillmentGoodsMapper request) {
        return executeGoodsRegistration(request, "REGISTER");
    }

    @Override
    public GetFulfillmentGoodsResult getGoods(FulfillmentGoodsQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment goods query is required.");
        }

        URI uri = buildGoodsUri(query.getCustomerCode());
        return executeGoodsList(
                uri,
                query.getCustomerCode(),
                query.getAccessToken(),
                null,
                false
        );
    }

    @Override
    public GetFulfillmentGoodsResult getGoodsDetail(FulfillmentGoodsDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment goods detail query is required.");
        }

        URI uri = buildGoodsDetailUri(query.getCustomerCode(), query.getGodNm());
        return executeGoodsList(
                uri,
                query.getCustomerCode(),
                query.getAccessToken(),
                query.getGodNm(),
                true
        );
    }

    @Override
    public GetFulfillmentGoodsElementsResult getGoodsElements(FulfillmentGoodsElementQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment goods element query is required.");
        }

        URI uri = buildGoodsElementUri(query.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.GOODS;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildListRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fulfillment goods elements: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;

                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentGoodsElementsResponse parsedResponse = parseGoodsElementsResponse(response);
            boolean isSuccess = isGoodsElementsSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveGoodsElementsException(response, parsedResponse);

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
                return mapGoodsElementsResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment goods elements request failed: attempt={}, status={}, exception={}",
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleep(sleepMillis);
            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to get Fulfillment goods elements: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentGoodsElementsFailedException(failureMessage, new IOException(error));
    }

    private GetFulfillmentGoodsResult executeGoodsList(
            URI uri,
            String customerCode,
            String accessToken,
            String godNm,
            boolean isDetail
    ) {
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.GOODS;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;
        String requestLabel = isDetail ? "detail" : "list";

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildListRequestPayloadJson(customerCode, accessToken, godNm, uri, attempt);
            String requestPayload = requestPayloadJson.toString();

            ResponseEntity<String> response;
            try {
                response = restClient.get()
                        .uri(uri)
                        .headers(h -> h.addAll(buildHeaders(accessToken, false)))
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

                log.warn("Failed to get Fulfillment goods {}: attempt={}, message={}", requestLabel, attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentGoodsListResponse parsedResponse = parseGoodsListResponse(response);
            boolean isSuccess = isGoodsListSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveGoodsListException(response, parsedResponse);

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
                return mapGoodsListResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment goods {} request failed: attempt={}, status={}, exception={}",
                    requestLabel,
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleep(sleepMillis);

            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to get Fulfillment goods {}: {} with error: {}", requestLabel, uri, error.getMessage(), error);
        if (isDetail) {
            throw new GetFulfillmentGoodsDetailFailedException(failureMessage, new IOException(error));
        }
        throw new GetFulfillmentGoodsFailedException(failureMessage, new IOException(error));
    }

    @Override
    public UpdateFulfillmentGoodsResult updateGoods(FulfillmentGoodsMapper request) {
        return executeGoodsUpdate(request, "UPDATE");
    }

    private RegisterFulfillmentGoodsResult executeGoodsRegistration(
            FulfillmentGoodsMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fulfillment goods request is required.");
        }

        URI uri = buildGoodsUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.GOODS;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildRequestPayloadJson(request, uri, attempt, action, HttpMethod.POST);
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

                log.warn("Failed to {} Fulfillment goods: attempt={}, message={}", action.toLowerCase(), attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            RegisterFulfillmentGoodsResponse parsedResponse = parseResponseBody(response);
            boolean isSuccess = isSuccessResponse(response, parsedResponse);
            String exception = isSuccess
                    ? null
                    : resolveResponseException(response, parsedResponse);
            String productId = request.getProductId();

            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.REQUEST,
                    FulfillmentVendorCommunicationSenderType.SERVER,
                    productId,
                    vendorName,
                    requestPayload,
                    requestPayloadJson,
                    exception
            );
            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.RESPONSE,
                    FulfillmentVendorCommunicationSenderType.VENDOR,
                    productId,
                    vendorName,
                    responsePayload,
                    responsePayloadJson,
                    exception
            );

            if (isSuccess) {
                return mapGoodsResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment goods {} failed: attempt={}, status={}, exception={}",
                    action.toLowerCase(),
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleep(sleepMillis);
            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to {} Fulfillment goods: {} with error: {}", action.toLowerCase(), uri, error.getMessage(), error);
        throw new RegisterFulfillmentGoodsFailedException(failureMessage, new IOException(error));
    }

    private UpdateFulfillmentGoodsResult executeGoodsUpdate(
            FulfillmentGoodsMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fulfillment goods request is required.");
        }

        URI uri = buildGoodsUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.GOODS;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildRequestPayloadJson(request, uri, attempt, action, HttpMethod.PATCH);
            String requestPayload = requestPayloadJson.toString();

            ResponseEntity<String> response;
            try {
                response = restClient.method(HttpMethod.PATCH)
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

                log.warn("Failed to {} Fulfillment goods: attempt={}, message={}", action.toLowerCase(), attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            UpdateFulfillmentGoodsResponse parsedResponse = parseUpdateResponseBody(response);
            boolean isSuccess = isUpdateSuccess(response, parsedResponse);
            String exception = isSuccess
                    ? null
                    : resolveUpdateException(response, parsedResponse);
            String productId = request.getProductId();

            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.REQUEST,
                    FulfillmentVendorCommunicationSenderType.SERVER,
                    productId,
                    vendorName,
                    requestPayload,
                    requestPayloadJson,
                    exception
            );
            vendorCommunicationRecorder.record(
                    targetType,
                    FulfillmentVendorCommunicationType.RESPONSE,
                    FulfillmentVendorCommunicationSenderType.VENDOR,
                    productId,
                    vendorName,
                    responsePayload,
                    responsePayloadJson,
                    exception
            );

            if (isSuccess) {
                return mapUpdateGoodsResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment goods {} failed: attempt={}, status={}, exception={}",
                    action.toLowerCase(),
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleep(sleepMillis);
            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to {} Fulfillment goods: {} with error: {}", action.toLowerCase(), uri, error.getMessage(), error);
        throw new UpdateFulfillmentGoodsFailedException(failureMessage, new IOException(error));
    }

    private URI buildGoodsUri(String customerCode) {
        validateGoodsProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getGoodsPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildGoodsDetailUri(String customerCode, String godNm) {
        validateGoodsProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getGoodsPath())
                .queryParam("godNm", godNm)
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildGoodsElementUri(String customerCode) {
        validateGoodsElementProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getGoodsElementPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private HttpHeaders buildHeaders(String accessToken) {
        return buildHeaders(accessToken, true);
    }

    private HttpHeaders buildHeaders(String accessToken, boolean includeContentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (includeContentType) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        headers.add(ACCESS_TOKEN_HEADER, accessToken);
        return headers;
    }

    private void validateGoodsProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getGoodsPath())) {
            throw new IllegalStateException("Fulfillment goods path is required.");
        }
        if (!properties.getGoodsPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment goods path must include {customerCode}.");
        }
    }

    private void validateGoodsElementProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getGoodsElementPath())) {
            throw new IllegalStateException("Fulfillment goods element path is required.");
        }
        if (!properties.getGoodsElementPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment goods element path must include {customerCode}.");
        }
    }

    private RegisterFulfillmentGoodsResponse parseResponseBody(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, RegisterFulfillmentGoodsResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment goods response: {}", e.getMessage(), e);
            return null;
        }
    }

    private UpdateFulfillmentGoodsResponse parseUpdateResponseBody(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, UpdateFulfillmentGoodsResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment goods update response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentGoodsListResponse parseGoodsListResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentGoodsListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment goods list response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentGoodsElementsResponse parseGoodsElementsResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentGoodsElementsResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment goods elements response: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isSuccessResponse(ResponseEntity<String> response, RegisterFulfillmentGoodsResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && FormatValidator.equals(response.getStatusCode().value(), 200)
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess()
                && FormatValidator.hasValue(parsedResponse.data());
    }

    private boolean isUpdateSuccess(ResponseEntity<String> response, UpdateFulfillmentGoodsResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && FormatValidator.equals(response.getStatusCode().value(), 200)
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess()
                && FormatValidator.hasValue(parsedResponse.data());
    }

    private boolean isGoodsListSuccess(ResponseEntity<String> response, FulfillmentGoodsListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isGoodsElementsSuccess(ResponseEntity<String> response, FulfillmentGoodsElementsResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private String resolveResponseException(ResponseEntity<String> response, RegisterFulfillmentGoodsResponse parsedResponse) {
        if ((FormatValidator.hasNoValue(response))) {
            return "NO_RESPONSE";
        }
        if (response.getStatusCode().value() != 200) {
            return "HTTP_" + response.getStatusCode().value();
        }
        if ((FormatValidator.hasNoValue(parsedResponse))) {
            return "INVALID_RESPONSE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.header()) || !parsedResponse.header().isSuccess()) {
            return "HEADER_FAILURE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveUpdateException(ResponseEntity<String> response, UpdateFulfillmentGoodsResponse parsedResponse) {
        if ((FormatValidator.hasNoValue(response))) {
            return "NO_RESPONSE";
        }
        if (response.getStatusCode().value() != 200) {
            return "HTTP_" + response.getStatusCode().value();
        }
        if ((FormatValidator.hasNoValue(parsedResponse))) {
            return "INVALID_RESPONSE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.header()) || !parsedResponse.header().isSuccess()) {
            return "HEADER_FAILURE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveGoodsListException(
            ResponseEntity<String> response,
            FulfillmentGoodsListResponse parsedResponse
    ) {
        if (FormatValidator.hasNoValue(response)) {
            return "NO_RESPONSE";
        }
        if (FormatValidator.notEquals(response.getStatusCode().value(), 200)) {
            return "HTTP_" + response.getStatusCode().value();
        }
        if (FormatValidator.hasNoValue(parsedResponse)) {
            return "INVALID_RESPONSE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.header()) || !parsedResponse.header().isSuccess()) {
            return "HEADER_FAILURE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveGoodsElementsException(
            ResponseEntity<String> response,
            FulfillmentGoodsElementsResponse parsedResponse
    ) {
        if (FormatValidator.hasNoValue(response)) {
            return "NO_RESPONSE";
        }
        if (FormatValidator.notEquals(response.getStatusCode().value(), 200)) {
            return "HTTP_" + response.getStatusCode().value();
        }
        if (FormatValidator.hasNoValue(parsedResponse)) {
            return "INVALID_RESPONSE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.header()) || !parsedResponse.header().isSuccess()) {
            return "HEADER_FAILURE";
        }
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private JsonNode buildListRequestPayloadJson(FulfillmentGoodsQuery query, URI uri, int attempt) {
        return buildListRequestPayloadJson(
                query.getCustomerCode(),
                query.getAccessToken(),
                null,
                uri,
                attempt
        );
    }

    private JsonNode buildListRequestPayloadJson(
            String customerCode,
            String accessToken,
            String godNm,
            URI uri,
            int attempt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", customerCode);
        if (FormatValidator.hasValue(godNm)) {
            payload.put("godNm", godNm);
        }
        payload.put(ACCESS_TOKEN_HEADER, maskValue(accessToken));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildListRequestPayloadJson(FulfillmentGoodsElementQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildRequestPayloadJson(
            FulfillmentGoodsMapper request,
            URI uri,
            int attempt,
            String action,
            HttpMethod httpMethod
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", httpMethod.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", request.getCustomerCode());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(request.getAccessToken()));
        payload.put("body", request.toPayload());
        payload.put("action", action);
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

    private Map<String, String> toSingleValueMap(HttpHeaders headers) {
        if (FormatValidator.hasNoValue(headers)) {
            return Map.of();
        }

        return headers.toSingleValueMap();
    }

    private RegisterFulfillmentGoodsResult mapGoodsResult(RegisterFulfillmentGoodsResponse response) {
        List<RegisterFulfillmentGoodsItemResult> goods = response.data().stream()
                .map(this::mapGoodsItem)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return RegisterFulfillmentGoodsResult.of(dataCount, goods);
    }

    private UpdateFulfillmentGoodsResult mapUpdateGoodsResult(UpdateFulfillmentGoodsResponse response) {
        List<UpdateFulfillmentGoodsItemResult> goods = response.data().stream()
                .map(this::mapUpdateGoodsItem)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return UpdateFulfillmentGoodsResult.of(dataCount, goods);
    }

    private GetFulfillmentGoodsResult mapGoodsListResult(FulfillmentGoodsListResponse response) {
        List<FulfillmentGoodsInfoResult> goods = response.data().stream()
                .map(this::mapGoodsInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentGoodsResult.of(dataCount, goods);
    }

    private GetFulfillmentGoodsElementsResult mapGoodsElementsResult(FulfillmentGoodsElementsResponse response) {
        List<FulfillmentGoodsElementInfoResult> elements = response.data().stream()
                .map(this::mapGoodsElementInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentGoodsElementsResult.of(dataCount, elements);
    }

    private RegisterFulfillmentGoodsItemResult mapGoodsItem(RegisterFulfillmentGoodsItemResponse item) {
        return RegisterFulfillmentGoodsItemResult.of(
                item.msg(),
                item.code(),
                item.cstGodCd()
        );
    }

    private UpdateFulfillmentGoodsItemResult mapUpdateGoodsItem(UpdateFulfillmentGoodsItemResponse item) {
        return UpdateFulfillmentGoodsItemResult.of(
                item.fmsSlipNo(),
                item.orderNo(),
                item.msg(),
                item.code(),
                item.outOfStockGoodsDetail()
        );
    }

    private FulfillmentGoodsInfoResult mapGoodsInfo(FulfillmentGoodsItemResponse item) {
        return FulfillmentGoodsInfoResult.of(
                item.godCd(),
                item.godType(),
                item.godNm(),
                item.godTypeNm(),
                item.invGodNmUseYn(),
                item.cstGodCd(),
                item.godOptCd1(),
                item.godOptCd2(),
                item.cstCd(),
                item.cstNm(),
                item.supCd(),
                item.supNm(),
                item.cateCd(),
                item.cateNm(),
                item.seasonCd(),
                item.genderCd(),
                item.godPr(),
                item.inPr(),
                item.salPr(),
                item.dealTemp(),
                item.pickFac(),
                item.giftDiv(),
                item.giftDivNm(),
                item.godWidth(),
                item.godLength(),
                item.godHeight(),
                item.makeYr(),
                item.godBulk(),
                item.godWeight(),
                item.godSideSum(),
                item.godVolume(),
                item.godBarcd(),
                item.boxWidth(),
                item.boxLength(),
                item.boxHeight(),
                item.boxBulk(),
                item.boxWeight(),
                item.inBoxBarcd(),
                item.inBoxLength(),
                item.inBoxHeight(),
                item.inBoxBulk(),
                item.inBoxWidth(),
                item.inBoxWeight(),
                item.inBoxSideSum(),
                item.boxInCnt(),
                item.inBoxInCnt(),
                item.pltInCnt(),
                item.origin(),
                item.distTermMgtYn(),
                item.useTermDay(),
                item.outCanDay(),
                item.inCanDay(),
                item.boxDiv(),
                item.bufGodYn(),
                item.loadingDirection(),
                item.firstInDt(),
                item.useYn(),
                item.feeYn(),
                item.saleUnitQty(),
                item.cstOneDayDeliveryYn(),
                item.safetyStock()
        );
    }

    private FulfillmentGoodsElementInfoResult mapGoodsElementInfo(FulfillmentGoodsElementResponse item) {
        List<FulfillmentGoodsElementItemResult> elements = FormatValidator.hasValue(item.elementList())
                ? item.elementList().stream()
                .map(this::mapGoodsElementItem)
                .toList()
                : List.of();

        return FulfillmentGoodsElementInfoResult.of(
                item.godCd(),
                item.cstGodCd(),
                item.godNm(),
                item.useYn(),
                elements
        );
    }

    private FulfillmentGoodsElementItemResult mapGoodsElementItem(FulfillmentGoodsElementItemResponse item) {
        return FulfillmentGoodsElementItemResult.of(
                item.godCd(),
                item.cstGodCd(),
                item.godBarcd(),
                item.godNm(),
                item.godType(),
                item.godTypeNm(),
                item.qty()
        );
    }

    private String maskValue(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return value;
        }

        int visible = Math.min(4, value.length());
        int maskedLength = value.length() - visible;
        if (maskedLength <= 0) {
            return value;
        }

        return "*".repeat(maskedLength) + value.substring(value.length() - visible);
    }

    private void sleep(long millis) {
        try {
            // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
            long jitteredSleepMillis = ThreadLocalRandom.current()
                    .nextLong(Math.max(1L, millis) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String resolveVendorMessage(RegisterFulfillmentGoodsResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(UpdateFulfillmentGoodsResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentGoodsListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentGoodsElementsResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(String rawBody) {
        if (FormatValidator.hasNoValue(rawBody)) {
            return null;
        }

        try {
            FulfillmentErrorResponse response = objectMapper.readValue(rawBody, FulfillmentErrorResponse.class);
            return response.resolveErrorMessage();
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment error response: {}", e.getMessage(), e);
            return null;
        }
    }

    private String resolveVendorMessageFromException(Exception e) {
        if (!(e instanceof RestClientResponseException responseException)) {
            return null;
        }

        String body = responseException.getResponseBodyAsString();
        return resolveVendorMessage(body);
    }
}
