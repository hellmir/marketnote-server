package com.personal.marketnote.common.domain.exception;

import com.personal.marketnote.common.utility.FormatValidator;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String LOG_ERROR_MESSAGE = "Error exception occurred: {}";
    private static final String LOG_WARN_MESSAGE = "Warning exception occurred: {}";
    private static final String LOG_INFO_MESSAGE = "Exception occurred: {}";

    private record ParsedMessage(String code, String message) {
        static ParsedMessage from(String errorMessage, HttpStatus httpStatus) {
            if (FormatValidator.hasNoValue(errorMessage)) {
                return new ParsedMessage(httpStatus.name(), "");
            }

            String[] messages = errorMessage.split("::");
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

    @ExceptionHandler(IOException.class)
    ResponseEntity<ErrorResponse> handleIOException(IOException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(LOG_ERROR_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(LOG_ERROR_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(ArithmeticException.class)
    ResponseEntity<ErrorResponse> handleArithmeticException(ArithmeticException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(LOG_ERROR_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        HttpStatus httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        log.info(LOG_INFO_MESSAGE, e.getMessage());
        return buildErrorResponse(httpStatus, httpStatus.name(), "요청한 경로를 찾을 수 없습니다.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        log.info(LOG_INFO_MESSAGE, e.getMessage());
        return buildErrorResponse(httpStatus, httpStatus.name(), "요청한 리소스를 찾을 수 없습니다.");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
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
                .collect(java.util.stream.Collectors.joining(", "));

        if (fieldErrorMessage.isEmpty()) {
            fieldErrorMessage = "요청 값이 올바르지 않습니다.";
        }

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, fieldErrorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "요청 본문을 읽을 수 없습니다.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        HttpStatus httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(MalformedJwtException.class)
    ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    ResponseEntity<ErrorResponse> handleUnsupportedJwtException(UnsupportedJwtException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(EntityExistsException.class)
    ResponseEntity<ErrorResponse> handleEntityExistsException(EntityExistsException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(MailException.class)
    ResponseEntity<ErrorResponse> handleMailException(MailException e) {
        HttpStatus httpStatus = HttpStatus.BAD_GATEWAY;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(UncheckedIOException.class)
    ResponseEntity<ErrorResponse> handleUncheckedIOException(UncheckedIOException e) {
        HttpStatus httpStatus = HttpStatus.BAD_GATEWAY;
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String message = "필수 요청 파라미터 '" + e.getParameterName() + "'이(가) 누락되었습니다.";
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String message = "요청 파라미터 '" + e.getName() + "'의 타입이 올바르지 않습니다.";
        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        log.error(LOG_ERROR_MESSAGE, e.getMessage(), e);
        return buildErrorResponse(httpStatus, httpStatus.name(), "데이터 무결성 제약 조건을 위반했습니다.");
    }
}
