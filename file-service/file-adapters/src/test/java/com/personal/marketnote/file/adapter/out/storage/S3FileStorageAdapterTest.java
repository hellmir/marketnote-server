package com.personal.marketnote.file.adapter.out.storage;

import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.adapter.out.exception.S3UploadFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageAdapterTest {
    @InjectMocks
    private S3FileStorageAdapter s3FileStorageAdapter;

    @Mock
    private S3Client s3Client;

    @Nested
    @DisplayName("uploadFiles")
    class UploadFiles {
        @Test
        @DisplayName("파일 목록이 null이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFilesIsNull() {
            // when
            List<String> result = s3FileStorageAdapter.uploadFiles(null, OwnerType.PRODUCT, 1L);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("파일 목록이 비어있으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFilesIsEmpty() {
            // when
            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(), OwnerType.PRODUCT, 1L);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("파일 업로드 성공 시 S3 URL을 반환한다")
        void returnsS3UrlOnSuccessfulUpload() {
            // given
            ReflectionTestUtils.setField(s3FileStorageAdapter, "s3BucketName", "test-bucket");
            MockMultipartFile file = new MockMultipartFile("file", "test-image.jpg", "image/jpeg", "data".getBytes());

            // when
            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).startsWith("https://test-bucket.s3.amazonaws.com/product/1/");
            assertThat(result.get(0)).contains("test-image.jpg");
            verify(s3Client).putObject(any(PutObjectRequest.class), any(Path.class));
        }

        @Test
        @DisplayName("여러 파일 업로드 시 파일 수만큼 URL을 반환한다")
        void returnsMultipleUrlsForMultipleFiles() {
            // given
            ReflectionTestUtils.setField(s3FileStorageAdapter, "s3BucketName", "test-bucket");
            MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "data1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", "data2".getBytes());

            // when
            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(file1, file2), OwnerType.PRODUCT, 1L);

            // then
            assertThat(result).hasSize(2);
            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(Path.class));
        }

        @Test
        @DisplayName("파일명이 null이면 빈 문자열로 처리하여 업로드한다")
        void handlesNullOriginalFilename() {
            // given
            ReflectionTestUtils.setField(s3FileStorageAdapter, "s3BucketName", "test-bucket");
            MultipartFile file = new MockMultipartFile("file", null, "image/jpeg", "data".getBytes());

            // when
            List<String> result = s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).startsWith("https://test-bucket.s3.amazonaws.com/product/1/");
        }

        @Test
        @DisplayName("S3 업로드 중 IOException 발생 시 S3UploadFailedException을 던진다")
        void throwsS3UploadFailedExceptionOnIOException() {
            // given
            ReflectionTestUtils.setField(s3FileStorageAdapter, "s3BucketName", "test-bucket");
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            try {
                when(file.getBytes()).thenThrow(new java.io.IOException("disk full"));
            } catch (java.io.IOException ignored) {
            }

            // when & then
            assertThatThrownBy(() -> s3FileStorageAdapter.uploadFiles(List.of(file), OwnerType.PRODUCT, 1L))
                    .isInstanceOf(S3UploadFailedException.class);
        }
    }
}
