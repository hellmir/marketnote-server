package com.personal.marketnote.common.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("에러 메시지 파싱")
    class ErrorMessageParsingTest {

        @Test
        @DisplayName("ERR_CODE::메시지 형식이면 코드와 메시지가 분리된다")
        void shouldParseCodeAndMessageFromFormattedError() {
            IllegalArgumentException exception = new IllegalArgumentException("ERR_TEST_01::테스트 에러 메시지");

            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("ERR_TEST_01");
            assertThat(response.getBody().getMessage()).isEqualTo("테스트 에러 메시지");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("일반 메시지이면 HTTP 상태명이 코드로 사용된다")
        void shouldUseHttpStatusNameAsCodeForPlainMessage() {
            IllegalArgumentException exception = new IllegalArgumentException("잘못된 요청입니다");

            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo("잘못된 요청입니다");
        }

        @Test
        @DisplayName("EntityNotFoundException 핸들러는 404를 반환한다")
        void shouldReturn404ForEntityNotFoundException() {
            jakarta.persistence.EntityNotFoundException exception =
                    new jakarta.persistence.EntityNotFoundException("ERR_ENTITY_01::엔티티를 찾을 수 없습니다");

            ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("ERR_ENTITY_01");
            assertThat(response.getBody().getMessage()).isEqualTo("엔티티를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("IOException 핸들러는 500을 반환한다")
        void shouldReturn500ForIOException() {
            IOException exception = new IOException("ERR_IO_01::파일 처리 실패");

            ResponseEntity<ErrorResponse> response = handler.handleIOException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("ERR_IO_01");
        }

        @Test
        @DisplayName("IllegalStateException 핸들러는 409를 반환한다")
        void shouldReturn409ForIllegalStateException() {
            IllegalStateException exception = new IllegalStateException("ERR_STATE_01::상태 충돌");

            ResponseEntity<ErrorResponse> response = handler.handleIllegalStateException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("ERR_STATE_01");
        }

        @Test
        @DisplayName("BusinessSecurityException 핸들러는 403을 반환한다")
        void shouldReturn403ForBusinessSecurityException() {
            BusinessSecurityException exception = new BusinessSecurityException("ERR_AUTH_01::접근 권한 없음") {
            };

            ResponseEntity<ErrorResponse> response = handler.handleBusinessSecurityException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("ERR_AUTH_01");
            assertThat(response.getBody().getMessage()).isEqualTo("접근 권한 없음");
        }
    }

    @Nested
    @DisplayName("스레드 안전성")
    class ThreadSafetyTest {

        @Test
        @DisplayName("동시 요청 시 각 핸들러가 올바른 코드와 메시지를 반환한다")
        void shouldHandleConcurrentRequestsWithCorrectResponses() throws Exception {
            int threadCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            List<Future<ResponseEntity<ErrorResponse>>> futures = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                int index = i;
                futures.add(executor.submit(() -> {
                    startLatch.await();

                    if (index % 2 == 0) {
                        IllegalArgumentException exception =
                                new IllegalArgumentException("ERR_ARG_" + index + "::인자 에러 " + index);
                        return handler.handleIllegalArgumentException(exception);
                    } else {
                        IllegalStateException exception =
                                new IllegalStateException("ERR_STATE_" + index + "::상태 에러 " + index);
                        return handler.handleIllegalStateException(exception);
                    }
                }));
            }

            startLatch.countDown();

            for (int i = 0; i < threadCount; i++) {
                ResponseEntity<ErrorResponse> response = futures.get(i).get();
                ErrorResponse body = response.getBody();

                assertThat(body).isNotNull();
                assertThat(body.getCode()).isEqualTo(i % 2 == 0 ? "ERR_ARG_" + i : "ERR_STATE_" + i);
                assertThat(body.getMessage()).isEqualTo(i % 2 == 0 ? "인자 에러 " + i : "상태 에러 " + i);

                if (i % 2 == 0) {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                } else {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                }
            }

            executor.shutdown();
        }
    }
}
