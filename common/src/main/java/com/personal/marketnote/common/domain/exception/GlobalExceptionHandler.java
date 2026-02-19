package com.personal.marketnote.common.domain.exception;

import com.personal.marketnote.common.utility.FormatValidator;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String LOG_ERROR_MESSAGE = "Error exception occurred: {}";
    private static final String LOG_WARN_MESSAGE = "Warning exception occurred: {}";
    private static final String LOG_INFO_MESSAGE = "Exception occurred: {}";

    private HttpStatus httpStatus;
    private String code;
    private String message;

    private void initializeMessage(String errorMessage) {
        if (FormatValidator.hasNoValue(errorMessage)) {
            return;
        }

        String[] messages = errorMessage.split("::");
        if (messages.length > 1) {
            code = messages[0].trim();
            message = messages[1].trim();
            return;
        }

        code = httpStatus.name();
        message = messages[0];
    }

    @ExceptionHandler(IOException.class)
    private ResponseEntity<ErrorResponse> handleIOException
            (IOException e) {
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        initializeMessage(e.getMessage());

        log.error(LOG_ERROR_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(NullPointerException.class)
    private ResponseEntity<ErrorResponse> handleNullPointerException
            (NullPointerException e) {
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        initializeMessage(e.getMessage());

        log.error(LOG_ERROR_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    private ResponseEntity<ErrorResponse> handleEntityNotFoundException
            (EntityNotFoundException e) {
        httpStatus = HttpStatus.NOT_FOUND;
        initializeMessage(e.getMessage());

        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    private ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException
            (HttpRequestMethodNotSupportedException e) {
        httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
        initializeMessage(e.getMessage());

        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(AuthenticationException.class)
    private ResponseEntity<ErrorResponse> handleAuthenticationException
            (AuthenticationException e) {
        httpStatus = HttpStatus.UNAUTHORIZED;
        initializeMessage(e.getMessage());

        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    private ResponseEntity<ErrorResponse> handleAccessDeniedException
            (AccessDeniedException e) {
        httpStatus = HttpStatus.FORBIDDEN;
        initializeMessage(e.getMessage());

        log.warn(LOG_WARN_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException
            (MethodArgumentNotValidException e) {
        httpStatus = HttpStatus.BAD_REQUEST;

        String fieldErrorMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");

        initializeMessage(fieldErrorMessage);

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<ErrorResponse> handleIllegalArgumentException
            (IllegalArgumentException e) {
        httpStatus = HttpStatus.BAD_REQUEST;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException
            (HttpMessageNotReadableException e) {
        httpStatus = HttpStatus.BAD_REQUEST;
        code = httpStatus.name();
        message = "요청 본문을 읽을 수 없습니다.";

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    private ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException
            (HttpMediaTypeNotSupportedException e) {
        httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(MalformedJwtException.class)
    private ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException e) {
        httpStatus = HttpStatus.BAD_REQUEST;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    private ResponseEntity<ErrorResponse> handleUnsupportedJwtException(UnsupportedJwtException e) {
        httpStatus = HttpStatus.BAD_REQUEST;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(EntityExistsException.class)
    private ResponseEntity<ErrorResponse> handleEntityExistsException(EntityExistsException e) {
        httpStatus = HttpStatus.CONFLICT;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(IllegalStateException.class)
    private ResponseEntity<ErrorResponse> handleMailException(IllegalStateException e) {
        httpStatus = HttpStatus.CONFLICT;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(MailException.class)
    private ResponseEntity<ErrorResponse> handleMailException(MailException e) {
        httpStatus = HttpStatus.BAD_GATEWAY;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }

    @ExceptionHandler(UncheckedIOException.class)
    private ResponseEntity<ErrorResponse> handleUncheckedIOException(UncheckedIOException e) {
        httpStatus = HttpStatus.BAD_GATEWAY;
        initializeMessage(e.getMessage());

        log.info(LOG_INFO_MESSAGE, e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .code(code)
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorResponse.getStatusCode()));
    }
}
