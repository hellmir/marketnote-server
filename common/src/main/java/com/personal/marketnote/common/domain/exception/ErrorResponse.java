package com.personal.marketnote.common.domain.exception;

import lombok.Builder;
import lombok.Getter;

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
}
