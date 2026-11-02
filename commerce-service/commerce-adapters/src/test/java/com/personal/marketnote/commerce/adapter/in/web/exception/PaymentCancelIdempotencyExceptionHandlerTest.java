package com.personal.marketnote.commerce.adapter.in.web.exception;

import com.personal.marketnote.commerce.exception.DuplicateIdempotencyKeyException;
import com.personal.marketnote.commerce.exception.IdempotencyKeyInProgressException;
import com.personal.marketnote.common.domain.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentCancelIdempotencyExceptionHandler")
class PaymentCancelIdempotencyExceptionHandlerTest {

    private final PaymentCancelIdempotencyExceptionHandler handler = new PaymentCancelIdempotencyExceptionHandler();

    @Nested
    @DisplayName("handleInProgress")
    class HandleInProgress {

        @Test
        @DisplayName("IdempotencyKeyInProgressException 발생 시 409 + status=IN_PROGRESS content를 응답한다")
        void returnsConflictWithInProgressContent() {
            IdempotencyKeyInProgressException exception = new IdempotencyKeyInProgressException("KEY-123");

            ResponseEntity<ErrorResponse> response = handler.handleInProgress(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            ErrorResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getStatusCode()).isEqualTo(HttpStatus.CONFLICT.value());
            assertThat(body.getCode()).isEqualTo("ERR_PAYMENT_CANCEL_IDEMPOTENCY_11");
            assertThat(body.getMessage()).contains("KEY-123");
            assertThat(body.getContent()).isEqualTo(Map.of("status", "IN_PROGRESS"));
            assertThat(body.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("handleDuplicate")
    class HandleDuplicate {

        @Test
        @DisplayName("DuplicateIdempotencyKeyException 발생 시 409 + content=null 응답을 반환한다")
        void returnsConflictWithNullContent() {
            DuplicateIdempotencyKeyException exception = new DuplicateIdempotencyKeyException("KEY-456");

            ResponseEntity<ErrorResponse> response = handler.handleDuplicate(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            ErrorResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getStatusCode()).isEqualTo(HttpStatus.CONFLICT.value());
            assertThat(body.getCode()).isEqualTo("ERR_PAYMENT_CANCEL_IDEMPOTENCY_10");
            assertThat(body.getMessage()).contains("KEY-456");
            assertThat(body.getContent()).isNull();
        }
    }

    @Nested
    @DisplayName("ParsedMessage 포맷 파싱")
    class ParsedMessageParsing {

        @Test
        @DisplayName("CODE::MESSAGE 포맷을 파싱하여 code와 message를 분리한다")
        void parsesCodeAndMessageFormat() {
            IdempotencyKeyInProgressException exception = new IdempotencyKeyInProgressException("KEY-789");

            ResponseEntity<ErrorResponse> response = handler.handleInProgress(exception);

            ErrorResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getCode()).isEqualTo("ERR_PAYMENT_CANCEL_IDEMPOTENCY_11");
            assertThat(body.getMessage()).doesNotContain("::");
            assertThat(body.getMessage()).contains("이미 처리 중");
        }

        @Test
        @DisplayName("단일 메시지(::없음)이면 CODE=CONFLICT 디폴트를 사용한다")
        void usesDefaultConflictCodeWhenNoDelimiter() {
            DuplicateIdempotencyKeyException exception = new DuplicateIdempotencyKeyException("KEY-abc") {
                @Override
                public String getMessage() {
                    return "구분자 없는 메시지";
                }
            };

            ResponseEntity<ErrorResponse> response = handler.handleDuplicate(exception);

            ErrorResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getCode()).isEqualTo("CONFLICT");
            assertThat(body.getMessage()).isEqualTo("구분자 없는 메시지");
        }
    }
}
