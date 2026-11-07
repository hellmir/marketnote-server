package com.personal.marketnote.common.domain.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.personal.marketnote.common.utility.FormatValidator;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String LOG_ERROR_MESSAGE = "Error exception occurred: {}";
    private static final String LOG_WARN_MESSAGE = "Warning exception occurred: {}";
    private static final String LOG_INFO_MESSAGE = "Exception occurred: {}";

    private final Counter sseAsyncUnusableCounter;

    // MeterRegistry는 Spring actuator 빈으로 항상 존재하며 ControllerAdvice는 컨텍스트 외 인스턴스화가 없으므로 null 가드 불필요.
    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.sseAsyncUnusableCounter = Counter.builder("sse.async.unusable.total")
                .description("SSE 비동기 요청 사용 불가 예외 총 건수 (클라이언트 커넥션 종료 등)")
                .register(meterRegistry);
    }

    private record ParsedMessage(String code, String message) {
        static ParsedMessage from(String errorMessage, HttpStatus httpStatus) {
            if (FormatValidator.hasNoValue(errorMessage)) {
                return new ParsedMessage(httpStatus.name(), "");
            }

            String[] messages = errorMessage.split("::", 2);
            if (messages.length > 1) {
                return new ParsedMessage(messages[0].trim(), messages[1].trim());
            }

            return new ParsedMessage(httpStatus.name(), messages[0]);
        }
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus httpStatus, String code, String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus httpStatus, String errorMessage) {
        ParsedMessage parsed = ParsedMessage.from(errorMessage, httpStatus);
        return buildErrorResponse(httpStatus, parsed.code(), parsed.message());
    }

    // 로그 유출 방지: rejectedValue(사용자 입력 원본)을 제외하고 field + constraint 메시지만 포맷한다.
    private String formatValidationLogMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(fieldError -> "field=" + fieldError.getField()
                        + ", constraint=" + safeConstraintText(fieldError.getDefaultMessage()))
                .collect(Collectors.joining(", "));
    }

    // 로그 유출 방지: invalidValue를 제외하고 propertyPath + message만 포맷한다.
    private String formatConstraintViolationLogMessage(ConstraintViolationException e) {
        return e.getConstraintViolations().stream()
                .map(violation -> "field=" + violation.getPropertyPath()
                        + ", constraint=" + safeConstraintText(violation.getMessage()))
                .collect(Collectors.joining(", "));
    }

    private String safeConstraintText(String constraintMessage) {
        if (FormatValidator.hasNoValue(constraintMessage)) {
            return "";
        }
        return constraintMessage;
    }

    // 로그 유출 방지: e.getMessage() / 원본 JSON 본문을 제외하고 Jackson path만 추출한다.
    private String formatHttpMessageNotReadableLogMessage(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof MismatchedInputException mie) {
            String path = mie.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
            return "type=HttpMessageNotReadable, cause=" + cause.getClass().getSimpleName()
                    + (path.isEmpty() ? "" : ", path=" + path);
        }
        return "type=HttpMessageNotReadable, cause="
                + (FormatValidator.hasNoValue(cause) ? "unknown" : cause.getClass().getSimpleName());
    }

    // 로그 유출 방지: e.getValue() (원본 문자열) 제외. name + requiredType만 포맷한다.
    private String formatMethodArgumentTypeMismatchLogMessage(MethodArgumentTypeMismatchException e) {
        String requiredTypeName = FormatValidator.hasNoValue(e.getRequiredType())
                ? "unknown"
                : e.getRequiredType().getSimpleName();
        return "type=MethodArgumentTypeMismatch, name=" + e.getName() + ", requiredType=" + requiredTypeName;
    }

    // 로그 유출 방지: JJWT 라이브러리가 토큰 일부를 메시지에 포함할 수 있으므로 class/cause만 포맷한다.
    private String formatJwtExceptionLogMessage(JwtException e) {
        Throwable cause = e.getCause();
        return "type=" + e.getClass().getSimpleName()
                + (FormatValidator.hasNoValue(cause) ? "" : ", cause=" + cause.getClass().getSimpleName());
    }

    // 로그 유출 방지: 원본 메시지(파일 경로, SQL, PII, 내부 클래스명 등)를 제외하고 예외 type/cause만 포맷한다.
    private static String formatGenericLogMessage(Throwable e) {
        Throwable cause = e.getCause();
        return "type=" + e.getClass().getSimpleName()
                + (FormatValidator.hasNoValue(cause) ? "" : ", cause=" + cause.getClass().getSimpleName());
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<ErrorResponse> handleIOException(IOException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(LOG_ERROR_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(NullPointerException.class)
    ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(LOG_ERROR_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(ArithmeticException.class)
    ResponseEntity<ErrorResponse> handleArithmeticException(ArithmeticException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(LOG_ERROR_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(DomainNotFoundException.class)
    ResponseEntity<ErrorResponse> handleDomainNotFoundException(DomainNotFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e
    ) {
        HttpStatus httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "지원하지 않는 HTTP 메서드입니다.");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        log.info(LOG_INFO_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "요청한 경로를 찾을 수 없습니다.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        log.info(LOG_INFO_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "요청한 리소스를 찾을 수 없습니다.");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        log.warn(LOG_WARN_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "인증에 실패했습니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        log.warn(LOG_WARN_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "접근이 거부되었습니다.");
    }

    @ExceptionHandler(BusinessSecurityException.class)
    ResponseEntity<ErrorResponse> handleBusinessSecurityException(BusinessSecurityException e) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        String fieldErrorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (fieldErrorMessage.isEmpty()) {
            fieldErrorMessage = "요청 값이 올바르지 않습니다.";
        }

        log.info(LOG_INFO_MESSAGE, formatValidationLogMessage(e.getBindingResult()));
        return buildErrorResponse(httpStatus, fieldErrorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.info(LOG_INFO_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));
        if (message.isEmpty()) {
            message = "요청 파라미터가 올바르지 않습니다.";
        }
        log.info(LOG_INFO_MESSAGE, formatConstraintViolationLogMessage(e));
        return buildErrorResponse(httpStatus, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.info(LOG_INFO_MESSAGE, formatHttpMessageNotReadableLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "요청 본문을 읽을 수 없습니다.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        HttpStatus httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "지원하지 않는 미디어 타입입니다.");
    }

    @ExceptionHandler(MalformedJwtException.class)
    ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.warn(LOG_WARN_MESSAGE, formatJwtExceptionLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "유효하지 않은 토큰입니다.");
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    ResponseEntity<ErrorResponse> handleUnsupportedJwtException(UnsupportedJwtException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.warn(LOG_WARN_MESSAGE, formatJwtExceptionLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "지원하지 않는 토큰입니다.");
    }

    @ExceptionHandler(DomainAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleDomainAlreadyExistsException(DomainAlreadyExistsException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "이미 존재하는 데이터입니다.");
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        log.info(LOG_INFO_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(MailException.class)
    ResponseEntity<ErrorResponse> handleMailException(MailException e) {
        HttpStatus httpStatus = HttpStatus.BAD_GATEWAY;
        log.error(LOG_ERROR_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "메일 발송에 실패했습니다.");
    }

    @ExceptionHandler(UncheckedIOException.class)
    ResponseEntity<ErrorResponse> handleUncheckedIOException(UncheckedIOException e) {
        HttpStatus httpStatus = HttpStatus.BAD_GATEWAY;
        log.error(LOG_ERROR_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e
    ) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String message = "필수 요청 파라미터 '" + e.getParameterName() + "'이(가) 누락되었습니다.";
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String message = "요청 파라미터 '" + e.getName() + "'의 타입이 올바르지 않습니다.";
        log.info(LOG_INFO_MESSAGE, formatMethodArgumentTypeMismatchLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), message);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException e) {
        HttpStatus httpStatus = HttpStatus.TOO_MANY_REQUESTS;
        log.warn(LOG_WARN_MESSAGE, e.getMessage());
        return buildErrorResponse(httpStatus, httpStatus.name(), "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        log.error(LOG_ERROR_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "데이터 무결성 제약 조건을 위반했습니다.");
    }

    // AsyncRequestNotUsableException은 IOException 하위 — 더 구체적이라 handleIOException보다 우선 매칭된다.
    // 응답 채널이 이미 unusable 상태이므로 ErrorResponse 본문 직렬화를 피하기 위해 Void를 반환한다.
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    ResponseEntity<Void> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.debug("비동기 요청 사용 불가 (클라이언트 커넥션 종료): {}", e.getMessage());
        sseAsyncUnusableCounter.increment();
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleException(Exception e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(LOG_ERROR_MESSAGE, formatGenericLogMessage(e));
        return buildErrorResponse(httpStatus, httpStatus.name(), "서버 내부 오류가 발생했습니다.");
    }
}
