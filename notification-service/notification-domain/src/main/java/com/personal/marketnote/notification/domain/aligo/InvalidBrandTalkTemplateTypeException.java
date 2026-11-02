package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkTemplateTypeException extends IllegalArgumentException {

    public InvalidBrandTalkTemplateTypeException(String code) {
        super("ERR_BRAND_TALK_01::알 수 없는 브랜드메시지 템플릿 타입: " + code);
    }
}
