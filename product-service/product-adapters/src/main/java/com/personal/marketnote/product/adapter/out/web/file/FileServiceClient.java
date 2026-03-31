package com.personal.marketnote.product.adapter.out.web.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.FileServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.out.file.DeleteProductImagesPort;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@ServiceAdapter
@Slf4j
public class FileServiceClient implements DeleteProductImagesPort {
    private static final ProductServiceCommunicationTargetType TARGET_TYPE =
            ProductServiceCommunicationTargetType.PRODUCT_IMAGE;
    private static final ProductServiceCommunicationSenderType REQUEST_SENDER =
            ProductServiceCommunicationSenderType.PRODUCT;
    private static final ProductServiceCommunicationSenderType RESPONSE_SENDER =
            ProductServiceCommunicationSenderType.FILE;

    private final RestClient restClient;
    private final String fileServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    public FileServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${file-service.base-url:http://localhost:9000}") String fileServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ServiceCommunicationRecorder serviceCommunicationRecorder,
            ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator
    ) {
        this.restClient = restClientBuilder.build();
        this.fileServiceBaseUrl = fileServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.serviceCommunicationRecorder = serviceCommunicationRecorder;
        this.serviceCommunicationPayloadGenerator = serviceCommunicationPayloadGenerator;
    }

    @Override
    public void delete(Long fileId) {
        URI uri = UriComponentsBuilder
                .fromUriString(fileServiceBaseUrl)
                .path("/api/v1/files/{id}")
                .buildAndExpand(fileId)
                .toUri();

        sendDeleteRequest(uri, String.valueOf(fileId));
    }

    private void sendDeleteRequest(URI uri, String targetId) {
        try {
            ResponseEntity<Void> responseEntity = restClient.delete()
                    .uri(uri)
                    .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "DELETE", uri.getPath()))
                    .retrieve()
                    .toBodilessEntity();

            if (responseEntity.getStatusCode().isError()) {
                throw new FileServiceRequestFailedException(new IOException("File service returned error: " + responseEntity.getStatusCode()));
            }
        } catch (Exception e) {
            String exception = e.getClass().getSimpleName();
            JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                    HttpMethod.DELETE,
                    uri,
                    null,
                    1
            );
            String requestPayload = requestPayloadJson.toString();
            JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                    exception,
                    e.getMessage(),
                    1
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
            throw new FileServiceRequestFailedException(new IOException());
        }
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
