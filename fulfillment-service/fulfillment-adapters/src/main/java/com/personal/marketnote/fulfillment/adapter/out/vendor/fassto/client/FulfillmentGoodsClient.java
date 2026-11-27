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
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsElementQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper.FasstoGoodsCommandToRequestMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.exception.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;
import com.personal.marketnote.fulfillment.port.in.result.vendor.*;
import com.personal.marketnote.fulfillment.port.out.vendor.*;
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
public class FulfillmentGoodsClient implements RegisterFulfillmentGoodsPort, GetFulfillmentGoodsPort, GetFulfillmentGoodsDetailPort, UpdateFulfillmentGoodsPort, GetFulfillmentGoodsElementsPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FulfillmentAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;
    private final ExternalCallLogger externalCallLogger;
    private final Sleeper sleeper;

    public FulfillmentGoodsClient(
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
    public RegisterFulfillmentGoodsResult registerGoods(RegisterFulfillmentGoodsCommand command) {
        String path = properties.getGoodsPath();
        return recordCall(path, "registerGoods", () -> doRegisterGoods(command));
    }

    private RegisterFulfillmentGoodsResult doRegisterGoods(RegisterFulfillmentGoodsCommand command) {
        FulfillmentGoodsMapper request = FasstoGoodsCommandToRequestMapper.mapToRegisterRequest(command);
        return executeGoodsRegistration(request, "REGISTER");
    }

    @Override
    public GetFulfillmentGoodsResult getGoods(GetFulfillmentGoodsCommand command) {
        String path = properties.getGoodsPath();
        return recordCall(path, "getGoods", () -> doGetGoods(command));
    }

    private <T> T recordCall(String path, String operation, Supplier<T> call) {
        if (isSensitivePath(path)) {
            return externalCallLogger.recordSensitive(ExternalSystem.FASSTO, operation, Map.of(), call);
        }
        return externalCallLogger.record(ExternalSystem.FASSTO, operation, Map.of(), call);
    }

    private GetFulfillmentGoodsResult doGetGoods(GetFulfillmentGoodsCommand command) {
        FulfillmentGoodsQuery query = FasstoGoodsCommandToRequestMapper.mapToGoodsQuery(command);

        if (FormatValidator.hasNoValue(query)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment goods query is required.");
        }

        URI uri = buildGoodsUri(query.getCustomerCode());
        return executeGoodsList(uri, query.getCustomerCode(), query.getAccessToken(), null, false);
    }

    @Override
    public GetFulfillmentGoodsResult getGoodsDetail(GetFulfillmentGoodsDetailCommand command) {
        String path = properties.getGoodsPath();
        return recordCall(path, "getGoodsDetail", () -> doGetGoodsDetail(command));
    }

    private GetFulfillmentGoodsResult doGetGoodsDetail(GetFulfillmentGoodsDetailCommand command) {
        FulfillmentGoodsDetailQuery query = FasstoGoodsCommandToRequestMapper.mapToGoodsDetailQuery(command);

        if (FormatValidator.hasNoValue(query)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment goods detail query is required.");
        }

        URI uri = buildGoodsDetailUri(query.getCustomerCode(), query.getGodNm());
        return executeGoodsList(uri, query.getCustomerCode(), query.getAccessToken(), query.getGodNm(), true);
    }

    @Override
    public GetFulfillmentGoodsElementsResult getGoodsElements(GetFulfillmentGoodsElementsCommand command) {
        String path = properties.getGoodsElementPath();
        return recordCall(path, "getGoodsElements", () -> doGetGoodsElements(command));
    }

    private GetFulfillmentGoodsElementsResult doGetGoodsElements(GetFulfillmentGoodsElementsCommand command) {
        FulfillmentGoodsElementQuery query = FasstoGoodsCommandToRequestMapper.mapToGoodsElementsQuery(command);

        if (FormatValidator.hasNoValue(query)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment goods element query is required.");
        }

        URI uri = buildGoodsElementUri(query.getCustomerCode());
        String path = properties.getGoodsElementPath();

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

                logVendorFailureWarn("Failed to get Fulfillment goods elements", attempt, path, e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleeper.sleepWithJitter(sleepMillis);

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

            FasstoInvalidAccessDetector.throwIfInvalidAccess(parsedResponse, response);

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

            sleeper.sleepWithJitter(sleepMillis);
            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        logVendorFailureError("Failed to get Fulfillment goods elements", uri, path, error);
        throw new GetFulfillmentGoodsElementsFailedException(failureMessage, wrapCauseIfSensitive(path, error));
    }

    private GetFulfillmentGoodsResult executeGoodsList(
        URI uri,
        String customerCode,
        String accessToken,
        String godNm,
        boolean isDetail
    ) {
        String path = properties.getGoodsPath();
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

                logVendorFailureWarn("Failed to get Fulfillment goods " + requestLabel, attempt, path, e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleeper.sleepWithJitter(sleepMillis);
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

            FasstoInvalidAccessDetector.throwIfInvalidAccess(parsedResponse, response);

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

            sleeper.sleepWithJitter(sleepMillis);

            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        logVendorFailureError("Failed to get Fulfillment goods " + requestLabel, uri, path, error);
        if (isDetail) {
            throw new GetFulfillmentGoodsDetailFailedException(failureMessage, wrapCauseIfSensitive(path, error));
        }
        throw new GetFulfillmentGoodsFailedException(failureMessage, wrapCauseIfSensitive(path, error));
    }

    @Override
    public UpdateFulfillmentGoodsResult updateGoods(UpdateFulfillmentGoodsCommand command) {
        String path = properties.getGoodsPath();
        return recordCall(path, "updateGoods", () -> doUpdateGoods(command));
    }

    private UpdateFulfillmentGoodsResult doUpdateGoods(UpdateFulfillmentGoodsCommand command) {
        FulfillmentGoodsMapper request = FasstoGoodsCommandToRequestMapper.mapToUpdateRequest(command);
        return executeGoodsUpdate(request, "UPDATE");
    }

    private RegisterFulfillmentGoodsResult executeGoodsRegistration(
        FulfillmentGoodsMapper request,
        String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment goods request is required.");
        }

        URI uri = buildGoodsUri(request.getCustomerCode());
        String path = properties.getGoodsPath();

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
                    "Failed to " + action.toLowerCase() + " Fulfillment goods", attempt, path, e
                );
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleeper.sleepWithJitter(sleepMillis);
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
            String productKey = request.getProductKey();

            vendorCommunicationRecorder.record(
                targetType,
                FulfillmentVendorCommunicationType.REQUEST,
                FulfillmentVendorCommunicationSenderType.SERVER,
                productKey,
                vendorName,
                requestPayload,
                requestPayloadJson,
                exception
            );
            vendorCommunicationRecorder.record(
                targetType,
                FulfillmentVendorCommunicationType.RESPONSE,
                FulfillmentVendorCommunicationSenderType.VENDOR,
                productKey,
                vendorName,
                responsePayload,
                responsePayloadJson,
                exception
            );

            if (isSuccess) {
                return mapGoodsResult(parsedResponse);
            }

            FasstoInvalidAccessDetector.throwIfInvalidAccess(parsedResponse, response);

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

            sleeper.sleepWithJitter(sleepMillis);
            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        logVendorFailureError(
            "Failed to " + action.toLowerCase() + " Fulfillment goods", uri, path, error
        );
        throw new RegisterFulfillmentGoodsFailedException(failureMessage, wrapCauseIfSensitive(path, error));
    }

    private UpdateFulfillmentGoodsResult executeGoodsUpdate(
        FulfillmentGoodsMapper request,
        String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment goods request is required.");
        }

        URI uri = buildGoodsUri(request.getCustomerCode());
        String path = properties.getGoodsPath();

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
                    "Failed to " + action.toLowerCase() + " Fulfillment goods", attempt, path, e
                );
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleeper.sleepWithJitter(sleepMillis);
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
            String productKey = request.getProductKey();

            vendorCommunicationRecorder.record(
                targetType,
                FulfillmentVendorCommunicationType.REQUEST,
                FulfillmentVendorCommunicationSenderType.SERVER,
                productKey,
                vendorName,
                requestPayload,
                requestPayloadJson,
                exception
            );
            vendorCommunicationRecorder.record(
                targetType,
                FulfillmentVendorCommunicationType.RESPONSE,
                FulfillmentVendorCommunicationSenderType.VENDOR,
                productKey,
                vendorName,
                responsePayload,
                responsePayloadJson,
                exception
            );

            if (isSuccess) {
                return mapUpdateGoodsResult(parsedResponse);
            }

            FasstoInvalidAccessDetector.throwIfInvalidAccess(parsedResponse, response);

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

            sleeper.sleepWithJitter(sleepMillis);
            // exponential backoff 적용
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        logVendorFailureError(
            "Failed to " + action.toLowerCase() + " Fulfillment goods", uri, path, error
        );
        throw new UpdateFulfillmentGoodsFailedException(failureMessage, wrapCauseIfSensitive(path, error));
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
            throw new FulfillmentConfigurationException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getGoodsPath())) {
            throw new FulfillmentConfigurationException("Fulfillment goods path is required.");
        }
        if (!properties.getGoodsPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new FulfillmentConfigurationException("Fulfillment goods path must include {customerCode}.");
        }
    }

    private void validateGoodsElementProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new FulfillmentConfigurationException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getGoodsElementPath())) {
            throw new FulfillmentConfigurationException("Fulfillment goods element path is required.");
        }
        if (!properties.getGoodsElementPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new FulfillmentConfigurationException("Fulfillment goods element path must include {customerCode}.");
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
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment goods 응답 파싱 실패, null 반환", e);
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
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment goods update 응답 파싱 실패, null 반환", e);
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
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment goods list 응답 파싱 실패, null 반환", e);
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
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment goods elements 응답 파싱 실패, null 반환", e);
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

    private JsonNode buildListRequestPayloadJson(FulfillmentGoodsElementQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
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
        return UpdateFulfillmentGoodsItemResult.builder()
            .fulfillmentSlipNumber(item.fmsSlipNo())
            .orderNumber(item.orderNo())
            .message(item.msg())
            .code(item.code())
            .outOfStockGoodsDetail(item.outOfStockGoodsDetail())
            .build();
    }

    private FulfillmentGoodsInfoResult mapGoodsInfo(FulfillmentGoodsItemResponse item) {
        return FulfillmentGoodsInfoResult.builder()
            .productCode(item.godCd())
            .productType(item.godType())
            .productName(item.godNm())
            .productTypeName(item.godTypeNm())
            .inventoryProductNameUseYn(item.invGodNmUseYn())
            .customerProductCode(item.cstGodCd())
            .productOptionCode1(item.godOptCd1())
            .productOptionCode2(item.godOptCd2())
            .customerCode(item.cstCd())
            .customerName(item.cstNm())
            .supplierCode(item.supCd())
            .supplierName(item.supNm())
            .categoryCode(item.cateCd())
            .categoryName(item.cateNm())
            .seasonCode(item.seasonCd())
            .genderCode(item.genderCd())
            .productPrice(item.godPr())
            .inboundPrice(item.inPr())
            .salePrice(item.salPr())
            .dealTemperature(item.dealTemp())
            .pickingFacility(item.pickFac())
            .giftDivision(item.giftDiv())
            .giftDivisionName(item.giftDivNm())
            .productWidth(item.godWidth())
            .productLength(item.godLength())
            .productHeight(item.godHeight())
            .manufacturingYear(item.makeYr())
            .productBulk(item.godBulk())
            .productWeight(item.godWeight())
            .productSideSum(item.godSideSum())
            .productVolume(item.godVolume())
            .productBarcode(item.godBarcd())
            .boxWidth(item.boxWidth())
            .boxLength(item.boxLength())
            .boxHeight(item.boxHeight())
            .boxBulk(item.boxBulk())
            .boxWeight(item.boxWeight())
            .innerBoxBarcode(item.inBoxBarcd())
            .innerBoxLength(item.inBoxLength())
            .innerBoxHeight(item.inBoxHeight())
            .innerBoxBulk(item.inBoxBulk())
            .innerBoxWidth(item.inBoxWidth())
            .innerBoxWeight(item.inBoxWeight())
            .innerBoxSideSum(item.inBoxSideSum())
            .boxInCount(item.boxInCnt())
            .innerBoxInCount(item.inBoxInCnt())
            .palletInCount(item.pltInCnt())
            .origin(item.origin())
            .expirationDateManagementYn(item.distTermMgtYn())
            .usableTermDays(item.useTermDay())
            .releaseCanDays(item.outCanDay())
            .inboundCanDays(item.inCanDay())
            .boxDivision(item.boxDiv())
            .bufferProductYn(item.bufGodYn())
            .loadingDirection(item.loadingDirection())
            .firstInboundDate(item.firstInDt())
            .useYn(item.useYn())
            .feeYn(item.feeYn())
            .saleUnitQuantity(item.saleUnitQty())
            .customerOneDayDeliveryYn(item.cstOneDayDeliveryYn())
            .safetyStock(item.safetyStock())
            .build();
    }

    private FulfillmentGoodsElementInfoResult mapGoodsElementInfo(FulfillmentGoodsElementResponse item) {
        List<FulfillmentGoodsElementItemResult> elements = FormatValidator.hasValue(item.elementList())
            ? item.elementList().stream()
            .map(this::mapGoodsElementItem)
            .toList()
            : List.of();

        return FulfillmentGoodsElementInfoResult.builder()
            .productCode(item.godCd())
            .customerProductCode(item.cstGodCd())
            .productName(item.godNm())
            .useYn(item.useYn())
            .elementList(elements)
            .build();
    }

    private FulfillmentGoodsElementItemResult mapGoodsElementItem(FulfillmentGoodsElementItemResponse item) {
        return FulfillmentGoodsElementItemResult.builder()
            .productCode(item.godCd())
            .customerProductCode(item.cstGodCd())
            .productBarcode(item.godBarcd())
            .productName(item.godNm())
            .productType(item.godType())
            .productTypeName(item.godTypeNm())
            .quantity(item.qty())
            .build();
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
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment 에러 응답 파싱 실패, null 반환", e);
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
