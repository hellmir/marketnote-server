package com.personal.marketnote.notification.domain.aligo;

public class AligoInsufficientPointException extends AligoApiException {

    public AligoInsufficientPointException() {
        super("ERR_ALIGO_03::알리고 발송 포인트가 부족합니다.");
    }
}
