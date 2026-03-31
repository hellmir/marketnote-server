package com.personal.marketnote.community.adapter.out.web.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.common.exception.FileServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.community.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.domain.file.OwnerType.POST;
import static com.personal.marketnote.common.domain.file.OwnerType.REVIEW;
import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_MAX_REQUEST_COUNT;

@ServiceAdapter
@Slf4j
public class FileServiceClient implements FindReviewImagesPort, FindPostImagesPort {
    private static final CommunityServiceCommunicationSenderType REQUEST_SENDER =
            CommunityServiceCommunicationSenderType.COMMUNITY;
    private static final CommunityServiceCommunicationSenderType RESPONSE_SENDER =
            CommunityServiceCommunicationSenderType.FILE;

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
    public Optional<GetFilesResult> findImagesByReviewIdAndSort(Long reviewId, FileSort sort) {
        return findImagesByOwnerAndSort(REVIEW, reviewId, sort);
    }

    @Override
    public Optional<GetFilesResult> findImagesByPostIdAndSort(Long postId, FileSort sort) {
        return findImagesByOwnerAndSort(POST, postId, sort);
    }

    private Optional<GetFilesResult> findImagesByOwnerAndSort(OwnerType ownerType, Long ownerId, FileSort sort) {
        URI uri = UriComponentsBuilder
                .fromUriString(fileServiceBaseUrl)
                .path("/api/v1/files")
                .queryParam("ownerType", ownerType)
                .queryParam("ownerId", ownerId)
                .queryParam("sort", sort)
                .build()
                .toUri();

        return sendRequest(uri, ownerId, sort, ownerType);
    }

    private Optional<GetFilesResult> sendRequest(URI uri, Long ownerId, FileSort sort, OwnerType ownerType) {
        CommunityServiceCommunicationTargetType targetType = resolveTargetType(ownerType);

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<FilesContent>> responseEntity =
                        restClient.get()
                                .uri(uri)
                                .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath()))
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });

                if (responseEntity.getStatusCode().isError()) {
                    throw new FileServiceRequestFailedException(
                            new IOException("File service returned error: " + responseEntity.getStatusCode()));
                }

                BaseResponse<FilesContent> body = responseEntity.getBody();
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
                                getFileResult.storageUrl,
                                getFileResult.resizedStorageUrls,
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
                        targetType,
                        String.valueOf(ownerId),
                        CommunityServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        targetType,
                        String.valueOf(ownerId),
                        CommunityServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);

                if (i < INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    sleepWithJitter();
                }
            }
        }

        log.error("Failed to find images by owner type: {} id: {} and sort: {}", ownerType, ownerId, sort);
        return Optional.empty();
    }

    private CommunityServiceCommunicationTargetType resolveTargetType(OwnerType ownerType) {
        if (ownerType == REVIEW) {
            return CommunityServiceCommunicationTargetType.REVIEW_IMAGE;
        }
        return CommunityServiceCommunicationTargetType.POST_IMAGE;
    }

    private void sleepWithJitter() {
        try {
            long jitteredSleepMillis = ThreadLocalRandom.current()
                    .nextLong(Math.max(1L, INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void recordCommunication(
            CommunityServiceCommunicationTargetType targetType,
            String targetId,
            CommunityServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        CommunityServiceCommunicationSenderType sender =
                communicationType == CommunityServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
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

        @JsonProperty("storageUrl")
        String storageUrl;

        @JsonProperty("resizedStorageUrls")
        List<String> resizedStorageUrls;

        @JsonProperty("orderNum")
        Long orderNum;
    }
}
