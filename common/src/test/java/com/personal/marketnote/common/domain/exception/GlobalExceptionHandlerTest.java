package com.personal.marketnote.common.domain.exception;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
        @DisplayName("메시지 본문에 ::이 포함되어도 코드와 메시지가 올바르게 분리된다")
        void shouldHandleDoubleColonInMessageBody() {
            IllegalArgumentException exception =
                    new IllegalArgumentException("ERR_TEST_02::값 A::B는 유효하지 않습니다");

            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("ERR_TEST_02");
            assertThat(response.getBody().getMessage()).isEqualTo("값 A::B는 유효하지 않습니다");
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
        @DisplayName("DomainNotFoundException 핸들러는 404를 반환한다")
        void shouldReturn404ForDomainNotFoundException() {
            DomainNotFoundException exception =
                    new DomainNotFoundException("ERR_ENTITY_01::엔티티를 찾을 수 없습니다");

            ResponseEntity<ErrorResponse> response = handler.handleDomainNotFoundException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("ERR_ENTITY_01");
            assertThat(response.getBody().getMessage()).isEqualTo("엔티티를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("IOException 핸들러는 500과 고정 메시지를 반환한다")
        void shouldReturn500WithFixedMessageForIOException() {
            IOException exception = new IOException("/var/log/app/secret.log (No such file or directory)");

            ResponseEntity<ErrorResponse> response = handler.handleIOException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("/var/log");
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
    @DisplayName("NPE 핸들러 정보 노출 방지")
    class NpeInfoLeakPreventionTest {

        @Test
        @DisplayName("NPE 핸들러는 500을 반환한다")
        void shouldReturn500ForNullPointerException() {
            NullPointerException exception =
                    new NullPointerException("Cannot invoke \"com.personal.marketnote.commerce.domain.order.OrderStatus.isMe()\" because \"status\" is null");

            ResponseEntity<ErrorResponse> response = handler.handleNullPointerException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("NPE 핸들러는 응답에 내부 패키지/클래스 정보를 노출하지 않는다")
        void shouldNotExposeInternalInfoInNpeResponse() {
            NullPointerException exception =
                    new NullPointerException("Cannot invoke \"com.personal.marketnote.commerce.domain.order.OrderStatus.isMe()\" because \"status\" is null");

            ResponseEntity<ErrorResponse> response = handler.handleNullPointerException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).doesNotContain("com.personal");
            assertThat(response.getBody().getMessage()).doesNotContain("OrderStatus");
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
        }

        @Test
        @DisplayName("NPE 핸들러는 null 메시지에도 고정 문자열을 반환한다")
        void shouldReturnGenericMessageForNullNpeMessage() {
            NullPointerException exception = new NullPointerException();

            ResponseEntity<ErrorResponse> response = handler.handleNullPointerException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
        }
    }

    @Nested
    @DisplayName("404 Not Found 핸들러")
    class NotFoundHandlerTest {

        @Test
        @DisplayName("NoHandlerFoundException 핸들러는 404를 반환한다")
        void shouldReturn404ForNoHandlerFoundException() {
            NoHandlerFoundException exception =
                    new NoHandlerFoundException("GET", "/api/v1/nonexistent", null);

            ResponseEntity<ErrorResponse> response = handler.handleNoHandlerFoundException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
            assertThat(response.getBody().getMessage()).isEqualTo("요청한 경로를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("NoHandlerFoundException 응답에 요청 경로가 노출되지 않는다")
        void shouldNotExposeRequestPathInNoHandlerResponse() {
            NoHandlerFoundException exception =
                    new NoHandlerFoundException("GET", "/api/v1/internal/secret", null);

            ResponseEntity<ErrorResponse> response = handler.handleNoHandlerFoundException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).doesNotContain("/api/v1/internal/secret");
        }

        @Test
        @DisplayName("NoResourceFoundException 핸들러는 404를 반환한다")
        void shouldReturn404ForNoResourceFoundException() {
            NoResourceFoundException exception =
                    new NoResourceFoundException(HttpMethod.GET, "/static/missing.js");

            ResponseEntity<ErrorResponse> response = handler.handleNoResourceFoundException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
            assertThat(response.getBody().getMessage()).isEqualTo("요청한 리소스를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("NoResourceFoundException 응답에 리소스 경로가 노출되지 않는다")
        void shouldNotExposeResourcePathInNoResourceResponse() {
            NoResourceFoundException exception =
                    new NoResourceFoundException(HttpMethod.GET, "/internal/config.json");

            ResponseEntity<ErrorResponse> response = handler.handleNoResourceFoundException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).doesNotContain("/internal/config.json");
        }
    }

    @Nested
    @DisplayName("ArithmeticException 핸들러 정보 노출 방지")
    class ArithmeticExceptionInfoLeakPreventionTest {

        @Test
        @DisplayName("ArithmeticException 핸들러는 500 INTERNAL_SERVER_ERROR를 반환한다")
        void shouldReturn500ForArithmeticException() {
            ArithmeticException exception = new ArithmeticException("/ by zero");

            ResponseEntity<ErrorResponse> response = handler.handleArithmeticException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("ArithmeticException 핸들러는 내부 정보를 노출하지 않고 고정 메시지를 반환한다")
        void shouldNotExposeInternalInfoInArithmeticExceptionResponse() {
            ArithmeticException exception = new ArithmeticException("/ by zero");

            ResponseEntity<ErrorResponse> response = handler.handleArithmeticException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).doesNotContain("/ by zero");
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
        }
    }

    @Nested
    @DisplayName("예외 메시지 정보 유출 방지")
    class ExceptionMessageInfoLeakPreventionTest {

        @Test
        @DisplayName("HttpRequestMethodNotSupportedException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForHttpRequestMethodNotSupportedException() {
            org.springframework.web.HttpRequestMethodNotSupportedException exception =
                    new org.springframework.web.HttpRequestMethodNotSupportedException("DELETE", List.of("GET", "POST", "PUT"));

            ResponseEntity<ErrorResponse> response = handler.handleHttpRequestMethodNotSupportedException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("지원하지 않는 HTTP 메서드입니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("GET");
            assertThat(response.getBody().getMessage()).doesNotContain("POST");
        }

        @Test
        @DisplayName("HttpMediaTypeNotSupportedException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForHttpMediaTypeNotSupportedException() {
            org.springframework.web.HttpMediaTypeNotSupportedException exception =
                    new org.springframework.web.HttpMediaTypeNotSupportedException("Content-Type 'text/xml' is not supported");

            ResponseEntity<ErrorResponse> response = handler.handleHttpMediaTypeNotSupportedException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("지원하지 않는 미디어 타입입니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("text/xml");
        }

        @Test
        @DisplayName("IOException 핸들러는 내부 파일 경로를 노출하지 않는다")
        void shouldNotExposeFilePathInIOExceptionResponse() {
            IOException exception = new IOException("/var/log/app/secret.log (No such file or directory)");

            ResponseEntity<ErrorResponse> response = handler.handleIOException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).doesNotContain("/var/log");
            assertThat(response.getBody().getMessage()).doesNotContain("secret.log");
        }

        @Test
        @DisplayName("AuthenticationException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForAuthenticationException() {
            BadCredentialsException exception =
                    new BadCredentialsException("Full authentication is required to access this resource at /api/v1/internal");

            ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("인증에 실패했습니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("/api/v1/internal");
        }

        @Test
        @DisplayName("AccessDeniedException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForAccessDeniedException() {
            AccessDeniedException exception =
                    new AccessDeniedException("Access denied for user 'admin' to resource /api/v1/admin/settings");

            ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("접근이 거부되었습니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("/api/v1/admin");
        }

        @Test
        @DisplayName("DomainAlreadyExistsException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForDomainAlreadyExistsException() {
            DomainAlreadyExistsException exception =
                    new DomainAlreadyExistsException("A different object with the same identifier value was already associated: [com.personal.marketnote.product.entity.ProductJpaEntity#123]");

            ResponseEntity<ErrorResponse> response = handler.handleDomainAlreadyExistsException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("이미 존재하는 데이터입니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("com.personal");
        }

        @Test
        @DisplayName("MailException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForMailException() {
            MailSendException exception =
                    new MailSendException("Mail server connection failed; nested exception: javax.mail.MessagingException: Could not connect to SMTP host: smtp.internal.personal.com, port: 587");

            ResponseEntity<ErrorResponse> response = handler.handleMailException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("메일 발송에 실패했습니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("smtp");
        }

        @Test
        @DisplayName("MalformedJwtException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForMalformedJwtException() {
            MalformedJwtException exception =
                    new MalformedJwtException("Unable to read JSON value: {\"alg\":\"HS256\",\"typ\":\"JWT\"}");

            ResponseEntity<ErrorResponse> response = handler.handleMalformedJwtException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("유효하지 않은 토큰입니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("HS256");
        }

        @Test
        @DisplayName("UnsupportedJwtException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForUnsupportedJwtException() {
            UnsupportedJwtException exception =
                    new UnsupportedJwtException("Signed JWSs are not supported. Use unsigned JWTs instead.");

            ResponseEntity<ErrorResponse> response = handler.handleUnsupportedJwtException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("지원하지 않는 토큰입니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("JWS");
        }

        @Test
        @DisplayName("UncheckedIOException 핸들러는 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForUncheckedIOException() {
            UncheckedIOException exception =
                    new UncheckedIOException(new IOException("Connection reset by peer: /10.0.1.5:8080"));

            ResponseEntity<ErrorResponse> response = handler.handleUncheckedIOException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("10.0.1.5");
        }
    }

    @Nested
    @DisplayName("catch-all 핸들러 정보 노출 방지")
    class CatchAllHandlerTest {

        @Test
        @DisplayName("미처리 예외에 대해 500 INTERNAL_SERVER_ERROR를 반환한다")
        void shouldReturn500ForUnhandledException() {
            Exception exception = new Exception("Unexpected error occurred in internal processing");

            ResponseEntity<ErrorResponse> response = handler.handleException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        }

        @Test
        @DisplayName("미처리 예외 응답에 원본 예외 메시지를 노출하지 않고 고정 메시지를 반환한다")
        void shouldNotExposeOriginalMessageInCatchAllResponse() {
            Exception exception = new Exception(
                    "org.hibernate.query.sqm.UnknownPathException: Could not resolve attribute 'accumulatedPointRate' of 'com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity'");

            ResponseEntity<ErrorResponse> response = handler.handleException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("com.personal");
            assertThat(response.getBody().getMessage()).doesNotContain("PricePolicyJpaEntity");
        }

        @Test
        @DisplayName("null 메시지 예외에도 고정 메시지를 반환한다")
        void shouldReturnFixedMessageForNullMessageException() {
            Exception exception = new Exception((String) null);

            ResponseEntity<ErrorResponse> response = handler.handleException(exception);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
        }

        @Test
        @DisplayName("RuntimeException 직접 상속 커스텀 예외도 catch-all로 처리된다")
        void shouldHandleDirectRuntimeExceptionSubclass() {
            RuntimeException exception = new RuntimeException("Custom domain exception message") {
            };

            ResponseEntity<ErrorResponse> response = handler.handleException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
            assertThat(response.getBody().getMessage()).doesNotContain("Custom domain exception");
        }
    }

    @Nested
    @DisplayName("Logger 필드 접근 제어자")
    class LoggerFieldTest {

        @Test
        @DisplayName("Logger 필드는 private static final이다")
        void loggerFieldShouldBePrivateStaticFinal() throws NoSuchFieldException {
            Field logField = GlobalExceptionHandler.class.getDeclaredField("log");

            assertThat(Modifier.isPrivate(logField.getModifiers())).isTrue();
            assertThat(Modifier.isStatic(logField.getModifiers())).isTrue();
            assertThat(Modifier.isFinal(logField.getModifiers())).isTrue();
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
