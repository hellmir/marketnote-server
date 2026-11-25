package com.personal.marketnote.file.adapter.out.storage;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.file.adapter.out.exception.S3UploadFailedException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.AwsResponseMetadata;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3ResponseMetadata;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("S3FileStorageAdapter 테스트")
class S3FileStorageAdapterTest {

    private static final String BUCKET = "test-bucket";

    private S3Client s3Client;
    private ExternalCallLogger externalCallLogger;
    private S3FileStorageAdapter s3FileStorageAdapter;

    private Logger s3Logger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        s3FileStorageAdapter = new S3FileStorageAdapter(s3Client, externalCallLogger);
        ReflectionTestUtils.setField(s3FileStorageAdapter, "s3BucketName", BUCKET);

        s3Logger = (Logger) LoggerFactory.getLogger(ExternalSystem.S3.loggerName());
        previousAdditive = s3Logger.isAdditive();
        previousLevel = s3Logger.getLevel();
        s3Logger.setAdditive(false);
        s3Logger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        s3Logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        s3Logger.detachAppender(appender);
        appender.stop();
        s3Logger.setAdditive(previousAdditive);
        s3Logger.setLevel(previousLevel);
    }

    @Nested
    @DisplayName("uploadFiles")
    class UploadFiles {
        @Test
        @DisplayName("파일 목록이 null이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFilesIsNull() {
            List<String> result = s3FileStorageAdapter.uploadFiles(null, OwnerType.PRODUCT, 1L);

            assertThat(result).isEmpty();
            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("파일 목록이 비어있으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFilesIsEmpty() {
            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(), OwnerType.PRODUCT, 1L);

            assertThat(result).isEmpty();
            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("파일 업로드 성공 시 S3 URL을 반환한다")
        void returnsS3UrlOnSuccessfulUpload() {
            stubPutObject("etag-1", "req-1");
            MockMultipartFile file = new MockMultipartFile("file", "test-image.jpg", "image/jpeg", "data".getBytes());

            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).startsWith("https://test-bucket.s3.amazonaws.com/product/1/");
            assertThat(result.get(0)).contains("test-image.jpg");
            verify(s3Client).putObject(any(PutObjectRequest.class), any(Path.class));
        }

        @Test
        @DisplayName("여러 파일 업로드 시 파일 수만큼 URL을 반환한다")
        void returnsMultipleUrlsForMultipleFiles() {
            stubPutObject("etag-multi", "req-multi");
            MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "data1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", "data2".getBytes());

            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(file1, file2), OwnerType.PRODUCT, 1L);

            assertThat(result).hasSize(2);
            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(Path.class));
        }

        @Test
        @DisplayName("파일명이 null이면 빈 문자열로 처리하여 업로드한다")
        void handlesNullOriginalFilename() {
            stubPutObject("etag-null", "req-null");
            MultipartFile file = new MockMultipartFile("file", null, "image/jpeg", "data".getBytes());

            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).startsWith("https://test-bucket.s3.amazonaws.com/product/1/");
        }

        @Test
        @DisplayName("S3 업로드 중 IOException 발생 시 S3UploadFailedException을 던진다")
        void throwsS3UploadFailedExceptionOnIOException() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            try {
                when(file.getBytes()).thenThrow(new java.io.IOException("disk full"));
            } catch (java.io.IOException ignored) {
            }

            assertThatThrownBy(() -> s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 1L))
                    .isInstanceOf(S3UploadFailedException.class);
        }
    }

    @Nested
    @DisplayName("DEBUG 로그")
    class DebugLogging {

        @Test
        @DisplayName("업로드 성공 시 operation/bucket/key/size 시작 로그가 기록된다")
        void recordsStartLogWithBucketKeySize() {
            stubPutObject("etag-x", "req-x");
            MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "abcd".getBytes());

            s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 7L);

            ILoggingEvent startEvent = findEvent("External call start: operation=upload");
            String message = startEvent.getFormattedMessage();
            assertThat(message).contains("operation=upload");
            assertThat(message).contains("bucket=" + BUCKET);
            assertThat(message).contains("key=product/7/");
            assertThat(message).contains("size=4");
        }

        @Test
        @DisplayName("업로드 성공 시 응답 메타(etag/requestId) DEBUG 로그가 기록된다")
        void recordsResponseMetaDebugLog() {
            stubPutObject("ETAG-RESP", "REQ-RESP");
            MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "abcd".getBytes());

            s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 7L);

            ILoggingEvent metaEvent = findEvent("S3 putObject response");
            String message = metaEvent.getFormattedMessage();
            assertThat(metaEvent.getLevel()).isEqualTo(Level.DEBUG);
            assertThat(message).contains("etag=ETAG-RESP");
            assertThat(message).contains("requestId=REQ-RESP");
            assertThat(message).contains("bucket=" + BUCKET);
            assertThat(message).contains("key=product/7/");
        }

        @Test
        @DisplayName("S3 호출 실패 시 ERROR 로그가 기록된다")
        void recordsErrorLogOnS3Failure() {
            RuntimeException cause = new RuntimeException("s3 boom");
            when(s3Client.putObject(any(PutObjectRequest.class), any(Path.class))).thenThrow(cause);
            MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "abcd".getBytes());

            assertThatThrownBy(() -> s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 7L))
                    .isSameAs(cause);

            ILoggingEvent errorEvent = findEvent("External call failure: operation=upload");
            assertThat(errorEvent.getLevel()).isEqualTo(Level.ERROR);
            String message = errorEvent.getFormattedMessage();
            assertThat(message).contains("bucket=" + BUCKET);
            assertThat(message).contains("key=product/7/");
        }
    }

    @Nested
    @DisplayName("deleteFiles")
    class DeleteFiles {
        @Test
        @DisplayName("URL 목록이 null이면 S3를 호출하지 않는다")
        void doesNothingWhenStorageUrlsIsNull() {
            s3FileStorageAdapter.deleteFiles(null);

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("URL 목록이 비어있으면 S3를 호출하지 않는다")
        void doesNothingWhenStorageUrlsIsEmpty() {
            s3FileStorageAdapter.deleteFiles(List.of());

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("URL prefix가 일치하면 deleteObject를 호출한다")
        void callsDeleteObjectWhenPrefixMatches() {
            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenReturn(DeleteObjectResponse.builder().build());
            String url = "https://" + BUCKET + ".s3.amazonaws.com/product/1/file.jpg";

            s3FileStorageAdapter.deleteFiles(List.of(url));

            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("URL prefix가 일치하지 않으면 skip하고 경고 로그를 남긴다")
        void skipsWhenPrefixDoesNotMatch() {
            String url = "https://other-bucket.s3.amazonaws.com/product/1/file.jpg";

            s3FileStorageAdapter.deleteFiles(List.of(url));

            verifyNoInteractions(s3Client);
            ILoggingEvent warnEvent = findEvent("S3 orphan cleanup skip");
            assertThat(warnEvent.getLevel()).isEqualTo(Level.WARN);
        }

        @Test
        @DisplayName("URL이 null이면 해당 항목을 skip한다")
        void skipsNullStorageUrl() {
            java.util.ArrayList<String> urls = new java.util.ArrayList<>();
            urls.add(null);

            s3FileStorageAdapter.deleteFiles(urls);

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("S3 deleteObject 실패 시 후속 cleanup이 계속 진행된다")
        void continuesCleanupWhenSingleDeleteFails() {
            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenThrow(new RuntimeException("first fail"))
                    .thenReturn(DeleteObjectResponse.builder().build());
            String url1 = "https://" + BUCKET + ".s3.amazonaws.com/product/1/a.jpg";
            String url2 = "https://" + BUCKET + ".s3.amazonaws.com/product/2/b.jpg";

            s3FileStorageAdapter.deleteFiles(List.of(url1, url2));

            verify(s3Client, times(2)).deleteObject(any(DeleteObjectRequest.class));
            ILoggingEvent warnEvent = findEvent("S3 orphan cleanup 단건 실패");
            assertThat(warnEvent.getLevel()).isEqualTo(Level.WARN);
        }
    }

    @Nested
    @DisplayName("logResponseMeta 분기")
    class LogResponseMetaBranches {
        @Test
        @DisplayName("DEBUG 로그가 비활성화되면 응답 메타 로그를 기록하지 않는다")
        void doesNotLogResponseMetaWhenDebugDisabled() {
            s3Logger.setLevel(Level.INFO);
            stubPutObject("etag-info", "req-info");
            MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "abcd".getBytes());

            s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 7L);

            boolean hasMetaLog = appender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("S3 putObject response"));
            assertThat(hasMetaLog).isFalse();
        }
    }

    private void stubPutObject(String etag, String requestId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-amz-request-id", requestId);
        AwsResponseMetadata awsMetadata = new AwsResponseMetadata(headers) {};
        S3ResponseMetadata s3Metadata = S3ResponseMetadata.create(awsMetadata);
        PutObjectResponse response = (PutObjectResponse) PutObjectResponse.builder()
                .eTag(etag)
                .build()
                .toBuilder()
                .responseMetadata(s3Metadata)
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(Path.class))).thenReturn(response);
    }

    private ILoggingEvent findEvent(String partialMessage) {
        return appender.list.stream()
                .filter(event -> event.getFormattedMessage().contains(partialMessage))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Expected log message containing: " + partialMessage + " but got " + appender.list
                ));
    }
}
