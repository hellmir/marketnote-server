package com.personal.marketnote.product.adapter.out.web.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.exception.FileServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.out.file.DeleteProductImagesPort;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.domain.file.OwnerType.PRODUCT;
import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_MAX_REQUEST_COUNT;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class FileServiceClient implements FindProductImagesPort, DeleteProductImagesPort {
    private static final ProductServiceCommunicationTargetType TARGET_TYPE =
            ProductServiceCommunicationTargetType.PRODUCT_IMAGE;
    private static final ProductServiceCommunicationSenderType REQUEST_SENDER =
            ProductServiceCommunicationSenderType.PRODUCT;
    private static final ProductServiceCommunicationSenderType RESPONSE_SENDER =
            ProductServiceCommunicationSenderType.FILE;

    @Value("${file-service.base-url:http://localhost:9000}")
    private String fileServiceBaseUrl;

    private final RestTemplate restTemplate;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @Override
    public Optional<GetFilesResult> findImagesByProductIdAndSort(Long productId, FileSort sort) {
        URI uri = UriComponentsBuilder
                .fromUriString(fileServiceBaseUrl)
                .path("/api/v1/files")
                .queryParam("ownerType", PRODUCT)
                .queryParam("ownerId", productId)
                .queryParam("sort", sort)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath());
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        return sendRequest(uri, httpEntity, productId, sort);
    }

    public Optional<GetFilesResult> sendRequest(URI uri, HttpEntity<Void> httpEntity, Long productId, FileSort sort) {
        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<FilesContent>> response =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                httpEntity,
                                new ParameterizedTypeReference<>() {
                                }
                        );

                BaseResponse<FilesContent> body = response.getBody();
                FilesContent filesContent = null;
                if (FormatValidator.hasValue(body)) {
                    filesContent = body.content;
                }

                if (FormatValidator.hasNoValue(filesContent) || FormatValidator.hasNoValue(filesContent.files)) {
                    return Optional.empty();
                }

                List<GetFileResult> getFileResults = filesContent.files
                        .stream()
                        .map(getFileResult -> new GetFileResult(
                                getFileResult.id,
                                getFileResult.sort,
                                getFileResult.extension,
                                getFileResult.name,
                                getFileResult.s3Url,
                                getFileResult.resizedS3Urls,
                                getFileResult.orderNum
                        ))
                        .toList();

                return Optional.of(new GetFilesResult(getFileResults));
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.GET,
                        uri,
                        null,
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
                        String.valueOf(productId),
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        TARGET_TYPE,
                        String.valueOf(productId),
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, 2000L) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("Failed to find images by product id: {} and sort: {}", productId, sort);
        return Optional.empty();
    }

    @Override
    public void delete(Long fileId) {
        URI uri = UriComponentsBuilder
                .fromUriString(fileServiceBaseUrl)
                .path("/api/v1/files/{id}")
                .buildAndExpand(fileId)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "DELETE", uri.getPath());
        sendRequest(uri, new HttpEntity<>(headers), String.valueOf(fileId));
    }

    public void sendRequest(URI uri, HttpEntity<Void> httpEntity, String targetId) {
        try {
            restTemplate.exchange(uri, HttpMethod.DELETE, httpEntity, Void.class);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BaseResponse<T> {
        @JsonProperty("content")
        T content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FilesContent {
        @JsonProperty("files")
        List<FileItem> files;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileItem {
        @JsonProperty("id")
        Long id;

        @JsonProperty("sort")
        String sort;

        @JsonProperty("extension")
        String extension;

        @JsonProperty("name")
        String name;

        @JsonProperty("s3Url")
        String s3Url;

        @JsonProperty("resizedS3Urls")
        List<String> resizedS3Urls;

        @JsonProperty("orderNum")
        Long orderNum;
    }
}
