package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.http.client.CommunicationFailureHandler;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentSettlementDailyCostItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentSettlementDailyCostListResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper.FasstoSettlementCommandToRequestMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.settlement.FulfillmentSettlementDailyCostQuery;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentSettlementDailyCostsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentSettlementDailyCostInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentSettlementDailyCostsPort;
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

    public FulfillmentSettlementClient(
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
    public GetFulfillmentSettlementDailyCostsResult getDailyCosts(GetFulfillmentSettlementDailyCostsCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            throw new IllegalArgumentException("Fulfillment settlement daily cost command is required.");
        }

        FulfillmentSettlementDailyCostQuery query = FasstoSettlementCommandToRequestMapper.mapToQuery(command);
        URI uri = buildSettlementDailyCostUri(query.getYearMonth(), query.getWhCd(), query.getCustomerCode());

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

                log.warn("Failed to get Fulfillment settlement daily costs: attempt={}, message={}", attempt, e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                sleep(sleepMillis);
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

            sleep(sleepMillis);
            sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
        }

        log.error("Failed to get Fulfillment settlement daily costs: {} with error: {}", uri, error.getMessage(), error);
        throw new GetFulfillmentSettlementDailyCostsFailedException(failureMessage, new IOException(error));
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
            throw new IllegalStateException("Fulfillment base URL is required.");
        }
        if (FormatValidator.hasNoValue(properties.getSettlementDailyCostPath())) {
            throw new IllegalStateException("Fulfillment settlement daily cost path is required.");
        }
        String path = properties.getSettlementDailyCostPath();
        if (!path.contains(YEAR_MONTH_PLACEHOLDER) || !path.contains(WH_CODE_PLACEHOLDER) || !path.contains(CUSTOMER_CODE_PLACEHOLDER)) {
            throw new IllegalStateException("Fulfillment settlement daily cost path must include {yearMonth}, {whCd}, {customerCode}.");
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
        } catch (Exception e) {
            log.warn("Failed to parse Fulfillment settlement daily costs response: {}", e.getMessage(), e);
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
        return FulfillmentSettlementDailyCostInfoResult.of(
                item.cloDt(),       // closeDate
                item.whCd(),        // warehouseCode
                item.cstCd(),       // customerCode
                item.inpAmt(),      // inboundAmount
                item.outAmt(),      // outboundAmount
                item.outcarAmt(),   // outboundCarAmount
                item.outairAmt(),   // outboundAirAmount
                item.keepAmt(),     // storageAmount
                item.retAmt(),      // returnAmount
                item.cashAmt(),     // cashAmount
                item.founAmt(),     // foundationAmount
                item.othAmt(),      // otherAmount
                item.totAmt()       // totalAmount
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
            long jitteredSleepMillis = ThreadLocalRandom.current()
                    .nextLong(Math.max(1L, millis) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
