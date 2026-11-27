package com.personal.marketnote.commerce.adapter.in.web.exception;

import com.personal.marketnote.commerce.exception.DuplicateIdempotencyKeyException;
import com.personal.marketnote.commerce.exception.IdempotencyKeyInProgressException;
import com.personal.marketnote.common.domain.exception.ErrorResponse;
import com.personal.marketnote.common.logging.TraceIdMdcFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentCancelIdempotencyExceptionHandler requestId 응답 주입")
class PaymentCancelIdempotencyExceptionHandlerTraceIdTest {

    private final PaymentCancelIdempotencyExceptionHandler handler =
        new PaymentCancelIdempotencyExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("IdempotencyKeyInProgressException 처리 시 traceId가 응답에 포함된다")
    void inProgressIncludesTraceId() {
        String traceId = "trace-in-progress";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, traceId);

        ResponseEntity<ErrorResponse> response = handler.handleInProgress(
            new IdempotencyKeyInProgressException("KEY-1")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("DuplicateIdempotencyKeyException 처리 시 traceId가 응답에 포함된다")
    void duplicateIncludesTraceId() {
        String traceId = "trace-duplicate";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, traceId);

        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(
            new DuplicateIdempotencyKeyException("KEY-2")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("MDC에 requestId가 없으면 응답 traceId는 null이다")
    void traceIdIsNullWhenMdcEmpty() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(
            new DuplicateIdempotencyKeyException("KEY-3")
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isNull();
    }

    @Test
    @DisplayName("IN_PROGRESS 응답의 content가 status=IN_PROGRESS를 유지한다 (회귀 방지)")
    void inProgressContentPreserved() {
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "trace-x");

        ResponseEntity<ErrorResponse> response = handler.handleInProgress(
            new IdempotencyKeyInProgressException("KEY-4")
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo(Map.of("status", "IN_PROGRESS"));
    }
}
