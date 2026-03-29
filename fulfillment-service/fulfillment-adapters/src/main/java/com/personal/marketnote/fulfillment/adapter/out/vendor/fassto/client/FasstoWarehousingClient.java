package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.http.client.CommunicationFailureHandler;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FasstoAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing.*;
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
public class FasstoWarehousingClient implements RegisterFasstoWarehousingPort, GetFasstoWarehousingPort, GetFasstoWarehousingDetailPort, GetFasstoWarehousingInspecDetailPort, GetFasstoWarehousingAbnormalPort, GetFasstoWarehousingAbnormalImagePort, UpdateFasstoWarehousingPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FasstoAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;

    public FasstoWarehousingClient(
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
    public RegisterFasstoWarehousingResult registerWarehousing(FasstoWarehousingMapper request) {
        RegisterFasstoWarehousingResponse response = executeWarehousingMutation(
                request,
                "REGISTER",
                HttpMethod.POST,
                false
        );
        return mapWarehousingResult(response);
    }

    @Override
    public UpdateFasstoWarehousingResult updateWarehousing(FasstoWarehousingMapper request) {
        RegisterFasstoWarehousingResponse response = executeWarehousingMutation(
                request,
                "UPDATE",
                HttpMethod.PATCH,
                true
        );
        return mapUpdateWarehousingResult(response);
    }

    @Override
    public GetFasstoWarehousingResult getWarehousing(FasstoWarehousingQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto warehousing query is required.");
        }

        URI uri = buildWarehousingListUri(
                query.getCustomerCode(),
                query.getStartDate(),
                query.getEndDate(),
                query.getInWay(),
                query.getOrdNo(),
                query.getWrkStat()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.WAREHOUSING;
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

                log.warn("Failed to get Fassto warehousing list: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);

                // exponential backoff applied
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;

                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoWarehousingListResponse parsedResponse = parseWarehousingListResponse(response);
            boolean isSuccess = isWarehousingListSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveWarehousingListException(response, parsedResponse);

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
                return mapWarehousingListResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fassto warehousing list request failed: attempt={}, status={}, exception={}",
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleep(sleepMillis);

            // exponential backoff applied
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to get Fassto warehousing list: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoWarehousingFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoWarehousingDetailResult getWarehousingDetail(FasstoWarehousingDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto warehousing detail query is required.");
        }

        URI uri = buildWarehousingDetailUri(query.getCustomerCode(), query.getSlipNo(), query.getOrdNo());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.WAREHOUSING;
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

                log.warn("Failed to get Fassto warehousing detail: attempt={}, message={}", attempt, e.getMessage(), e);
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

            FasstoWarehousingDetailListResponse parsedResponse = parseWarehousingDetailResponse(response);
            boolean isSuccess = isWarehousingDetailSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveWarehousingDetailException(response, parsedResponse);

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
                return mapWarehousingDetailResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fassto warehousing detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto warehousing detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoWarehousingDetailFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoWarehousingInspecDetailResult getWarehousingInspecDetail(FasstoWarehousingInspecDetailQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto warehousing inspection detail query is required.");
        }

        URI uri = buildWarehousingInspecDetailUri(
                query.getCustomerCode(),
                query.getSlipNo(),
                query.getWhCd()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.WAREHOUSING;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildInspecDetailRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fassto warehousing inspection detail: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoWarehousingInspecDetailListResponse parsedResponse = parseWarehousingInspecDetailResponse(response);
            boolean isSuccess = isWarehousingInspecDetailSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveWarehousingInspecDetailException(response, parsedResponse);

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
                return mapWarehousingInspecDetailResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fassto warehousing inspection detail request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto warehousing inspection detail: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoWarehousingInspecDetailFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoWarehousingAbnormalResult getWarehousingAbnormal(FasstoWarehousingAbnormalQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto warehousing abnormal query is required.");
        }

        URI uri = buildWarehousingAbnormalUri(query.getCustomerCode(), query.getWhCd(), query.getSlipNo());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.WAREHOUSING;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildAbnormalRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fassto warehousing abnormal: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoWarehousingAbnormalListResponse parsedResponse = parseWarehousingAbnormalResponse(response);
            boolean isSuccess = isWarehousingAbnormalSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveWarehousingAbnormalException(response, parsedResponse);

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
                return mapWarehousingAbnormalResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fassto warehousing abnormal request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto warehousing abnormal: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoWarehousingAbnormalFailedException(failureMessage, new IOException(error));
    }

    @Override
    public GetFasstoWarehousingAbnormalImageResult getWarehousingAbnormalImage(FasstoWarehousingAbnormalImageQuery query) {
        if (FormatValidator.hasNoValue(query)) {
            throw new IllegalArgumentException("Fassto warehousing abnormal image query is required.");
        }

        URI uri = buildWarehousingAbnormalImageUri(
                query.getSlipNo(),
                query.getGodCd(),
                query.getGoodsSerialNo(),
                query.getFileSeq(),
                query.getImgNo()
        );
        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.WAREHOUSING;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildAbnormalImageRequestPayloadJson(query, uri, attempt);
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

                log.warn("Failed to get Fassto warehousing abnormal image: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FasstoWarehousingAbnormalImageResponse parsedResponse = parseWarehousingAbnormalImageResponse(response);
            boolean isSuccess = isWarehousingAbnormalImageSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveWarehousingAbnormalImageException(response, parsedResponse);

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
                return mapWarehousingAbnormalImageResult(parsedResponse);
            }

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fassto warehousing abnormal image request failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to get Fassto warehousing abnormal image: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFasstoWarehousingAbnormalImageFailedException(failureMessage, new IOException(error));
    }

    private RegisterFasstoWarehousingResponse executeWarehousingMutation(
            FasstoWarehousingMapper request,
            String action,
            HttpMethod method,
            boolean isUpdate
    ) {
        if (FormatValidator.hasNoValue(request)) {
            throw new IllegalArgumentException("Fassto warehousing request is required.");
        }

        URI uri = buildWarehousingUri(request.getCustomerCode());

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.WAREHOUSING;
        FulfillmentVendorName vendorName = FulfillmentVendorName.FASSTO;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            JsonNode requestPayloadJson = buildRequestPayloadJson(request, uri, attempt, action, method);
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

                log.warn("Failed to {} Fassto warehousing request: attempt={}, message={}",
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

            RegisterFasstoWarehousingResponse parsedResponse = parseResponseBody(response);
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

            log.warn("Fassto warehousing {} failed: attempt={}, status={}, exception={}",
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

        log.error("Failed to {} Fassto warehousing request: {} with error: {}",
                action.toLowerCase(),
                uri,
                error.getMessage(),
                error
        );

        if (isUpdate) {
            throw new UpdateFasstoWarehousingFailedException(failureMessage, new IOException(error));
        }
        throw new RegisterFasstoWarehousingFailedException(failureMessage, new IOException(error));
    }

    private URI buildWarehousingUri(String customerCode) {
        validateWarehousingProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getWarehousingPath())
                .buildAndExpand(customerCode)
                .toUri();
    }

    private URI buildWarehousingListUri(
            String customerCode,
            String startDate,
            String endDate,
            String inWay,
            String ordNo,
            String wrkStat
    ) {
        validateWarehousingListProperties();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getWarehousingListPath());
        if (FormatValidator.hasValue(inWay)) {
            builder.queryParam("inWay", inWay);
        }
        if (FormatValidator.hasValue(ordNo)) {
            builder.queryParam("ordNo", ordNo);
        }
        if (FormatValidator.hasValue(wrkStat)) {
            builder.queryParam("wrkStat", wrkStat);
        }
        return builder
                .buildAndExpand(customerCode, startDate, endDate)
                .toUri();
    }

    private URI buildWarehousingDetailUri(
            String customerCode,
            String slipNo,
            String ordNo
    ) {
        validateWarehousingDetailProperties();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getWarehousingDetailPath());
        if (FormatValidator.hasValue(ordNo)) {
            builder.queryParam("ordNo", ordNo);
        }
        return builder
                .buildAndExpand(customerCode, slipNo)
                .toUri();
    }

    private URI buildWarehousingInspecDetailUri(
            String customerCode,
            String slipNo,
            String whCd
    ) {
        validateWarehousingInspecDetailProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getWarehousingInspecDetailPath())
                .buildAndExpand(customerCode, slipNo, whCd)
                .toUri();
    }

    private URI buildWarehousingAbnormalUri(
            String customerCode,
            String whCd,
            String slipNo
    ) {
        validateWarehousingAbnormalProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getWarehousingAbnormalPath())
                .buildAndExpand(customerCode, whCd, slipNo)
                .toUri();
    }

    private URI buildWarehousingAbnormalImageUri(
            String slipNo,
            String godCd,
            String goodsSerialNo,
            String fileSeq,
            String imgNo
    ) {
        validateWarehousingAbnormalImageProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getWarehousingAbnormalImagePath())
                .buildAndExpand(slipNo, godCd, goodsSerialNo, fileSeq, imgNo)
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

    private void validateWarehousingProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getWarehousingPath())) {
            throw new IllegalStateException("Fassto warehousing path is required.");
        }
        if (!properties.getWarehousingPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto warehousing path must include {customerCode}.");
        }
    }

    private void validateWarehousingListProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getWarehousingListPath())) {
            throw new IllegalStateException("Fassto warehousing list path is required.");
        }
        if (!properties.getWarehousingListPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto warehousing list path must include {customerCode}.");
        }
        if (!properties.getWarehousingListPath().contains("{startDate}")) {
            throw new IllegalStateException("Fassto warehousing list path must include {startDate}.");
        }
        if (!properties.getWarehousingListPath().contains("{endDate}")) {
            throw new IllegalStateException("Fassto warehousing list path must include {endDate}.");
        }
    }

    private void validateWarehousingDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getWarehousingDetailPath())) {
            throw new IllegalStateException("Fassto warehousing detail path is required.");
        }
        if (!properties.getWarehousingDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto warehousing detail path must include {customerCode}.");
        }
        if (!properties.getWarehousingDetailPath().contains("{slipNo}")) {
            throw new IllegalStateException("Fassto warehousing detail path must include {slipNo}.");
        }
    }

    private void validateWarehousingInspecDetailProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getWarehousingInspecDetailPath())) {
            throw new IllegalStateException("Fassto warehousing inspection detail path is required.");
        }
        if (!properties.getWarehousingInspecDetailPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto warehousing inspection detail path must include {customerCode}.");
        }
        if (!properties.getWarehousingInspecDetailPath().contains("{slipNo}")) {
            throw new IllegalStateException("Fassto warehousing inspection detail path must include {slipNo}.");
        }
        if (!properties.getWarehousingInspecDetailPath().contains("{whCd}")) {
            throw new IllegalStateException("Fassto warehousing inspection detail path must include {whCd}.");
        }
    }

    private void validateWarehousingAbnormalProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getWarehousingAbnormalPath())) {
            throw new IllegalStateException("Fassto warehousing abnormal path is required.");
        }
        if (!properties.getWarehousingAbnormalPath().contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fassto warehousing abnormal path must include {customerCode}.");
        }
        if (!properties.getWarehousingAbnormalPath().contains("{whCd}")) {
            throw new IllegalStateException("Fassto warehousing abnormal path must include {whCd}.");
        }
        if (!properties.getWarehousingAbnormalPath().contains("{slipNo}")) {
            throw new IllegalStateException("Fassto warehousing abnormal path must include {slipNo}.");
        }
    }

    private void validateWarehousingAbnormalImageProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new IllegalStateException("Fassto base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getWarehousingAbnormalImagePath())) {
            throw new IllegalStateException("Fassto warehousing abnormal image path is required.");
        }
        if (!properties.getWarehousingAbnormalImagePath().contains("{slipNo}")) {
            throw new IllegalStateException("Fassto warehousing abnormal image path must include {slipNo}.");
        }
        if (!properties.getWarehousingAbnormalImagePath().contains("{godCd}")) {
            throw new IllegalStateException("Fassto warehousing abnormal image path must include {godCd}.");
        }
        if (!properties.getWarehousingAbnormalImagePath().contains("{goodsSerialNo}")) {
            throw new IllegalStateException("Fassto warehousing abnormal image path must include {goodsSerialNo}.");
        }
        if (!properties.getWarehousingAbnormalImagePath().contains("{fileSeq}")) {
            throw new IllegalStateException("Fassto warehousing abnormal image path must include {fileSeq}.");
        }
        if (!properties.getWarehousingAbnormalImagePath().contains("{imgNo}")) {
            throw new IllegalStateException("Fassto warehousing abnormal image path must include {imgNo}.");
        }
    }

    private RegisterFasstoWarehousingResponse parseResponseBody(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, RegisterFasstoWarehousingResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto warehousing response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoWarehousingListResponse parseWarehousingListResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoWarehousingListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto warehousing list response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoWarehousingDetailListResponse parseWarehousingDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoWarehousingDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto warehousing detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoWarehousingInspecDetailListResponse parseWarehousingInspecDetailResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoWarehousingInspecDetailListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto warehousing inspection detail response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoWarehousingAbnormalListResponse parseWarehousingAbnormalResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoWarehousingAbnormalListResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto warehousing abnormal response: {}", e.getMessage(), e);
            return null;
        }
    }

    private FasstoWarehousingAbnormalImageResponse parseWarehousingAbnormalImageResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FasstoWarehousingAbnormalImageResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse Fassto warehousing abnormal image response: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isSuccessResponse(ResponseEntity<String> response, RegisterFasstoWarehousingResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess()
                && FormatValidator.hasValue(parsedResponse.data());
    }

    private boolean isWarehousingListSuccess(ResponseEntity<String> response, FasstoWarehousingListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isWarehousingDetailSuccess(ResponseEntity<String> response, FasstoWarehousingDetailListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isWarehousingInspecDetailSuccess(
            ResponseEntity<String> response,
            FasstoWarehousingInspecDetailListResponse parsedResponse
    ) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isWarehousingAbnormalSuccess(ResponseEntity<String> response, FasstoWarehousingAbnormalListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private boolean isWarehousingAbnormalImageSuccess(ResponseEntity<String> response, FasstoWarehousingAbnormalImageResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private String resolveResponseException(ResponseEntity<String> response, RegisterFasstoWarehousingResponse parsedResponse) {
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

    private String resolveWarehousingListException(
            ResponseEntity<String> response,
            FasstoWarehousingListResponse parsedResponse
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

    private String resolveWarehousingDetailException(
            ResponseEntity<String> response,
            FasstoWarehousingDetailListResponse parsedResponse
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

    private String resolveWarehousingInspecDetailException(
            ResponseEntity<String> response,
            FasstoWarehousingInspecDetailListResponse parsedResponse
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

    private String resolveWarehousingAbnormalException(
            ResponseEntity<String> response,
            FasstoWarehousingAbnormalListResponse parsedResponse
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

    private String resolveWarehousingAbnormalImageException(
            ResponseEntity<String> response,
            FasstoWarehousingAbnormalImageResponse parsedResponse
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

    private JsonNode buildListRequestPayloadJson(FasstoWarehousingQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("startDate", query.getStartDate());
        payload.put("endDate", query.getEndDate());
        if (FormatValidator.hasValue(query.getInWay())) {
            payload.put("inWay", query.getInWay());
        }
        if (FormatValidator.hasValue(query.getOrdNo())) {
            payload.put("ordNo", query.getOrdNo());
        }
        if (FormatValidator.hasValue(query.getWrkStat())) {
            payload.put("wrkStat", query.getWrkStat());
        }
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildDetailRequestPayloadJson(FasstoWarehousingDetailQuery query, URI uri, int attempt) {
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

    private JsonNode buildInspecDetailRequestPayloadJson(FasstoWarehousingInspecDetailQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("slipNo", query.getSlipNo());
        payload.put("whCd", query.getWhCd());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildAbnormalRequestPayloadJson(FasstoWarehousingAbnormalQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("customerCode", query.getCustomerCode());
        payload.put("whCd", query.getWhCd());
        payload.put("slipNo", query.getSlipNo());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildAbnormalImageRequestPayloadJson(FasstoWarehousingAbnormalImageQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("slipNo", query.getSlipNo());
        payload.put("godCd", query.getGodCd());
        payload.put("goodsSerialNo", query.getGoodsSerialNo());
        payload.put("fileSeq", query.getFileSeq());
        payload.put("imgNo", query.getImgNo());
        payload.put(ACCESS_TOKEN_HEADER, maskValue(query.getAccessToken()));
        payload.put("attempt", attempt);
        return vendorCommunicationPayloadGenerator.buildPayloadJson(payload);
    }

    private JsonNode buildRequestPayloadJson(
            FasstoWarehousingMapper request,
            URI uri,
            int attempt,
            String action,
            HttpMethod method
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", method.name());
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

    private RegisterFasstoWarehousingResult mapWarehousingResult(RegisterFasstoWarehousingResponse response) {
        List<RegisterFasstoWarehousingItemResult> warehousing = response.data().stream()
                .map(this::mapWarehousingItem)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return RegisterFasstoWarehousingResult.of(dataCount, warehousing);
    }

    private UpdateFasstoWarehousingResult mapUpdateWarehousingResult(RegisterFasstoWarehousingResponse response) {
        List<UpdateFasstoWarehousingItemResult> warehousing = response.data().stream()
                .map(this::mapUpdateWarehousingItem)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return UpdateFasstoWarehousingResult.of(dataCount, warehousing);
    }

    private GetFasstoWarehousingResult mapWarehousingListResult(FasstoWarehousingListResponse response) {
        List<FasstoWarehousingInfoResult> warehousing = response.data().stream()
                .map(this::mapWarehousingInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoWarehousingResult.of(dataCount, warehousing);
    }

    private GetFasstoWarehousingDetailResult mapWarehousingDetailResult(FasstoWarehousingDetailListResponse response) {
        List<FasstoWarehousingDetailInfoResult> details = response.data().stream()
                .map(this::mapWarehousingDetailInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoWarehousingDetailResult.of(dataCount, details);
    }

    private GetFasstoWarehousingInspecDetailResult mapWarehousingInspecDetailResult(
            FasstoWarehousingInspecDetailListResponse response
    ) {
        List<FasstoWarehousingInspecDetailInfoResult> details = response.data().stream()
                .map(this::mapWarehousingInspecDetailInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoWarehousingInspecDetailResult.of(dataCount, details);
    }

    private GetFasstoWarehousingAbnormalResult mapWarehousingAbnormalResult(FasstoWarehousingAbnormalListResponse response) {
        List<FasstoWarehousingAbnormalInfoResult> abnormals = response.data().stream()
                .map(this::mapWarehousingAbnormalInfo)
                .toList();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoWarehousingAbnormalResult.of(dataCount, abnormals);
    }

    private GetFasstoWarehousingAbnormalImageResult mapWarehousingAbnormalImageResult(
            FasstoWarehousingAbnormalImageResponse response
    ) {
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFasstoWarehousingAbnormalImageResult.of(dataCount, response.data());
    }

    private RegisterFasstoWarehousingItemResult mapWarehousingItem(RegisterFasstoWarehousingItemResponse item) {
        return RegisterFasstoWarehousingItemResult.of(
                item.msg(),
                item.code(),
                item.slipNo(),
                item.ordNo()
        );
    }

    private UpdateFasstoWarehousingItemResult mapUpdateWarehousingItem(RegisterFasstoWarehousingItemResponse item) {
        return UpdateFasstoWarehousingItemResult.of(
                item.msg(),
                item.code(),
                item.slipNo(),
                item.ordNo()
        );
    }

    private FasstoWarehousingInfoResult mapWarehousingInfo(FasstoWarehousingItemResponse item) {
        List<Object> goodsSerialNo = FormatValidator.hasValue(item.goodsSerialNo())
                ? item.goodsSerialNo()
                : List.of();

        return FasstoWarehousingInfoResult.of(
                item.ordDt(),
                item.whCd(),
                item.whNm(),
                item.ordNo(),
                item.slipNo(),
                item.cstCd(),
                item.cstNm(),
                item.supCd(),
                item.cstSupCd(),
                item.sku(),
                item.supNm(),
                item.ordQty(),
                item.inQty(),
                item.inWay(),
                item.inWayNm(),
                item.parcelComp(),
                item.parcelInvoiceNo(),
                item.wrkStat(),
                item.wrkStatNm(),
                item.emgrYn(),
                item.remark(),
                goodsSerialNo
        );
    }

    private FasstoWarehousingDetailInfoResult mapWarehousingDetailInfo(FasstoWarehousingDetailItemResponse item) {
        List<FasstoWarehousingDetailGoodsInfoResult> goods = FormatValidator.hasValue(item.goods())
                ? item.goods().stream().map(this::mapWarehousingDetailGoods).toList()
                : List.of();

        return FasstoWarehousingDetailInfoResult.of(
                item.ordDt(),
                item.whCd(),
                item.whNm(),
                item.slipNo(),
                item.ordNo(),
                item.cstCd(),
                item.cstNm(),
                item.supCd(),
                item.supNm(),
                item.cstSupCd(),
                item.sku(),
                item.ordQty(),
                item.inQty(),
                item.tarQty(),
                item.inWay(),
                item.inWayNm(),
                item.parcelComp(),
                item.parcelInvoiceNo(),
                item.wrkStat(),
                item.wrkStatNm(),
                item.emgrYn(),
                item.remark(),
                goods
        );
    }

    private FasstoWarehousingDetailGoodsInfoResult mapWarehousingDetailGoods(FasstoWarehousingDetailGoodsResponse item) {
        return FasstoWarehousingDetailGoodsInfoResult.of(
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

    private FasstoWarehousingInspecDetailInfoResult mapWarehousingInspecDetailInfo(
            FasstoWarehousingInspecDetailItemResponse item
    ) {
        List<Object> goods = FormatValidator.hasValue(item.goods()) ? item.goods() : List.of();
        List<Object> goodsSerialNo = FormatValidator.hasValue(item.goodsSerialNo()) ? item.goodsSerialNo() : List.of();

        return FasstoWarehousingInspecDetailInfoResult.of(
                item.ordDt(),
                item.whCd(),
                item.whNm(),
                item.slipNo(),
                item.cstCd(),
                item.cstNm(),
                item.supCd(),
                item.supNm(),
                item.inWay(),
                item.inWayNm(),
                item.godCd(),
                item.ordQty(),
                item.totInQty(),
                item.parcelComp(),
                item.parcelInvoiceNo(),
                item.wrkStat(),
                item.wrkStatNm(),
                item.remark(),
                goods,
                goodsSerialNo,
                item.externalGodImgUrl(),
                item.distTermDt()
        );
    }

    private FasstoWarehousingAbnormalInfoResult mapWarehousingAbnormalInfo(FasstoWarehousingAbnormalItemResponse item) {
        List<Object> imageUrl = FormatValidator.hasValue(item.imageUrl())
                ? item.imageUrl()
                : List.of();

        return FasstoWarehousingAbnormalInfoResult.of(
                item.slipNo(),
                item.goodsSerialNo(),
                item.goodsSerialStatus(),
                item.whCd(),
                item.cstCd(),
                item.cstNm(),
                item.godCd(),
                item.description(),
                item.remark(),
                item.fileSeq(),
                item.lastFileSeqNo(),
                item.regDate(),
                item.regNM(),
                item.updDate(),
                item.updNm(),
                item.fileNo(),
                imageUrl
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

    private String resolveVendorMessage(RegisterFasstoWarehousingResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoWarehousingListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoWarehousingDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoWarehousingInspecDetailListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoWarehousingAbnormalListResponse response, String rawBody) {
        if (FormatValidator.hasValue(response)) {
            String message = response.resolveErrorMessage();
            if (FormatValidator.hasValue(message)) {
                return message;
            }
        }
        return resolveVendorMessage(rawBody);
    }

    private String resolveVendorMessage(FasstoWarehousingAbnormalImageResponse response, String rawBody) {
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
