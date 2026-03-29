package com.personal.marketnote.fulfillment.adapter.out.web.commerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.CommerceServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.out.web.commerce.request.SyncFulfillmentVendorInventoryRequest;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationType;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryCommand;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryItemCommand;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryPort;
import com.personal.marketnote.fulfillment.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
@Slf4j
public class CommerceServiceClient implements UpdateCommerceInventoryPort {
    private static final FulfillmentServiceCommunicationTargetType TARGET_TYPE =
            FulfillmentServiceCommunicationTargetType.COMMERCE_INVENTORY;
    private static final FulfillmentServiceCommunicationSenderType REQUEST_SENDER =
            FulfillmentServiceCommunicationSenderType.FULFILLMENT;
    private static final FulfillmentServiceCommunicationSenderType RESPONSE_SENDER =
            FulfillmentServiceCommunicationSenderType.COMMERCE;

    private final RestClient restClient;
    private final String commerceServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    public CommerceServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${commerce-service.base-url}") String commerceServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ServiceCommunicationRecorder serviceCommunicationRecorder,
            ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator
    ) {
        this.restClient = restClientBuilder.build();
        this.commerceServiceBaseUrl = commerceServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.serviceCommunicationRecorder = serviceCommunicationRecorder;
        this.serviceCommunicationPayloadGenerator = serviceCommunicationPayloadGenerator;
    }

    @Override
    public void updateInventories(UpdateCommerceInventoryCommand command) {
        if (FormatValidator.hasNoValue(command) || FormatValidator.hasNoValue(command.inventories())) {
            throw new IllegalArgumentException("Update commerce inventory command is required.");
        }
        if (FormatValidator.hasNoValue(commerceServiceBaseUrl)) {
            throw new CommerceServiceRequestFailedException(new IOException());
        }

        String baseUrl = Objects.requireNonNull(commerceServiceBaseUrl, "commerceServiceBaseUrl");

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/api/v1/internal/inventories/fulfillment/vendors/stocks/sync")
                .build()
                .toUri();

        SyncFulfillmentVendorInventoryRequest request = SyncFulfillmentVendorInventoryRequest.from(command);

        String targetId = resolveTargetId(command);
        sendRequest(uri, request, targetId);
    }

    private void sendRequest(
            URI uri,
            SyncFulfillmentVendorInventoryRequest request,
            String targetId
    ) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();
        URI requestUri = Objects.requireNonNull(uri, "uri");
        HttpMethod method = Objects.requireNonNull(HttpMethod.POST, "method");

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                restClient.post()
                        .uri(requestUri)
                        .headers(h -> hmacServiceAuthHeaderBuilder.applyHeaders(h, "POST", requestUri.getPath()))
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        method,
                        requestUri,
                        request,
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                        exception,
                        e.getMessage(),
                        attempt
                );
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        TARGET_TYPE,
                        targetId,
                        FulfillmentServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        targetId,
                        FulfillmentServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to sync commerce inventories: {} with error: {}", requestUri, error.getMessage(), error);
        throw new CommerceServiceRequestFailedException(new IOException(error));
    }

    private String resolveTargetId(UpdateCommerceInventoryCommand command) {
        if (FormatValidator.hasNoValue(command) || FormatValidator.hasNoValue(command.inventories())) {
            return null;
        }

        return command.inventories().stream()
                .map(UpdateCommerceInventoryItemCommand::productId)
                .filter(productId -> FormatValidator.hasValue(productId))
                .map(String::valueOf)
                .limit(10)
                .collect(Collectors.joining(","));
    }

    private void recordCommunication(
            FulfillmentServiceCommunicationTargetType targetType,
            String targetId,
            FulfillmentServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        FulfillmentServiceCommunicationSenderType sender =
                communicationType == FulfillmentServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
        serviceCommunicationRecorder.record(
                targetType,
                communicationType,
                sender,
                targetId,
                payload,
                payloadJson,
                exception
        );
    }
}
