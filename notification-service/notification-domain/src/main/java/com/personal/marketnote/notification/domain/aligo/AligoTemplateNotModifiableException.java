package com.personal.marketnote.notification.domain.aligo;

public class AligoTemplateNotModifiableException extends AligoApiException {

    public AligoTemplateNotModifiableException() {
        super("ERR_ALIGO_05::알리고 템플릿을 수정할 수 없는 상태입니다.");
    }
}
