package com.personal.marketnote.notification.domain.aligo;

public class AligoBrandTalkTemplateNotFoundException extends RuntimeException {

    public AligoBrandTalkTemplateNotFoundException(String templateCode) {
        super("ERR_BRAND_TALK_TEMPLATE_01::브랜드메시지 템플릿을 찾을 수 없습니다. templateCode=" + templateCode);
    }
}
