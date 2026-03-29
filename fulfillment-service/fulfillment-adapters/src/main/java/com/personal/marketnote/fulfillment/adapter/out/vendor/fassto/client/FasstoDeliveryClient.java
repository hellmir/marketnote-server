package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.http.client.CommunicationFailureHandler;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FasstoAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.*;
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
public class FasstoDeliveryClient implements RegisterFasstoDeliveryPort, UpdateFasstoDeliveryPort, RegisterFasstoDeliveryCarPort, UpdateFasstoDeliveryCarPort, RegisterFasstoDeliveryIcsPort, CompleteFasstoDeliveryIcsPort, GetFasstoDeliveriesPort, GetFasstoDeliveryStatusesPort, GetFasstoDeliveryDetailPort, GetFasstoDeliveryOutOrdGoodsDetailPort, GetFasstoDeliveryOutOrdGoodsByOrdNoPort, GetFasstoDeliveryGoodDetailPort, CancelFasstoDeliveryPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FasstoAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;

    public FasstoDeliveryClient(
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
    public RegisterFasstoDeliveryResult registerDelivery(FasstoDeliveryMapper request) {
        RegisterFasstoDeliveryResponse response = executeDeliveryMutation(request, "REGISTER", HttpMethod.POST, false);
        return mapDeliveryResult(response);
    }

    @Override
    public RegisterFasstoDeliveryResult updateDelivery(FasstoDeliveryMapper request) {
        RegisterFasstoDeliveryResponse response = executeDeliveryMutation(request, "UPDATE", HttpMethod.PATCH, true);
        return mapDeliveryResult(response);
    }

    @Override
    public RegisterFasstoDeliveryResult registerDeliveryCar(FasstoDeliveryCarMapper request) {
        RegisterFasstoDeliveryResponse response = executeDeliveryCarMutation(request, "REGISTER_CAR", HttpMethod.POST, false);
        return mapDeliveryResult(response);
    }

    @Override
    public RegisterFasstoDeliveryResult registerDeliveryIcs(FasstoDeliveryIcsMapper request) {
        RegisterFasstoDeliveryResponse response = executeDeliveryIcsMutation(request, "REGISTER_ICS", HttpMethod.POST);
        return mapDeliveryResult(response);
    }

    @Override
    public CompleteFasstoDeliveryIcsResult completeDeliveryIcs(FasstoDeliveryIcsCompletionMapper request) {
        FasstoDeliveryIcsCompletionListResponse response = executeDeliveryIcsCompletionMutation(request, "COMPLETE_ICS");
        return mapDeliveryIcsCompletionResult(response);
    }

    @Override
    public RegisterFasstoDeliveryResult updateDeliveryCar(FasstoDeliveryCarMapper request) {
        RegisterFasstoDeliveryResponse response = executeDeliveryCarMutation(request, "UPDATE_CAR", HttpMethod.PATCH, true);
        return mapDeliveryResult(response);
    }

    @Override
    public CancelFasstoDeliveryResult cancelDelivery(FasstoDeliveryCancelMapper request) {
        RegisterFasstoDeliveryResponse response = executeDeliveryCancellation(request, "CANCEL");
        return mapCancelDeliveryResult(response);
    }

    @Override
    public GetFasstoDeliveriesResult getDeliveries(FasstoDeliveryQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto delivery query is required.");
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

                log.warn("Failed to get Fassto delivery list: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoDeliveryListResponse parsedResponse = parseDeliveryListResponse(response);
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

            log.warn("Fassto delivery list request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto delivery list: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoDeliveriesFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoDeliveryStatusesResult getDeliveryStatuses(FasstoDeliveryStatusQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto delivery status query is required.");
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

                log.warn("Failed to get Fassto delivery status: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoDeliveryStatusListResponse parsedResponse = parseDeliveryStatusResponse(response);
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

            log.warn("Fassto delivery status request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto delivery status: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoDeliveryStatusesFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoDeliveryOutOrdGoodsDetailResult getOutOrdGoodsDetail(FasstoDeliveryOutOrdGoodsDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto out-ord goods detail query is required.");
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

                log.warn("Failed to get Fassto out-ord goods detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoOutOrdGoodsDetailListResponse parsedResponse = parseOutOrdGoodsDetailResponse(response);
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

            log.warn("Fassto out-ord goods detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto out-ord goods detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoDeliveryOutOrdGoodsDetailFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(FasstoDeliveryOutOrdGoodsByOrdNoQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto out-ord goods by ordNo query is required.");
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

                log.warn("Failed to get Fassto out-ord goods by ordNo: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoOutOrdGoodsByOrdNoListResponse parsedResponse = parseOutOrdGoodsByOrdNoResponse(response);
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

            log.warn("Fassto out-ord goods by ordNo request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto out-ord goods by ordNo: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoDeliveryOutOrdGoodsByOrdNoFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoDeliveryGoodDetailResult getDeliveryGoodDetail(FasstoDeliveryGoodDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto delivery good detail query is required.");
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

                log.warn("Failed to get Fassto delivery good detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoDeliveryGoodDetailListResponse parsedResponse = parseDeliveryGoodDetailResponse(response);
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

            log.warn("Fassto delivery good detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto delivery good detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoDeliveryGoodDetailFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoDeliveryDetailResult getDeliveryDetail(FasstoDeliveryDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto delivery detail query is required.");
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

                log.warn("Failed to get Fassto delivery detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoDeliveryDetailListResponse parsedResponse = parseDeliveryDetailResponse(response);
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

            log.warn("Fassto delivery detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto delivery detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoDeliveryDetailFailedException(failureMessage, new IOException(error));
    }

    private RegisterFasstoDeliveryResponse executeDeliveryMutation(
            FasstoDeliveryMapper request,
            String action,
            HttpMethod method,
            boolean isUpdate
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fassto delivery request is required.");
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

                log.warn("Failed to {} Fassto delivery: attempt={}, message={}",
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

            log.warn("Fassto delivery {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fassto delivery request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        if (isUpdate) {
            throw new UpdateFasstoDeliveryFailedException(failureMessage, new IOException(error));
        }
        throw new RegisterFasstoDeliveryFailedException(failureMessage, new IOException(error));
    }

    private RegisterFasstoDeliveryResponse executeDeliveryCarMutation(
            FasstoDeliveryCarMapper request,
            String action,
            HttpMethod method,
            boolean isUpdate
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fassto delivery car request is required.");
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

                log.warn("Failed to {} Fassto delivery car: attempt={}, message={}",
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

            log.warn("Fassto delivery car {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fassto delivery car request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        if (isUpdate) {
            throw new UpdateFasstoDeliveryCarFailedException(failureMessage, new IOException(error));
        }
        throw new RegisterFasstoDeliveryFailedException(failureMessage, new IOException(error));
    }

    private RegisterFasstoDeliveryResponse executeDeliveryIcsMutation(
            FasstoDeliveryIcsMapper request,
            String action,
            HttpMethod method
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fassto delivery ics request is required.");
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

                log.warn("Failed to {} Fassto delivery ics: attempt={}, message={}",
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

            log.warn("Fassto delivery ics {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fassto delivery ics request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        throw new RegisterFasstoDeliveryFailedException(failureMessage, new IOException(error));
    }

    private FasstoDeliveryIcsCompletionListResponse executeDeliveryIcsCompletionMutation(
            FasstoDeliveryIcsCompletionMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fassto delivery ics completion request is required.");
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

                log.warn("Failed to {} Fassto delivery ics completion: attempt={}, message={}",
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

            FasstoDeliveryIcsCompletionListResponse parsedResponse = parseDeliveryIcsCompletionResponse(response);
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

            log.warn("Fassto delivery ics completion {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fassto delivery ics completion request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        throw new CompleteFasstoDeliveryIcsFailedException(failureMessage, new IOException(error));
    }

    private RegisterFasstoDeliveryResponse executeDeliveryCancellation(
            FasstoDeliveryCancelMapper request,
            String action
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fassto delivery cancel request is required.");
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

                log.warn("Failed to {} Fassto delivery: attempt={}, message={}",
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

            log.warn("Fassto delivery {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fassto delivery request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        throw new CancelFasstoDeliveryFailedException(failureMessage, new IOException(error));
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
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryPath())) {
            throw new IllegalStateException("Fassto delivery path is required.");
        }
        if (!properties.getDeliveryPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery path must include {customerCode}.");
        }
    }

    private void validateDeliveryCarProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryCarPath())) {
            throw new IllegalStateException("Fassto delivery car path is required.");
        }
        if (!properties.getDeliveryCarPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery car path must include {customerCode}.");
        }
    }

    private void validateDeliveryIcsProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryIcsPath())) {
            throw new IllegalStateException("Fassto delivery ics path is required.");
        }
        if (!properties.getDeliveryIcsPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery ics path must include {customerCode}.");
        }
    }

    private void validateDeliveryIcsCompletedProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryIcsCompletedPath())) {
            throw new IllegalStateException("Fassto delivery ics completed path is required.");
        }
        if (!properties.getDeliveryIcsCompletedPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery ics completed path must include {customerCode}.");
        }
    }

    private void validateDeliveryListProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryListPath())) {
            throw new IllegalStateException("Fassto delivery list path is required.");
        }
        if (!properties.getDeliveryListPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery list path must include {customerCode}.");
        }
        if (!properties.getDeliveryListPath().contains("{startDate}")) {
            throw new IllegalStateException("Fassto delivery list path must include {startDate}.");
        }
        if (!properties.getDeliveryListPath().contains("{endDate}")) {
            throw new IllegalStateException("Fassto delivery list path must include {endDate}.");
        }
        if (!properties.getDeliveryListPath().contains("{status}")) {
            throw new IllegalStateException("Fassto delivery list path must include {status}.");
        }
        if (!properties.getDeliveryListPath().contains("{outDiv}")) {
            throw new IllegalStateException("Fassto delivery list path must include {outDiv}.");
        }
    }

    private void validateDeliveryStatusProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryStatusPath())) {
            throw new IllegalStateException("Fassto delivery status path is required.");
        }
        if (!properties.getDeliveryStatusPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery status path must include {customerCode}.");
        }
        if (!properties.getDeliveryStatusPath().contains("{startDate}")) {
            throw new IllegalStateException("Fassto delivery status path must include {startDate}.");
        }
        if (!properties.getDeliveryStatusPath().contains("{endDate}")) {
            throw new IllegalStateException("Fassto delivery status path must include {endDate}.");
        }
        if (!properties.getDeliveryStatusPath().contains("{outDiv}")) {
            throw new IllegalStateException("Fassto delivery status path must include {outDiv}.");
        }
    }

    private void validateDeliveryDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryDetailPath())) {
            throw new IllegalStateException("Fassto delivery detail path is required.");
        }
        if (!properties.getDeliveryDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery detail path must include {customerCode}.");
        }
        if (!properties.getDeliveryDetailPath().contains("{slipNo}")) {
            throw new IllegalStateException("Fassto delivery detail path must include {slipNo}.");
        }
    }

    private void validateDeliveryOutOrdGoodsDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryOutOrdGoodsDetailPath())) {
            throw new IllegalStateException("Fassto delivery out-ord goods detail path is required.");
        }
        if (!properties.getDeliveryOutOrdGoodsDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery out-ord goods detail path must include {customerCode}.");
        }
    }

    private void validateDeliveryOutOrdGoodsByOrdNoProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryOutOrdGoodsByOrdNoPath())) {
            throw new IllegalStateException("Fassto delivery out-ord goods by ordNo path is required.");
        }
        if (!properties.getDeliveryOutOrdGoodsByOrdNoPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery out-ord goods by ordNo path must include {customerCode}.");
        }
        if (!properties.getDeliveryOutOrdGoodsByOrdNoPath().contains("{startDate}")) {
            throw new IllegalStateException("Fassto delivery out-ord goods by ordNo path must include {startDate}.");
        }
        if (!properties.getDeliveryOutOrdGoodsByOrdNoPath().contains("{endDate}")) {
            throw new IllegalStateException("Fassto delivery out-ord goods by ordNo path must include {endDate}.");
        }
    }

    private void validateDeliveryGoodDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryGoodDetailPath())) {
            throw new IllegalStateException("Fassto delivery good detail path is required.");
        }
        if (!properties.getDeliveryGoodDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery good detail path must include {customerCode}.");
        }
        if (!properties.getDeliveryGoodDetailPath().contains("{startDate}")) {
            throw new IllegalStateException("Fassto delivery good detail path must include {startDate}.");
        }
        if (!properties.getDeliveryGoodDetailPath().contains("{endDate}")) {
            throw new IllegalStateException("Fassto delivery good detail path must include {endDate}.");
        }
    }

    private void validateDeliveryCancelProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getDeliveryCancelPath())) {
            throw new IllegalStateException("Fassto delivery cancel path is required.");
        }
        if (!properties.getDeliveryCancelPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto delivery cancel path must include {customerCode}.");
        }
    }

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
            log.warn("Failed to parse Fassto delivery response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoDeliveryListResponse parseDeliveryListResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoDeliveryListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto delivery list response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoDeliveryStatusListResponse parseDeliveryStatusResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoDeliveryStatusListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto delivery status response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoDeliveryDetailListResponse parseDeliveryDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoDeliveryDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto delivery detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoOutOrdGoodsDetailListResponse parseOutOrdGoodsDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoOutOrdGoodsDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto out-ord goods detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoOutOrdGoodsByOrdNoListResponse parseOutOrdGoodsByOrdNoResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoOutOrdGoodsByOrdNoListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto out-ord goods by ordNo response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoDeliveryGoodDetailListResponse parseDeliveryGoodDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoDeliveryGoodDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto delivery good detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoDeliveryIcsCompletionListResponse parseDeliveryIcsCompletionResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoDeliveryIcsCompletionListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto delivery ics completion response: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isSuccessResponse(ResponseEntity<String> response, RegisterFasstoDeliveryResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryListSuccess(ResponseEntity<String> response, FasstoDeliveryListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryStatusSuccess(ResponseEntity<String> response, FasstoDeliveryStatusListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryDetailSuccess(ResponseEntity<String> response, FasstoDeliveryDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isOutOrdGoodsDetailSuccess(ResponseEntity<String> response, FasstoOutOrdGoodsDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isOutOrdGoodsDetailEmptyDataResponse(ResponseEntity<String> response, FasstoOutOrdGoodsDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && FormatValidator.hasValue(parsedResponse.header())
                && FormatValidator.hasNoValue(parsedResponse.data());
    }

    private boolean isOutOrdGoodsByOrdNoSuccess(ResponseEntity<String> response, FasstoOutOrdGoodsByOrdNoListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryGoodDetailSuccess(ResponseEntity<String> response, FasstoDeliveryGoodDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isDeliveryIcsCompletionSuccess(ResponseEntity<String> response, FasstoDeliveryIcsCompletionListResponse parsedResponse) {
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

    private String resolveDeliveryListException(ResponseEntity<String> response, FasstoDeliveryListResponse parsedResponse) {
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

    private String resolveDeliveryStatusException(ResponseEntity<String> response, FasstoDeliveryStatusListResponse parsedResponse) {
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

    private String resolveDeliveryDetailException(ResponseEntity<String> response, FasstoDeliveryDetailListResponse parsedResponse) {
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
            FasstoOutOrdGoodsDetailListResponse parsedResponse
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
            FasstoOutOrdGoodsByOrdNoListResponse parsedResponse
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
            FasstoDeliveryGoodDetailListResponse parsedResponse
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
            FasstoDeliveryIcsCompletionListResponse parsedResponse
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
            FasstoDeliveryMapper request,
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
            FasstoDeliveryCarMapper request,
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
            FasstoDeliveryIcsMapper request,
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
            FasstoDeliveryCancelMapper request,
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
            FasstoDeliveryIcsCompletionMapper request,
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

    private JsonNode buildListRequestPayloadJson(FasstoDeliveryQuery query, URI uri, int attempt) {
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

    private JsonNode buildDeliveryStatusRequestPayloadJson(FasstoDeliveryStatusQuery query, URI uri, int attempt) {
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

    private JsonNode buildDetailRequestPayloadJson(FasstoDeliveryDetailQuery query, URI uri, int attempt) {
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
            FasstoDeliveryOutOrdGoodsDetailQuery query,
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
            FasstoDeliveryOutOrdGoodsByOrdNoQuery query,
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
            FasstoDeliveryGoodDetailQuery query,
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

    private RegisterFasstoDeliveryResult mapDeliveryResult(RegisterFasstoDeliveryResponse response) {
        List<RegisterFasstoDeliveryItemResult> deliveries = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return RegisterFasstoDeliveryResult.of(dataCount, deliveries);
    }

    private CancelFasstoDeliveryResult mapCancelDeliveryResult(RegisterFasstoDeliveryResponse response) {
        List<CancelFasstoDeliveryItemResult> deliveries = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapCancelDeliveryItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return CancelFasstoDeliveryResult.of(dataCount, deliveries);
    }

    private GetFasstoDeliveriesResult mapDeliveryListResult(FasstoDeliveryListResponse response) {
        List<FasstoDeliveryInfoResult> deliveries = response.data().stream()
                .map(this::mapDeliveryInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoDeliveriesResult.of(dataCount, deliveries);
    }

    private GetFasstoDeliveryStatusesResult mapDeliveryStatusResult(FasstoDeliveryStatusListResponse response) {
        List<FasstoDeliveryStatusInfoResult> statuses = response.data().stream()
                .map(this::mapDeliveryStatusInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoDeliveryStatusesResult.of(dataCount, statuses);
    }

    private GetFasstoDeliveryDetailResult mapDeliveryDetailResult(FasstoDeliveryDetailListResponse response) {
        List<FasstoDeliveryDetailInfoResult> deliveries = response.data().stream()
                .map(this::mapDeliveryDetailInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoDeliveryDetailResult.of(dataCount, deliveries);
    }

    private GetFasstoDeliveryOutOrdGoodsDetailResult mapOutOrdGoodsDetailResult(FasstoOutOrdGoodsDetailListResponse response) {
        List<FasstoDeliveryOutOrdGoodsInfoResult> goodsByInvoice = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapOutOrdGoodsByInvoice).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoDeliveryOutOrdGoodsDetailResult.of(dataCount, goodsByInvoice);
    }

    private GetFasstoDeliveryOutOrdGoodsByOrdNoResult mapOutOrdGoodsByOrdNoResult(FasstoOutOrdGoodsByOrdNoListResponse response) {
        List<FasstoDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapOutOrdGoodsByOrdNo).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoDeliveryOutOrdGoodsByOrdNoResult.of(dataCount, goodsByOrdNo);
    }

    private GetFasstoDeliveryGoodDetailResult mapDeliveryGoodDetailResult(FasstoDeliveryGoodDetailListResponse response) {
        List<FasstoDeliveryGoodDetailInfoResult> goodDetails = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryGoodDetailItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoDeliveryGoodDetailResult.of(dataCount, goodDetails);
    }

    private CompleteFasstoDeliveryIcsResult mapDeliveryIcsCompletionResult(FasstoDeliveryIcsCompletionListResponse response) {
        List<CompleteFasstoDeliveryIcsItemResult> completions = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDeliveryIcsCompletionItem).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return CompleteFasstoDeliveryIcsResult.of(dataCount, completions);
    }

    private RegisterFasstoDeliveryItemResult mapDeliveryItem(RegisterFasstoDeliveryItemResponse item) {
        return RegisterFasstoDeliveryItemResult.of(
                item.fmsSlipNo(),
                item.orderNo(),
                item.msg(),
                item.code(),
                item.outOfStockGoodsDetail()
        );
    }

    private CancelFasstoDeliveryItemResult mapCancelDeliveryItem(RegisterFasstoDeliveryItemResponse item) {
        return CancelFasstoDeliveryItemResult.of(
                item.fmsSlipNo(),
                item.orderNo(),
                item.msg(),
                item.code(),
                item.outOfStockGoodsDetail()
        );
    }

    private FasstoDeliveryInfoResult mapDeliveryInfo(FasstoDeliveryItemResponse item) {
        List<Object> goodsSerialNo = FormatValidator.hasValue(item.goodsSerialNo())
                ? item.goodsSerialNo()
                : List.of();

        return FasstoDeliveryInfoResult.of(
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

    private FasstoDeliveryStatusInfoResult mapDeliveryStatusInfo(FasstoDeliveryStatusItemResponse item) {
        List<Object> goodsSerialNo = FormatValidator.hasValue(item.goodsSerialNo())
                ? item.goodsSerialNo()
                : List.of();

        return FasstoDeliveryStatusInfoResult.of(
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

    private FasstoDeliveryDetailInfoResult mapDeliveryDetailInfo(FasstoDeliveryDetailItemResponse item) {
        List<FasstoDeliveryDetailGoodsInfoResult> goods = FormatValidator.hasValue(item.goods())
                ? item.goods().stream().map(this::mapDeliveryDetailGoods).toList()
                : List.of();

        return FasstoDeliveryDetailInfoResult.of(
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

    private FasstoDeliveryDetailGoodsInfoResult mapDeliveryDetailGoods(FasstoDeliveryDetailGoodsResponse item) {
        return FasstoDeliveryDetailGoodsInfoResult.of(
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

    private FasstoDeliveryOutOrdGoodsInfoResult mapOutOrdGoodsByInvoice(FasstoOutOrdGoodsByInvoiceResponse item) {
        List<FasstoDeliveryOutOrdGoodsItemInfoResult> delivered = FormatValidator.hasValue(item.goodsDeliveredList())
                ? item.goodsDeliveredList().stream().map(this::mapOutOrdGoodsDelivered).toList()
                : List.of();

        return FasstoDeliveryOutOrdGoodsInfoResult.of(item.invoiceNo(), delivered);
    }

    private FasstoDeliveryOutOrdGoodsItemInfoResult mapOutOrdGoodsDelivered(FasstoOutOrdGoodsDeliveredResponse item) {
        return FasstoDeliveryOutOrdGoodsItemInfoResult.of(
                item.cstGodCd(),
                item.godNm(),
                item.packQty()
        );
    }

    private FasstoDeliveryOutOrdGoodsByOrdNoInfoResult mapOutOrdGoodsByOrdNo(FasstoOutOrdGoodsByOrdNoItemResponse item) {
        List<FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods = FormatValidator.hasValue(item.goods())
                ? item.goods().stream().map(this::mapOutOrdGoodsByOrdNoGoods).toList()
                : List.of();

        return FasstoDeliveryOutOrdGoodsByOrdNoInfoResult.of(item.ordNo(), item.invoiceNo(), goods);
    }

    private FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult mapOutOrdGoodsByOrdNoGoods(FasstoOutOrdGoodsByOrdNoGoodsResponse item) {
        return FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult.of(
                item.cstGodCd(),
                item.godNm(),
                item.ordQty()
        );
    }

    private FasstoDeliveryGoodDetailInfoResult mapDeliveryGoodDetailItem(FasstoDeliveryGoodDetailItemResponse item) {
        return FasstoDeliveryGoodDetailInfoResult.of(
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

    private CompleteFasstoDeliveryIcsItemResult mapDeliveryIcsCompletionItem(FasstoDeliveryIcsCompletionItemResponse item) {
        return CompleteFasstoDeliveryIcsItemResult.of(
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

    private String resolveVendorMessage(RegisterFasstoDeliveryResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoDeliveryListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoDeliveryStatusListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoDeliveryDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoOutOrdGoodsDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoOutOrdGoodsByOrdNoListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoDeliveryGoodDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoDeliveryIcsCompletionListResponse response, String rawBody) {
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
            FasstoErrorResponse response = objectMapper.readValue(rawBody, FasstoErrorResponse.class);
            return response.resolveErrorMessage();
        } catch (Exception e) {
            log.warn("Failed to parse Fassto error response: {}", e.getMessage(), e);
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
