package com.personal.marketnote.common.domain.exception;

/**
 * 공용 예외 메시지 상수.
 *
 * <p>주의: Bean Validation 의 {@code @Pattern(message=...)} 등 외부 포맷 체인을 거치지 않는 경로에
 * 직접 참조되므로 {@code %s} 등 포맷 specifier 포함 상수는 추가하지 않는다. 값 치환이 필요한 경우
 * 접두사 상수로 분리하고 concat 하거나, 해당 예외 클래스 내부 private 상수로 관리한다.
 */
public final class ExceptionMessage {
    public static final String INVALID_EMAIL_EXCEPTION_MESSAGE
            = "이메일 주소 형식이 잘못되었습니다. 예: abcd@abc.com";
    public static final String INVALID_ACCESS_TOKEN_EXCEPTION_MESSAGE = "유효하지 않은 Access Token입니다.";

    private ExceptionMessage() {
    }
}
