package com.personal.marketnote.notification.domain.aligo;

public class AligoTemplateNotFoundException extends RuntimeException {

    public AligoTemplateNotFoundException(String templateCode) {
        super("ERR_ALIGO_TEMPLATE_01::알림톡 템플릿을 찾을 수 없습니다. templateCode=" + templateCode);
    }

    public AligoTemplateNotFoundException(Long id) {
        super("ERR_ALIGO_TEMPLATE_01::알림톡 템플릿을 찾을 수 없습니다. id=" + id);
    }
}
