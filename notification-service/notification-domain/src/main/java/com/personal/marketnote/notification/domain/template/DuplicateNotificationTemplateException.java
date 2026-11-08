package com.personal.marketnote.notification.domain.template;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateNotificationTemplateException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_NOTIFICATION_TEMPLATE_04::이미 존재하는 템플릿 코드입니다.";

    public DuplicateNotificationTemplateException() {
        super(MESSAGE);
    }
}
