package com.personal.marketnote.product.adapter.out.web.commerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.in.request.RegisterInventoryRequest;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.CommerceServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.out.inventory.RegisterInventoryPort;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
@Slf4j
public class CommerceServiceClient implements RegisterInventoryPort {
    private static final ProductServiceCommunicationTargetType TARGET_TYPE =
            ProductServiceCommunicationTargetType.INVENTORY;
    private static final ProductServiceCommunicationSenderType REQUEST_SENDER =
            ProductServiceCommunicationSenderType.PRODUCT;
    private static final ProductServiceCommunicationSenderType RESPONSE_SENDER =
            ProductServiceCommunicationSenderType.COMMERCE;

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
    public void registerInventory(Long productId, Long pricePolicyId) {
        URI uri = UriComponentsBuilder
                .fromUriString(commerceServiceBaseUrl)
                .path("/api/v1/internal/inventories")
                .build()
                .toUri();

        RegisterInventoryRequest requestBody = new RegisterInventoryRequest(productId, pricePolicyId);
        sendRegisterRequest(uri, requestBody, pricePolicyId);
    }

    private void sendRegisterRequest(URI uri, RegisterInventoryRequest requestBody, Long pricePolicyId) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();
        String targetId = FormatValidator.hasValue(pricePolicyId) ? String.valueOf(pricePolicyId) : null;

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<Void> responseEntity = restClient.post()
                        .uri(uri)
                        .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "POST", uri.getPath()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .toBodilessEntity();

                if (responseEntity.getStatusCode().isError()) {
                    throw new CommerceServiceRequestFailedException(new IOException("Commerce service returned error: " + responseEntity.getStatusCode()));
                }
                return;
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.POST,
                        uri,
                        requestBody,
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
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        targetId,
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to register inventory: {} with error: {}", uri, error.getMessage(), error);
        throw new CommerceServiceRequestFailedException(new IOException());
    }

    private void recordCommunication(
            ProductServiceCommunicationTargetType targetType,
            String targetId,
            ProductServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        ProductServiceCommunicationSenderType sender =
                communicationType == ProductServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
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
