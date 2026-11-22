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
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper.FasstoSettlementCommandToRequestMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentSettlementDailyCostItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentSettlementDailyCostListResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.settlement.FulfillmentSettlementDailyCostQuery;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.exception.FulfillmentClientInvalidRequestException;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentSettlementDailyCostsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentSettlementDailyCostInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentSettlementDailyCostsPort;
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
public class FulfillmentSettlementClient implements GetFulfillmentSettlementDailyCostsPort {
    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String YEAR_MONTH_PLACEHOLDER = "{yearMonth}";
    private static final String WH_CODE_PLACEHOLDER = "{whCd}";
    private static final String CUSTOMER_CODE_PLACEHOLDER = "{customerCode}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FulfillmentAuthProperties properties;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;
    private final ExternalCallLogger externalCallLogger;
    private final Sleeper sleeper;

    public FulfillmentSettlementClient(
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
    public GetFulfillmentSettlementDailyCostsResult getDailyCosts(GetFulfillmentSettlementDailyCostsCommand command) {
        String path = properties.getSettlementDailyCostPath();
        return recordCall(path, "getDailyCosts", () -> doGetDailyCosts(command));
    }

    private <T> T recordCall(String path, String operation, Supplier<T> call) {
        if (isSensitivePath(path)) {
            return externalCallLogger.recordSensitive(ExternalSystem.FASSTO, operation, Map.of(), call);
        }
        return externalCallLogger.record(ExternalSystem.FASSTO, operation, Map.of(), call);
    }

    private GetFulfillmentSettlementDailyCostsResult doGetDailyCosts(GetFulfillmentSettlementDailyCostsCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            throw new FulfillmentClientInvalidRequestException("Fulfillment settlement daily cost command is required.");
        }

        FulfillmentSettlementDailyCostQuery query = FasstoSettlementCommandToRequestMapper.mapToQuery(command);
        URI uri = buildSettlementDailyCostUri(query.getYearMonth(), query.getWhCd(), query.getCustomerCode());
        String path = properties.getSettlementDailyCostPath();

        Exception error = new Exception();
        String failureMessage = null;
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        FulfillmentVendorCommunicationTargetType targetType = FulfillmentVendorCommunicationTargetType.SETTLEMENT;
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

                logVendorFailureWarn("Failed to get Fulfillment settlement daily costs", attempt, path, e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleeper.sleepWithJitter(sleepMillis);
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;

                continue;
            }

            JsonNode responsePayloadJson = buildResponsePayloadJson(response, attempt);
            String responsePayload = responsePayloadJson.toString();

            FulfillmentSettlementDailyCostListResponse parsedResponse = parseDailyCostListResponse(response);
            boolean isSuccess = isDailyCostListSuccess(response, parsedResponse);
            String exception = isSuccess ? null : resolveDailyCostListException(response, parsedResponse);

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
                return mapDailyCostListResult(parsedResponse);
            }

            FasstoInvalidAccessDetector.throwIfInvalidAccess(parsedResponse, response);

            String vendorMessage = resolveVendorMessage(parsedResponse, FormatValidator.hasValue(response) ? response.getBody() : null);
            if (FormatValidator.hasValue(vendorMessage)) {
                failureMessage = vendorMessage;
                error = new Exception(vendorMessage);
            }

            log.warn("Fulfillment settlement daily costs request failed: attempt={}, status={}, exception={}",
                    attempt,
                    FormatValidator.hasValue(response) ? response.getStatusCode() : null,
                    exception
            );

            if (CommunicationFailureHandler.isCertainFailure(response)) {
                break;
            }

            sleeper.sleepWithJitter(sleepMillis);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        logVendorFailureError("Failed to get Fulfillment settlement daily costs", uri, path, error);
        throw new GetFulfillmentSettlementDailyCostsFailedException(failureMessage, wrapCauseIfSensitive(path, error));
    }

    private URI buildSettlementDailyCostUri(String yearMonth, String whCd, String customerCode) {
        validateSettlementDailyCostProperties();
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getSettlementDailyCostPath())
                .buildAndExpand(yearMonth, whCd, customerCode)
                .toUri();
    }

    private void validateSettlementDailyCostProperties() {
        if (FormatValidator.hasNoValue(properties.getBaseUrl())) {
            throw new FulfillmentConfigurationException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getSettlementDailyCostPath())) {
            throw new FulfillmentConfigurationException("Fulfillment settlement daily cost path is required.");
        }
        String path = properties.getSettlementDailyCostPath();
        if (!path.contains(YEAR_MONTH_PLACEHOLDER) || !path.contains(WH_CODE_PLACEHOLDER) || !path.contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new FulfillmentConfigurationException("Fulfillment settlement daily cost path must include {yearMonth}, {whCd}, {customerCode}.");
        }
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

    private FulfillmentSettlementDailyCostListResponse parseDailyCostListResponse(ResponseEntity<String> response) {
        if (FormatValidator.hasNoValue(response)) {
            return null;
        }

        String body = response.getBody();
        if (FormatValidator.hasNoValue(body)) {
            return null;
        }

        try {
            return objectMapper.readValue(body, FulfillmentSettlementDailyCostListResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: Fulfillment settlement daily costs 응답 파싱 실패, null 반환", e);
            return null;
        }
    }

    private boolean isDailyCostListSuccess(ResponseEntity<String> response, FulfillmentSettlementDailyCostListResponse parsedResponse) {
        return FormatValidator.hasValue(response)
                && response.getStatusCode().value() == 200
                && FormatValidator.hasValue(parsedResponse)
                && parsedResponse.isSuccess();
    }

    private String resolveDailyCostListException(
            ResponseEntity<String> response,
            FulfillmentSettlementDailyCostListResponse parsedResponse
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

    private JsonNode buildListRequestPayloadJson(FulfillmentSettlementDailyCostQuery query, URI uri, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", HttpMethod.GET.name());
        payload.put("url", uri.toString());
        payload.put("yearMonth", query.getYearMonth());
        payload.put("whCd", query.getWhCd());
        payload.put("customerCode", query.getCustomerCode());
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

    private GetFulfillmentSettlementDailyCostsResult mapDailyCostListResult(FulfillmentSettlementDailyCostListResponse response) {
        List<FulfillmentSettlementDailyCostInfoResult> dailyCosts = FormatValidator.hasValue(response.data())
                ? response.data().stream().map(this::mapDailyCostInfo).toList()
                : List.of();
        Integer dataCount = FormatValidator.hasValue(response.header()) ? response.header().dataCount() : null;
        return GetFulfillmentSettlementDailyCostsResult.of(dataCount, dailyCosts);
    }

    private FulfillmentSettlementDailyCostInfoResult mapDailyCostInfo(FulfillmentSettlementDailyCostItemResponse item) {
        return FulfillmentSettlementDailyCostInfoResult.builder()
                .closeDate(item.cloDt())
                .warehouseCode(item.whCd())
                .customerCode(item.cstCd())
                .inboundAmount(item.inpAmt())
                .outboundAmount(item.outAmt())
                .outboundCarAmount(item.outcarAmt())
                .outboundAirAmount(item.outairAmt())
                .storageAmount(item.keepAmt())
                .returnAmount(item.retAmt())
                .cashAmount(item.cashAmt())
                .foundationAmount(item.founAmt())
                .otherAmount(item.othAmt())
                .totalAmount(item.totAmt())
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

    private String resolveVendorMessage(FulfillmentSettlementDailyCostListResponse response, String rawBody) {
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
