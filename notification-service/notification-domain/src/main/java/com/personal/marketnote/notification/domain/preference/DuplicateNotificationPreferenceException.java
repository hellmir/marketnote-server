package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateNotificationPreferenceException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_NOTIFICATION_PREFERENCE_02::이미 초기화된 알림 수신 설정입니다.";

    public DuplicateNotificationPreferenceException() {
        super(MESSAGE);
    }
}
