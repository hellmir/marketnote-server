package com.personal.marketnote.file.service.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InMemoryMultipartFile")
class InMemoryMultipartFileTest {

    private static final String FIELD_NAME = "file";
    private static final String FILE_NAME = "test.txt";
    private static final String CONTENT_TYPE = "text/plain";
    private static final byte[] CONTENT = "hello world".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("메타데이터")
    class Metadata {

        @Test
        @DisplayName("name, originalFilename, contentType, size를 올바르게 반환한다")
        void shouldReturnMetadata() {
            InMemoryMultipartFile file = new InMemoryMultipartFile(FIELD_NAME, FILE_NAME, CONTENT_TYPE, CONTENT);

            assertThat(file.getName()).isEqualTo(FIELD_NAME);
            assertThat(file.getOriginalFilename()).isEqualTo(FILE_NAME);
            assertThat(file.getContentType()).isEqualTo(CONTENT_TYPE);
            assertThat(file.getSize()).isEqualTo(CONTENT.length);
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        @DisplayName("content가 null이면 isEmpty는 true이다")
        void shouldBeEmptyWhenContentNull() {
            InMemoryMultipartFile file = new InMemoryMultipartFile(FIELD_NAME, FILE_NAME, CONTENT_TYPE, null);

            assertThat(file.isEmpty()).isTrue();
            assertThat(file.getSize()).isEqualTo(0L);
        }

        @Test
        @DisplayName("content가 null이 아닌 0바이트 배열이면 isEmpty는 false이고 size는 0이다")
        void shouldNotBeEmptyForZeroLengthArrayButSizeZero() {
            // FormatValidator.hasNoValue(Object)는 null/blank toString 기준이므로 byte[0]은 "값 있음"으로 평가된다
            InMemoryMultipartFile file = new InMemoryMultipartFile(FIELD_NAME, FILE_NAME, CONTENT_TYPE, new byte[0]);

            assertThat(file.isEmpty()).isFalse();
            assertThat(file.getSize()).isEqualTo(0L);
        }

        @Test
        @DisplayName("content가 존재하면 isEmpty는 false이다")
        void shouldNotBeEmptyWhenContentPresent() {
            InMemoryMultipartFile file = new InMemoryMultipartFile(FIELD_NAME, FILE_NAME, CONTENT_TYPE, CONTENT);

            assertThat(file.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("getBytes / getInputStream")
    class BytesAndStream {

        @Test
        @DisplayName("getBytes는 생성 시 전달한 content를 그대로 반환한다")
        void shouldReturnContentBytes() throws IOException {
            InMemoryMultipartFile file = new InMemoryMultipartFile(FIELD_NAME, FILE_NAME, CONTENT_TYPE, CONTENT);

            assertThat(file.getBytes()).isEqualTo(CONTENT);
        }

        @Test
        @DisplayName("getInputStream은 content를 스트리밍하여 읽을 수 있다")
        void shouldStreamContent() throws IOException {
            InMemoryMultipartFile file = new InMemoryMultipartFile(FIELD_NAME, FILE_NAME, CONTENT_TYPE, CONTENT);

            try (InputStream is = file.getInputStream();
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[4];
                int read;
                while ((read = is.read(chunk)) != -1) {
                    buffer.write(chunk, 0, read);
                }
                assertThat(buffer.toByteArray()).isEqualTo(CONTENT);
            }
        }
    }

    @Nested
    @DisplayName("transferTo")
    class TransferTo {

        @Test
        @DisplayName("대상 파일에 content를 그대로 기록한다")
        void shouldWriteContentToFile(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
            InMemoryMultipartFile file = new InMemoryMultipartFile(FIELD_NAME, FILE_NAME, CONTENT_TYPE, CONTENT);
            File target = tempDir.resolve("out.txt").toFile();

            file.transferTo(target);

            assertThat(Files.readAllBytes(target.toPath())).isEqualTo(CONTENT);
        }
    }
}
