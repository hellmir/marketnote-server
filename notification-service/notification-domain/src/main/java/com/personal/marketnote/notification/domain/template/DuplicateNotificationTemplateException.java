package com.personal.marketnote.notification.domain.template;

public class DuplicateNotificationTemplateException extends RuntimeException {
    public DuplicateNotificationTemplateException(String templateCode) {
        super("ERR_NOTIFICATION_TEMPLATE_04::이미 존재하는 템플릿 코드입니다. templateCode=" + templateCode);
    }
}
