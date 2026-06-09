package com.personal.marketnote.notification.domain.template;

public class NotificationTemplateNotFoundException extends RuntimeException {
    public NotificationTemplateNotFoundException(Long id) {
        super("ERR_NOTIFICATION_TEMPLATE_03::알림 템플릿을 찾을 수 없습니다. id=" + id);
    }

    public NotificationTemplateNotFoundException(String templateCode) {
        super("ERR_NOTIFICATION_TEMPLATE_03::알림 템플릿을 찾을 수 없습니다. templateCode=" + templateCode);
    }
}
