package com.personal.marketnote.commerce.adapter.in.web.exception;

import com.personal.marketnote.commerce.exception.DuplicateIdempotencyKeyException;
import com.personal.marketnote.commerce.exception.IdempotencyKeyInProgressException;
import com.personal.marketnote.common.domain.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 결제 취소 멱등성 관련 예외 응답 처리.
 *
 * <ul>
 *   <li>{@link IdempotencyKeyInProgressException}: 409 + content={"status":"IN_PROGRESS"}</li>
 *   <li>{@link DuplicateIdempotencyKeyException}:  409 — 동시 INSERT race 최종 노출분</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class PaymentCancelIdempotencyExceptionHandler {

    private static final String IDEMPOTENCY_STATUS_KEY = "status";
    private static final String IN_PROGRESS_STATUS_VALUE = "IN_PROGRESS";

    @ExceptionHandler(IdempotencyKeyInProgressException.class)
    public ResponseEntity<ErrorResponse> handleInProgress(IdempotencyKeyInProgressException e) {
        log.info("Idempotency-Key IN_PROGRESS 충돌: {}", e.getMessage());
        return buildConflictResponse(
                e.getMessage(),
                Map.of(IDEMPOTENCY_STATUS_KEY, IN_PROGRESS_STATUS_VALUE)
        );
    }

    @ExceptionHandler(DuplicateIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateIdempotencyKeyException e) {
        log.warn("Idempotency-Key UNIQUE 충돌 노출: {}", e.getMessage());
        return buildConflictResponse(e.getMessage(), null);
    }

    private ResponseEntity<ErrorResponse> buildConflictResponse(String rawMessage, Object content) {
        ParsedMessage parsed = ParsedMessage.from(rawMessage);
        ErrorResponse response = ErrorResponse.builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .code(parsed.code())
                .message(parsed.message())
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    private record ParsedMessage(String code, String message) {
        private static ParsedMessage from(String raw) {
            if (raw == null) {
                return new ParsedMessage(HttpStatus.CONFLICT.name(), "");
            }
            String[] parts = raw.split("::", 2);
            if (parts.length > 1) {
                return new ParsedMessage(parts[0].trim(), parts[1].trim());
            }
            return new ParsedMessage(HttpStatus.CONFLICT.name(), parts[0]);
        }
    }
}
