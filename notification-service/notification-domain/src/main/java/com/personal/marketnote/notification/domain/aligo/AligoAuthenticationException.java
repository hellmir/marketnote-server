package com.personal.marketnote.notification.domain.aligo;

public class AligoAuthenticationException extends AligoApiException {

    public AligoAuthenticationException() {
        super("ERR_ALIGO_01::알리고 API 인증에 실패했습니다.");
    }
}
