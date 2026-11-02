package com.personal.marketnote.notification.port.out.template;

import com.personal.marketnote.notification.domain.template.NotificationTemplate;

import java.util.List;
import java.util.Optional;

public interface FindNotificationTemplatePort {

    Optional<NotificationTemplate> findActiveById(Long id);

    Optional<NotificationTemplate> findActiveByTemplateCode(String templateCode);

    List<NotificationTemplate> findAllActive();
}
