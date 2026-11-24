package com.personal.marketnote.common.domain.exception;

import com.personal.marketnote.common.logging.TraceIdMdcFilter;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@Builder
@Getter
public class ErrorResponse {
    /**
     * 응답의 상태 코드.
     */
    private int statusCode;

    /**
     * 응답 코드.
     */
    private String code;

    /**
     * 응답이 전송되는 시간을 나타내는 타임스탬프.
     */
    private LocalDateTime timestamp;

    /**
     * 응답 처리 결과, 실제 데이터를 나타내는 필드.
     */
    private Object content;

    /**
     * 클라이언트로 전달되는 메시지.
     */
    private String message;

    /**
     * 요청 추적용 자체 발급 ID (X-Request-ID 헤더 기반).
     * <p>주의: W3C Tracing의 traceId(16/32자 hex span 식별자)와 다르다. 본 필드는
     * MDC의 {@code requestId} 키 값을 노출하며 패턴은 {@code [A-Za-z0-9._-]{1,64}}이다.
     * 필드명 traceId는 클라이언트 API 호환성을 위해 유지한다.
     */
    private String traceId;

    public static ErrorResponse of(int statusCode, String code, String message) {
        return of(statusCode, code, message, null);
    }

    public static ErrorResponse of(int statusCode, String code, String message, Object content) {
        String rawRequestId = MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY);
        return ErrorResponse.builder()
                .statusCode(statusCode)
                .code(code)
                .message(message)
                .content(content)
                .timestamp(LocalDateTime.now())
                .traceId(FormatValidator.hasValue(rawRequestId) ? rawRequestId : null)
                .build();
    }
}
