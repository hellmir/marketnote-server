package com.personal.marketnote.community.adapter.out.web.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.community.utility.ServiceCommunicationRecorder;
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

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.domain.file.OwnerType.POST;
import static com.personal.marketnote.common.domain.file.OwnerType.REVIEW;
import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
import static com.personal.marketnote.common.utility.ApiConstant.INTER_SERVER_MAX_REQUEST_COUNT;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class FileServiceClient implements FindReviewImagesPort, FindPostImagesPort {
    private static final CommunityServiceCommunicationSenderType REQUEST_SENDER =
            CommunityServiceCommunicationSenderType.COMMUNITY;
    private static final CommunityServiceCommunicationSenderType RESPONSE_SENDER =
            CommunityServiceCommunicationSenderType.FILE;

    @Value("${file-service.base-url:http://localhost:9000}")
    private String fileServiceBaseUrl;

    private final RestTemplate restTemplate;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

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

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath());
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        return sendRequest(uri, httpEntity, ownerId, sort, ownerType);
    }

    private Optional<GetFilesResult> sendRequest(
            URI uri, HttpEntity<Void> httpEntity, Long ownerId, FileSort sort, OwnerType ownerType
    ) {
        CommunityServiceCommunicationTargetType targetType = resolveTargetType(ownerType);
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

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
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

        @JsonProperty("s3Url")
        String s3Url;

        @JsonProperty("resizedS3Urls")
        List<String> resizedS3Urls;

        @JsonProperty("orderNum")
        Long orderNum;
    }
}
