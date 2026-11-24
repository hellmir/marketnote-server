package com.personal.marketnote.common.domain.exception;

import com.personal.marketnote.common.logging.TraceIdMdcFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse 정적 팩토리 requestId 주입")
class ErrorResponseTraceIdTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("MDC에 requestId가 있으면 ErrorResponse.of()는 동일 requestId를 가진 인스턴스를 반환한다")
    void includesTraceIdFromMdcWhenPresent() {
        String expectedTraceId = "trace-id-abc-123";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, expectedTraceId);

        ErrorResponse response = ErrorResponse.of(400, "ERR_TEST_01", "잘못된 요청");

        assertThat(response.getTraceId()).isEqualTo(expectedTraceId);
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getCode()).isEqualTo("ERR_TEST_01");
        assertThat(response.getMessage()).isEqualTo("잘못된 요청");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getContent()).isNull();
    }

    @Test
    @DisplayName("MDC에 requestId가 없으면 ErrorResponse.of()는 traceId가 null인 인스턴스를 반환한다")
    void traceIdIsNullWhenMdcEmpty() {
        ErrorResponse response = ErrorResponse.of(500, "ERR_TEST_02", "서버 오류");

        assertThat(response.getTraceId()).isNull();
        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("MDC requestId가 빈 문자열이면 ErrorResponse.of()는 traceId를 null로 정규화한다")
    void normalizesBlankTraceIdToNull() {
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "");

        ErrorResponse response = ErrorResponse.of(404, "ERR_TEST_03", "찾을 수 없음");

        assertThat(response.getTraceId()).isNull();
    }

    @Test
    @DisplayName("content 없는 of 오버로드는 content가 null인 인스턴스를 반환한다")
    void ofWithoutContentReturnsNullContent() {
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "trace-x");

        ErrorResponse response = ErrorResponse.of(403, "ERR_TEST_04", "접근 거부");

        assertThat(response.getContent()).isNull();
        assertThat(response.getTraceId()).isEqualTo("trace-x");
    }

    @Test
    @DisplayName("content 있는 of 오버로드는 인자 content를 그대로 보존한다")
    void ofWithContentPreservesContent() {
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "trace-y");
        Map<String, String> content = Map.of("status", "IN_PROGRESS");

        ErrorResponse response = ErrorResponse.of(409, "ERR_TEST_05", "충돌", content);

        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getTraceId()).isEqualTo("trace-y");
        assertThat(response.getStatusCode()).isEqualTo(409);
    }
}
