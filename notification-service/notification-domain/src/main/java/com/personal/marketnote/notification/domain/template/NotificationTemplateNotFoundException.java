package com.personal.marketnote.notification.domain.template;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class NotificationTemplateNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "알림 템플릿을 찾을 수 없습니다.";

    public NotificationTemplateNotFoundException() {
        super(MESSAGE);
    }
}
