package com.personal.marketnote.notification.port.out.template;

import com.personal.marketnote.notification.domain.template.NotificationTemplate;

public interface SaveNotificationTemplatePort {

    Long save(NotificationTemplate notificationTemplate);
}
