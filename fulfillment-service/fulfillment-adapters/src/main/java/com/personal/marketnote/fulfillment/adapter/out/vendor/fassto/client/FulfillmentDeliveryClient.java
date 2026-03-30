package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.http.client.CommunicationFailureHandler;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendor.delivery.*;
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
public class FulfillmentDeliveryClient implements RegisterFulfillmentDeliveryPort, UpdateFulfillmentDeliveryPort, RegisterFulfillmentDeliveryCarPort, UpdateFulfillmentDeliveryCarPort, RegisterFulfillmentDeliveryIcsPort, CompleteFulfillmentDeliveryIcsPort, GetFulfillmentDeliveriesPort, GetFulfillmentDeliveryStatusesPort, GetFulfillmentDeliveryDetailPort, GetFulfillmentDeliveryOutOrdGoodsDetailPort, GetFulfillmentDeliveryOutOrdGoodsByOrdNoPort, GetFulfillmentDeliveryGoodDetailPort, CancelFulfillmentDeliveryPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FulfillmentAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;

    public FulfillmentDeliveryClient(
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
    public RegisterFulfillmentDeliveryResult registerDelivery(FulfillmentDeliveryMapper request) {
        RegisterFulfillmentDeliveryResponse response = executeDeliveryMutation(request, "REGISTER", HttpMethod.POST, false);
        return mapDeliveryResult(response);
    }

    @Override
    public RegisterFulfillmentDeliveryResult updateDelivery(FulfillmentDeliveryMapper request) {
        RegisterFulfillmentDeliveryResponse response = executeDeliveryMutation(request, "UPDATE", HttpMethod.PATCH, true);
        return mapDeliveryResult(response);
    }

    @Override
    public RegisterFulfillmentDeliveryResult registerDeliveryCar(FulfillmentDeliveryCarMapper request) {
        RegisterFulfillmentDeliveryResponse response = executeDeliveryCarMutation(request, "REGISTER_CAR", HttpMethod.POST, false);
        return mapDeliveryResult(response);
    }

    @Override
    public RegisterFulfillmentDeliveryResult registerDeliveryIcs(FulfillmentDeliveryIcsMapper request) {
        RegisterFulfillmentDeliveryResponse response = executeDeliveryIcsMutation(request, "REGISTER_ICS", HttpMethod.POST);
        return mapDeliveryResult(response);
    }

    @Override
    public CompleteFulfillmentDeliveryIcsResult completeDeliveryIcs(FulfillmentDeliveryIcsCompletionMapper request) {
        FulfillmentDeliveryIcsCompletionListResponse response = executeDeliveryIcsCompletionMutation(request, "COMPLETE_ICS");
        return mapDeliveryIcsCompletionResult(response);
    }

    @Override
    public RegisterFulfillmentDeliveryResult updateDeliveryCar(FulfillmentDeliveryCarMapper request) {
        RegisterFulfillmentDeliveryResponse response = executeDeliveryCarMutation(request, "UPDATE_CAR", HttpMethod.PATCH, true);
        return mapDeliveryResult(response);
    }

    @Override
    public CancelFulfillmentDeliveryResult cancelDelivery(FulfillmentDeliveryCancelMapper request) {
        RegisterFulfillmentDeliveryResponse response = executeDeliveryCancellation(request, "CANCEL");
        return mapCancelDeliveryResult(response);
    }

    @Override
    public GetFulfillmentDeliveriesResult getDeliveries(FulfillmentDeliveryQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment delivery query is required.");
        }

        URI uri = buildDeliveryListUri(
                query.getCustomerCode(),
                query.getStartDate(),
                query.getEndDate(),
                query.getStatus(),
                query.getOutDiv(),
                query.getOrdNo()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
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

                log.warn("Failed to get Fulfillment delivery list: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentDeliveryListResponse parsedResponse = parseDeliveryListResponse(response);
            boolean isSuccess = isDeliveryListSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveDeliveryListException(response, parsedResponse);

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
                return mapDeliveryListResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery list request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fulfillment delivery list: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentDeliveriesFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFulfillmentDeliveryStatusesResult getDeliveryStatuses(FulfillmentDeliveryStatusQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment delivery status query is required.");
        }

        URI uri = buildDeliveryStatusUri(
                query.getCustomerCode(),
                query.getStartDate(),
                query.getEndDate(),
                query.getOutDiv()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildDeliveryStatusRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fulfillment delivery status: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentDeliveryStatusListResponse parsedResponse = parseDeliveryStatusResponse(response);
            boolean isSuccess = isDeliveryStatusSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveDeliveryStatusException(response, parsedResponse);

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
                return mapDeliveryStatusResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery status request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fulfillment delivery status: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentDeliveryStatusesFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFulfillmentDeliveryOutOrdGoodsDetailResult getOutOrdGoodsDetail(FulfillmentDeliveryOutOrdGoodsDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment out-ord goods detail query is required.");
        }

        URI uri = buildOutOrdGoodsDetailUri(query.getCustomerCode(), query.getOutOrdSlipNo());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildOutOrdGoodsDetailRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fulfillment out-ord goods detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentOutOrdGoodsDetailListResponse parsedResponse = parseOutOrdGoodsDetailResponse(response);
            boolean isSuccess = isOutOrdGoodsDetailSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveOutOrdGoodsDetailException(response, parsedResponse);

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
                return mapOutOrdGoodsDetailResult(parsedResponse);
            }

            if (isOutOrdGoodsDetailEmptyDataResponse(response, parsedResponse)) {
                return mapOutOrdGoodsDetailResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment out-ord goods detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fulfillment out-ord goods detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentDeliveryOutOrdGoodsDetailFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(FulfillmentDeliveryOutOrdGoodsByOrdNoQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment out-ord goods by ordNo query is required.");
        }

        URI uri = buildOutOrdGoodsByOrdNoUri(
                query.getCustomerCode(),
                query.getStartDate(),
                query.getEndDate(),
                query.getOrdNo()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildOutOrdGoodsByOrdNoRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fulfillment out-ord goods by ordNo: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentOutOrdGoodsByOrdNoListResponse parsedResponse = parseOutOrdGoodsByOrdNoResponse(response);
            boolean isSuccess = isOutOrdGoodsByOrdNoSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveOutOrdGoodsByOrdNoException(response, parsedResponse);

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
                return mapOutOrdGoodsByOrdNoResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment out-ord goods by ordNo request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fulfillment out-ord goods by ordNo: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentDeliveryOutOrdGoodsByOrdNoFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFulfillmentDeliveryGoodDetailResult getDeliveryGoodDetail(FulfillmentDeliveryGoodDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment delivery good detail query is required.");
        }

        URI uri = buildDeliveryGoodDetailUri(
                query.getCustomerCode(),
                query.getStartDate(),
                query.getEndDate(),
                query.getOrdNo()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildDeliveryGoodDetailRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fulfillment delivery good detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentDeliveryGoodDetailListResponse parsedResponse = parseDeliveryGoodDetailResponse(response);
            boolean isSuccess = isDeliveryGoodDetailSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveDeliveryGoodDetailException(response, parsedResponse);

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
                return mapDeliveryGoodDetailResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery good detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fulfillment delivery good detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentDeliveryGoodDetailFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFulfillmentDeliveryDetailResult getDeliveryDetail(FulfillmentDeliveryDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fulfillment delivery detail query is required.");
        }

        URI uri = buildDeliveryDetailUri(
                query.getCustomerCode(),
                query.getSlipNo(),
                query.getOrdNo()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildDetailRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fulfillment delivery detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentDeliveryDetailListResponse parsedResponse = parseDeliveryDetailResponse(response);
            boolean isSuccess = isDeliveryDetailSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveDeliveryDetailException(response, parsedResponse);

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
                return mapDeliveryDetailResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fulfillment delivery detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentDeliveryDetailFailedException(failureMessage, new IOException(error));
    }

    private RegisterFulfillmentDeliveryResponse executeDeliveryMutation(
            FulfillmentDeliveryMapper request,
            String action,
            HttpMethod method,
            boolean isUpdate
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fulfillment delivery request is required.");
        }

        URI uri = buildDeliveryUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildRequestPayloadJson(request, uri, attempt, action);
            String requestPayload = requestPayloadJson.toString();

            ResponseEntity<String> response;
            try {
                response = restClient.method(method)
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

                log.warn("Failed to {} Fulfillment delivery: attempt={}, message={}",
                        action.toLowerCase(),
                        attempt,
                        e.getMessage(),
                        e
                );
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

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fulfillment delivery request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        if (isUpdate) {
            throw new UpdateFulfillmentDeliveryFailedException(failureMessage, new IOException(error));
        }
        throw new RegisterFulfillmentDeliveryFailedException(failureMessage, new IOException(error));
    }

    private RegisterFulfillmentDeliveryResponse executeDeliveryCarMutation(
            FulfillmentDeliveryCarMapper request,
            String action,
            HttpMethod method,
            boolean isUpdate
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fulfillment delivery car request is required.");
        }

        URI uri = buildDeliveryCarUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildCarRequestPayloadJson(request, uri, attempt, action);
            String requestPayload = requestPayloadJson.toString();

            ResponseEntity<String> response;
            try {
                response = restClient.method(method)
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

                log.warn("Failed to {} Fulfillment delivery car: attempt={}, message={}",
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

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery car {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fulfillment delivery car request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        if (isUpdate) {
            throw new UpdateFulfillmentDeliveryCarFailedException(failureMessage, new IOException(error));
        }
        throw new RegisterFulfillmentDeliveryFailedException(failureMessage, new IOException(error));
    }

    private RegisterFulfillmentDeliveryResponse executeDeliveryIcsMutation(
            FulfillmentDeliveryIcsMapper request,
            String action,
            HttpMethod method
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fulfillment delivery ics request is required.");
        }

        URI uri = buildDeliveryIcsUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildIcsRequestPayloadJson(request, uri, attempt, action);
            String requestPayload = requestPayloadJson.toString();

            ResponseEntity<String> response;
            try {
                response = restClient.method(method)
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

                log.warn("Failed to {} Fulfillment delivery ics: attempt={}, message={}",
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

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery ics {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fulfillment delivery ics request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        throw new RegisterFulfillmentDeliveryFailedException(failureMessage, new IOException(error));
    }

    private FulfillmentDeliveryIcsCompletionListResponse executeDeliveryIcsCompletionMutation(
            FulfillmentDeliveryIcsCompletionMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fulfillment delivery ics completion request is required.");
        }

        URI uri = buildDeliveryIcsCompletedUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildIcsCompletionRequestPayloadJson(request, uri, attempt, action);
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

                log.warn("Failed to {} Fulfillment delivery ics completion: attempt={}, message={}",
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

            FulfillmentDeliveryIcsCompletionListResponse parsedResponse = parseDeliveryIcsCompletionResponse(response);
            boolean isSuccess = isDeliveryIcsCompletionSuccess(response, parsedResponse);
            String exception = isSuccess
                    ? null
                    : resolveDeliveryIcsCompletionException(response, parsedResponse);

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

            log.warn("Fulfillment delivery ics completion {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fulfillment delivery ics completion request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        throw new CompleteFulfillmentDeliveryIcsFailedException(failureMessage, new IOException(error));
    }

    private RegisterFulfillmentDeliveryResponse executeDeliveryCancellation(
            FulfillmentDeliveryCancelMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fulfillment delivery cancel request is required.");
        }

        URI uri = buildDeliveryCancelUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.DELIVERY;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildCancelRequestPayloadJson(request, uri, attempt, action);
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

                log.warn("Failed to {} Fulfillment delivery: attempt={}, message={}",
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

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment delivery {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fulfillment delivery request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        throw new CancelFulfillmentDeliveryFailedException(failureMessage, new IOException(error));
    }

    private URI buildDeliveryUri(String customerCode) {
        validateDeliveryProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildDeliveryCarUri(String customerCode) {
        validateDeliveryCarProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryCarPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildDeliveryIcsUri(String customerCode) {
        validateDeliveryIcsProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryIcsPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildDeliveryIcsCompletedUri(String customerCode) {
        validateDeliveryIcsCompletedProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryIcsCompletedPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildDeliveryListUri(
            String customerCode,
            String startDate,
            String endDate,
            String status,
            String outDiv,
            String ordNo
    ) {
        validateDeliveryListProperties();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryListPath());
        if (FormatValidator.hasValue(ordNo)) {
            builder.queryParam("ordNo", ordNo);
        }
        return builder
                .buildAndExpand(customerCode, startDate, endDate, status, outDiv)
                .toUri();
    }

    private URI buildDeliveryStatusUri(
            String customerCode,
            String startDate,
            String endDate,
            String outDiv
    ) {
        validateDeliveryStatusProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryStatusPath())
                .buildAndExpand(customerCode, startDate, endDate, outDiv)
                .toUri();
    }

    private URI buildDeliveryDetailUri(
            String customerCode,
            String slipNo,
            String ordNo
    ) {
        validateDeliveryDetailProperties();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryDetailPath());
        if (FormatValidator.hasValue(ordNo)) {
            builder.queryParam("ordNo", ordNo);
        }
        return builder
                .buildAndExpand(customerCode, slipNo)
                .toUri();
    }

    private URI buildOutOrdGoodsDetailUri(String customerCode, String outOrdSlipNo) {
        validateDeliveryOutOrdGoodsDetailProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryOutOrdGoodsDetailPath())
                .queryParam("outOrdSlipNo", outOrdSlipNo)
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildOutOrdGoodsByOrdNoUri(
            String customerCode,
            String startDate,
            String endDate,
            String ordNo
    ) {
        validateDeliveryOutOrdGoodsByOrdNoProperties();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryOutOrdGoodsByOrdNoPath());
        if (FormatValidator.hasValue(ordNo)) {
            builder.queryParam("ordNo", ordNo);
        }
        return builder
                .buildAndExpand(customerCode, startDate, endDate)
                .toUri();
    }

    private URI buildDeliveryGoodDetailUri(
            String customerCode,
            String startDate,
            String endDate,
            String ordNo
    ) {
        validateDeliveryGoodDetailProperties();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryGoodDetailPath());
        if (FormatValidator.hasValue(ordNo)) {
            builder.queryParam("ordNo", ordNo);
        }
        return builder
                .buildAndExpand(customerCode, startDate, endDate)
                .toUri();
    }

    private URI buildDeliveryCancelUri(String customerCode) {
        validateDeliveryCancelProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDeliveryCancelPath())
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

    private void validateDeliveryProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryPath())) {
            throw new IllegalStateException("Fulfillment delivery path is required.");
        }
        if (!properties.getDeliveryPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery path must include {customerCode}.");
        }
    }

    private void validateDeliveryCarProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryCarPath())) {
            throw new IllegalStateException("Fulfillment delivery car path is required.");
        }
        if (!properties.getDeliveryCarPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery car path must include {customerCode}.");
        }
    }

    private void validateDeliveryIcsProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryIcsPath())) {
            throw new IllegalStateException("Fulfillment delivery ics path is required.");
        }
        if (!properties.getDeliveryIcsPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery ics path must include {customerCode}.");
        }
    }

    private void validateDeliveryIcsCompletedProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryIcsCompletedPath())) {
            throw new IllegalStateException("Fulfillment delivery ics completed path is required.");
        }
        if (!properties.getDeliveryIcsCompletedPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery ics completed path must include {customerCode}.");
        }
    }

    private void validateDeliveryListProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryListPath())) {
            throw new IllegalStateException("Fulfillment delivery list path is required.");
        }
        if (!properties.getDeliveryListPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery list path must include {customerCode}.");
        }
        if (!properties.getDeliveryListPath().contains("{startDate}")) {
            throw new IllegalStateException("Fulfillment delivery list path must include {startDate}.");
        }
        if (!properties.getDeliveryListPath().contains("{endDate}")) {
            throw new IllegalStateException("Fulfillment delivery list path must include {endDate}.");
        }
        if (!properties.getDeliveryListPath().contains("{status}")) {
            throw new IllegalStateException("Fulfillment delivery list path must include {status}.");
        }
        if (!properties.getDeliveryListPath().contains("{outDiv}")) {
            throw new IllegalStateException("Fulfillment delivery list path must include {outDiv}.");
        }
    }

    private void validateDeliveryStatusProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryStatusPath())) {
            throw new IllegalStateException("Fulfillment delivery status path is required.");
        }
        if (!properties.getDeliveryStatusPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery status path must include {customerCode}.");
        }
        if (!properties.getDeliveryStatusPath().contains("{startDate}")) {
            throw new IllegalStateException("Fulfillment delivery status path must include {startDate}.");
        }
        if (!properties.getDeliveryStatusPath().contains("{endDate}")) {
            throw new IllegalStateException("Fulfillment delivery status path must include {endDate}.");
        }
        if (!properties.getDeliveryStatusPath().contains("{outDiv}")) {
            throw new IllegalStateException("Fulfillment delivery status path must include {outDiv}.");
        }
    }

    private void validateDeliveryDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryDetailPath())) {
            throw new IllegalStateException("Fulfillment delivery detail path is required.");
        }
        if (!properties.getDeliveryDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery detail path must include {customerCode}.");
        }
        if (!properties.getDeliveryDetailPath().contains("{slipNo}")) {
            throw new IllegalStateException("Fulfillment delivery detail path must include {slipNo}.");
        }
    }

    private void validateDeliveryOutOrdGoodsDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryOutOrdGoodsDetailPath())) {
            throw new IllegalStateException("Fulfillment delivery out-ord goods detail path is required.");
        }
        if (!properties.getDeliveryOutOrdGoodsDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery out-ord goods detail path must include {customerCode}.");
        }
    }

    private void validateDeliveryOutOrdGoodsByOrdNoProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryOutOrdGoodsByOrdNoPath())) {
            throw new IllegalStateException("Fulfillment delivery out-ord goods by ordNo path is required.");
        }
        if (!properties.getDeliveryOutOrdGoodsByOrdNoPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery out-ord goods by ordNo path must include {customerCode}.");
        }
        if (!properties.getDeliveryOutOrdGoodsByOrdNoPath().contains("{startDate}")) {
            throw new IllegalStateException("Fulfillment delivery out-ord goods by ordNo path must include {startDate}.");
        }
        if (!properties.getDeliveryOutOrdGoodsByOrdNoPath().contains("{endDate}")) {
            throw new IllegalStateException("Fulfillment delivery out-ord goods by ordNo path must include {endDate}.");
        }
    }

    private void validateDeliveryGoodDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryGoodDetailPath())) {
            throw new IllegalStateException("Fulfillment delivery good detail path is required.");
        }
        if (!properties.getDeliveryGoodDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery good detail path must include {customerCode}.");
        }
        if (!properties.getDeliveryGoodDetailPath().contains("{startDate}")) {
            throw new IllegalStateException("Fulfillment delivery good detail path must include {startDate}.");
        }
        if (!properties.getDeliveryGoodDetailPath().contains("{endDate}")) {
            throw new IllegalStateException("Fulfillment delivery good detail path must include {endDate}.");
        }
    }

    private void validateDeliveryCancelProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryCancelPath())) {
            throw new IllegalStateException("Fulfillment delivery cancel path is required.");
        }
        if (!properties.getDeliveryCancelPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment delivery cancel path must include {customerCode}.");
        }
    }

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
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment delivery response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentDeliveryListResponse parseDeliveryListResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentDeliveryListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment delivery list response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentDeliveryStatusListResponse parseDeliveryStatusResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentDeliveryStatusListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment delivery status response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentDeliveryDetailListResponse parseDeliveryDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentDeliveryDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment delivery detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentOutOrdGoodsDetailListResponse parseOutOrdGoodsDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentOutOrdGoodsDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment out-ord goods detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentOutOrdGoodsByOrdNoListResponse parseOutOrdGoodsByOrdNoResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentOutOrdGoodsByOrdNoListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment out-ord goods by ordNo response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentDeliveryGoodDetailListResponse parseDeliveryGoodDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentDeliveryGoodDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment delivery good detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FulfillmentDeliveryIcsCompletionListResponse parseDeliveryIcsCompletionResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentDeliveryIcsCompletionListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment delivery ics completion response: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isSuccessResponse(ResponseEntity<String> response, RegisterFulfillmentDeliveryResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryListSuccess(ResponseEntity<String> response, FulfillmentDeliveryListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryStatusSuccess(ResponseEntity<String> response, FulfillmentDeliveryStatusListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryDetailSuccess(ResponseEntity<String> response, FulfillmentDeliveryDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isOutOrdGoodsDetailSuccess(ResponseEntity<String> response, FulfillmentOutOrdGoodsDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isOutOrdGoodsDetailEmptyDataResponse(ResponseEntity<String> response, FulfillmentOutOrdGoodsDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && FormatValidator.hasValue(parsedResponse.header())
                && FormatValidator.hasNoValue(parsedResponse.data());
    }

    private boolean isOutOrdGoodsByOrdNoSuccess(ResponseEntity<String> response, FulfillmentOutOrdGoodsByOrdNoListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryGoodDetailSuccess(ResponseEntity<String> response, FulfillmentDeliveryGoodDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryIcsCompletionSuccess(ResponseEntity<String> response, FulfillmentDeliveryIcsCompletionListResponse parsedResponse) {
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

    private String resolveDeliveryListException(ResponseEntity<String> response, FulfillmentDeliveryListResponse parsedResponse) {
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
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveDeliveryStatusException(ResponseEntity<String> response, FulfillmentDeliveryStatusListResponse parsedResponse) {
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
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveDeliveryDetailException(ResponseEntity<String> response, FulfillmentDeliveryDetailListResponse parsedResponse) {
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
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveOutOrdGoodsDetailException(
            ResponseEntity<String> response,
            FulfillmentOutOrdGoodsDetailListResponse parsedResponse
    ) {
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
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveOutOrdGoodsByOrdNoException(
            ResponseEntity<String> response,
            FulfillmentOutOrdGoodsByOrdNoListResponse parsedResponse
    ) {
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
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveDeliveryGoodDetailException(
            ResponseEntity<String> response,
            FulfillmentDeliveryGoodDetailListResponse parsedResponse
    ) {
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
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private String resolveDeliveryIcsCompletionException(
            ResponseEntity<String> response,
            FulfillmentDeliveryIcsCompletionListResponse parsedResponse
    ) {
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
        if (FormatValidator.hasNoValue(parsedResponse.data())) {
            return "DATA_MISSING";
        }
        return "UNKNOWN_FAILURE";
    }

    private JsonNode buildRequestPayloadJson(
            FulfillmentDeliveryMapper request,
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

    private JsonNode buildCarRequestPayloadJson(
            FulfillmentDeliveryCarMapper request,
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

    private JsonNode buildIcsRequestPayloadJson(
            FulfillmentDeliveryIcsMapper request,
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

    private JsonNode buildCancelRequestPayloadJson(
            FulfillmentDeliveryCancelMapper request,
            URI uri,
            int attempt,
            String action
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.PATCH.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", request.getCustomerCode());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(request.getAccessToken()));
        payload.put("body", request.toPayload());
        payload.put("action", action);
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildIcsCompletionRequestPayloadJson(
            FulfillmentDeliveryIcsCompletionMapper request,
            URI uri,
            int attempt,
            String action
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.PATCH.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", request.getCustomerCode());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(request.getAccessToken()));
        payload.put("body", request.toPayload());
        payload.put("action", action);
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildListRequestPayloadJson(FulfillmentDeliveryQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("startDate", query.getStartDate());
        payload.put("endDate", query.getEndDate());
        payload.put("status", query.getStatus());
        payload.put("outDiv", query.getOutDiv());
        if (FormatValidator.hasValue(query.getOrdNo())) {
            payload.put("ordNo", query.getOrdNo());
        }
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildDeliveryStatusRequestPayloadJson(FulfillmentDeliveryStatusQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("startDate", query.getStartDate());
        payload.put("endDate", query.getEndDate());
        payload.put("outDiv", query.getOutDiv());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildDetailRequestPayloadJson(FulfillmentDeliveryDetailQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("slipNo", query.getSlipNo());
        if (FormatValidator.hasValue(query.getOrdNo())) {
            payload.put("ordNo", query.getOrdNo());
        }
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildOutOrdGoodsDetailRequestPayloadJson(
            FulfillmentDeliveryOutOrdGoodsDetailQuery query,
            URI uri,
            int attempt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("outOrdSlipNo", query.getOutOrdSlipNo());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildOutOrdGoodsByOrdNoRequestPayloadJson(
            FulfillmentDeliveryOutOrdGoodsByOrdNoQuery query,
            URI uri,
            int attempt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("startDate", query.getStartDate());
        payload.put("endDate", query.getEndDate());
        if (FormatValidator.hasValue(query.getOrdNo())) {
            payload.put("ordNo", query.getOrdNo());
        }
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildDeliveryGoodDetailRequestPayloadJson(
            FulfillmentDeliveryGoodDetailQuery query,
            URI uri,
            int attempt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("startDate", query.getStartDate());
        payload.put("endDate", query.getEndDate());
        if (FormatValidator.hasValue(query.getOrdNo())) {
            payload.put("ordNo", query.getOrdNo());
        }
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
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

    private RegisterFulfillmentDeliveryResult mapDeliveryResult(RegisterFulfillmentDeliveryResponse response) {
        List<RegisterFulfillmentDeliveryItemResult> deliveries = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return RegisterFulfillmentDeliveryResult.of(dataCount, deliveries);
    }

    private CancelFulfillmentDeliveryResult mapCancelDeliveryResult(RegisterFulfillmentDeliveryResponse response) {
        List<CancelFulfillmentDeliveryItemResult> deliveries = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapCancelDeliveryItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return CancelFulfillmentDeliveryResult.of(dataCount, deliveries);
    }

    private GetFulfillmentDeliveriesResult mapDeliveryListResult(FulfillmentDeliveryListResponse response) {
        List<FulfillmentDeliveryInfoResult> deliveries = response.data().stream()
                .map(this::mapDeliveryInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentDeliveriesResult.of(dataCount, deliveries);
    }

    private GetFulfillmentDeliveryStatusesResult mapDeliveryStatusResult(FulfillmentDeliveryStatusListResponse response) {
        List<FulfillmentDeliveryStatusInfoResult> statuses = response.data().stream()
                .map(this::mapDeliveryStatusInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentDeliveryStatusesResult.of(dataCount, statuses);
    }

    private GetFulfillmentDeliveryDetailResult mapDeliveryDetailResult(FulfillmentDeliveryDetailListResponse response) {
        List<FulfillmentDeliveryDetailInfoResult> deliveries = response.data().stream()
                .map(this::mapDeliveryDetailInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentDeliveryDetailResult.of(dataCount, deliveries);
    }

    private GetFulfillmentDeliveryOutOrdGoodsDetailResult mapOutOrdGoodsDetailResult(FulfillmentOutOrdGoodsDetailListResponse response) {
        List<FulfillmentDeliveryOutOrdGoodsInfoResult> goodsByInvoice = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapOutOrdGoodsByInvoice).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentDeliveryOutOrdGoodsDetailResult.of(dataCount, goodsByInvoice);
    }

    private GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult mapOutOrdGoodsByOrdNoResult(FulfillmentOutOrdGoodsByOrdNoListResponse response) {
        List<FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapOutOrdGoodsByOrdNo).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult.of(dataCount, goodsByOrdNo);
    }

    private GetFulfillmentDeliveryGoodDetailResult mapDeliveryGoodDetailResult(FulfillmentDeliveryGoodDetailListResponse response) {
        List<FulfillmentDeliveryGoodDetailInfoResult> goodDetails = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryGoodDetailItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentDeliveryGoodDetailResult.of(dataCount, goodDetails);
    }

    private CompleteFulfillmentDeliveryIcsResult mapDeliveryIcsCompletionResult(FulfillmentDeliveryIcsCompletionListResponse response) {
        List<CompleteFulfillmentDeliveryIcsItemResult> completions = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryIcsCompletionItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return CompleteFulfillmentDeliveryIcsResult.of(dataCount, completions);
    }

    private RegisterFulfillmentDeliveryItemResult mapDeliveryItem(RegisterFulfillmentDeliveryItemResponse item) {
        return RegisterFulfillmentDeliveryItemResult.of(
                item.fmsSlipNo(),
                item.orderNo(),
                item.msg(),
                item.code(),
                item.outOfStockGoodsDetail()
        );
    }

    private CancelFulfillmentDeliveryItemResult mapCancelDeliveryItem(RegisterFulfillmentDeliveryItemResponse item) {
        return CancelFulfillmentDeliveryItemResult.of(
                item.fmsSlipNo(),
                item.orderNo(),
                item.msg(),
                item.code(),
                item.outOfStockGoodsDetail()
        );
    }

    private FulfillmentDeliveryInfoResult mapDeliveryInfo(FulfillmentDeliveryItemResponse item) {
        List<Object> goodsSerialNo = FormatValidator.hasValue(item.goodsSerialNo())
                ? item.goodsSerialNo()
                : List.of();

        return FulfillmentDeliveryInfoResult.of(
                item.outDt(),
                item.ordDt(),
                item.whCd(),
                item.whNm(),
                item.slipNo(),
                item.cstCd(),
                item.cstNm(),
                item.shopCd(),
                item.mapSlipNo(),
                item.shopNm(),
                item.sku(),
                item.ordQty(),
                item.addGodOrdQty(),
                item.outDiv(),
                item.outDivNm(),
                item.cstShopCd(),
                item.ordNo(),
                item.ordSeq(),
                item.shipReqTerm(),
                item.salChanel(),
                item.outWay(),
                item.ordDiv(),
                item.outWayNm(),
                item.wrkStat(),
                item.wrkStatNm(),
                item.invoiceNo(),
                item.parcelNm(),
                item.parcelCd(),
                item.custNm(),
                goodsSerialNo,
                item.custAddr(),
                item.supCd(),
                item.custTelNo(),
                item.supNm(),
                item.remark(),
                item.sendNm(),
                item.sendTelNo(),
                item.updUserNm(),
                item.updTime()
        );
    }

    private FulfillmentDeliveryStatusInfoResult mapDeliveryStatusInfo(FulfillmentDeliveryStatusItemResponse item) {
        List<Object> goodsSerialNo = FormatValidator.hasValue(item.goodsSerialNo())
                ? item.goodsSerialNo()
                : List.of();

        return FulfillmentDeliveryStatusInfoResult.of(
                item.boxDiv(),
                item.boxDivNm(),
                item.boxNm(),
                item.boxNo(),
                item.boxTp(),
                item.crgSt(),
                item.crgStNm(),
                item.cstCd(),
                item.cstNm(),
                item.custAddr(),
                item.custNm(),
                item.custTelNo(),
                item.dlvMisYn(),
                item.godCd(),
                item.godNm(),
                item.invoiceNo(),
                item.ordNo(),
                item.ordSeq(),
                item.outDiv(),
                item.outDivNm(),
                item.outOrdSlipNo(),
                item.packDt(),
                item.packQty(),
                item.packSeq(),
                item.parcelCd(),
                item.parcelLinkYn(),
                item.parcelNm(),
                item.pickSeq(),
                item.postYn(),
                item.printCnt(),
                item.rtnAddr1(),
                item.rtnAddr2(),
                item.rtnCheck(),
                item.rtnEmpNm(),
                item.rtnOrdDt(),
                item.rtnTelNo(),
                item.rtnZipCd(),
                item.salChanel(),
                item.shipReqTerm(),
                item.shopCd(),
                item.shopNm(),
                item.sku(),
                item.whCd(),
                goodsSerialNo,
                item.supCd(),
                item.supNm()
        );
    }

    private FulfillmentDeliveryDetailInfoResult mapDeliveryDetailInfo(FulfillmentDeliveryDetailItemResponse item) {
        List<FulfillmentDeliveryDetailGoodsInfoResult> goods = FormatValidator.hasValue(item.goods())
                ? item.goods().stream().map(this::mapDeliveryDetailGoods).toList()
                : List.of();

        return FulfillmentDeliveryDetailInfoResult.of(
                item.outDt(),
                item.ordDt(),
                item.whCd(),
                item.whNm(),
                item.slipNo(),
                item.cstCd(),
                item.cstNm(),
                item.cstShopCd(),
                item.shopCd(),
                item.mapSlipNo(),
                item.shopNm(),
                item.sku(),
                item.ordQty(),
                item.addGodOrdQty(),
                item.outDiv(),
                item.outDivNm(),
                item.ordNo(),
                item.ordSeq(),
                item.shipReqTerm(),
                item.salChanel(),
                item.outWay(),
                item.ordDiv(),
                item.outWayNm(),
                item.wrkStat(),
                item.wrkStatNm(),
                item.invoiceNo(),
                item.parcelNm(),
                item.parcelCd(),
                item.custNm(),
                item.custAddr(),
                item.custTelNo(),
                item.sendNm(),
                item.sendTelNo(),
                item.updUserNm(),
                item.updTime(),
                goods,
                item.remark()
        );
    }

    private FulfillmentDeliveryDetailGoodsInfoResult mapDeliveryDetailGoods(FulfillmentDeliveryDetailGoodsResponse item) {
        return FulfillmentDeliveryDetailGoodsInfoResult.of(
                item.ordDt(),
                item.whCd(),
                item.slipNo(),
                item.cstCd(),
                item.shopCd(),
                item.supCd(),
                item.godCd(),
                item.cstGodCd(),
                item.godNm(),
                item.orgGodCd(),
                item.godType(),
                item.godTypeNm(),
                item.distTermDt(),
                item.stockQty(),
                item.ordQty(),
                item.addGodOrdQty(),
                item.ordQtySum(),
                item.giftDiv(),
                item.addType(),
                item.emgrYn()
        );
    }

    private FulfillmentDeliveryOutOrdGoodsInfoResult mapOutOrdGoodsByInvoice(FulfillmentOutOrdGoodsByInvoiceResponse item) {
        List<FulfillmentDeliveryOutOrdGoodsItemInfoResult> delivered = FormatValidator.hasValue(item.goodsDeliveredList())
                ? item.goodsDeliveredList().stream().map(this::mapOutOrdGoodsDelivered).toList()
                : List.of();

        return FulfillmentDeliveryOutOrdGoodsInfoResult.of(item.invoiceNo(), delivered);
    }

    private FulfillmentDeliveryOutOrdGoodsItemInfoResult mapOutOrdGoodsDelivered(FulfillmentOutOrdGoodsDeliveredResponse item) {
        return FulfillmentDeliveryOutOrdGoodsItemInfoResult.of(
                item.cstGodCd(),
                item.godNm(),
                item.packQty()
        );
    }

    private FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult mapOutOrdGoodsByOrdNo(FulfillmentOutOrdGoodsByOrdNoItemResponse item) {
        List<FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods = FormatValidator.hasValue(item.goods())
                ? item.goods().stream().map(this::mapOutOrdGoodsByOrdNoGoods).toList()
                : List.of();

        return FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult.of(item.ordNo(), item.invoiceNo(), goods);
    }

    private FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult mapOutOrdGoodsByOrdNoGoods(FulfillmentOutOrdGoodsByOrdNoGoodsResponse item) {
        return FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult.of(
                item.cstGodCd(),
                item.godNm(),
                item.ordQty()
        );
    }

    private FulfillmentDeliveryGoodDetailInfoResult mapDeliveryGoodDetailItem(FulfillmentDeliveryGoodDetailItemResponse item) {
        return FulfillmentDeliveryGoodDetailInfoResult.of(
                item.outDt(),
                item.slipNo(),
                item.outOrdSlipNo(),
                item.orderNo(),
                item.productOrderNo(),
                item.ordDiv(),
                item.invoiceNo(),
                item.sellerChannel(),
                item.custNm(),
                item.godCd(),
                item.cstGodCd(),
                item.godNm(),
                item.outQty(),
                item.markedPrAmount(),
                item.sellingPrAmount(),
                item.dcAmount(),
                item.sellerDcAmount(),
                item.naverDcAmount()
        );
    }

    private CompleteFulfillmentDeliveryIcsItemResult mapDeliveryIcsCompletionItem(FulfillmentDeliveryIcsCompletionItemResponse item) {
        return CompleteFulfillmentDeliveryIcsItemResult.of(
                item.code(),
                item.msg(),
                item.ordNo()
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

    private String resolveVendorMessage(RegisterFulfillmentDeliveryResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentDeliveryListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentDeliveryStatusListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentDeliveryDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentOutOrdGoodsDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentOutOrdGoodsByOrdNoListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentDeliveryGoodDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FulfillmentDeliveryIcsCompletionListResponse response, String rawBody) {
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
