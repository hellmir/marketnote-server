package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkTemplateStatusException extends IllegalArgumentException {

    public InvalidBrandTalkTemplateStatusException(String code) {
        super("ERR_BRAND_TALK_TEMPLATE_STATUS::알 수 없는 브랜드메시지 템플릿 상태입니다. code=" + code);
    }
}
