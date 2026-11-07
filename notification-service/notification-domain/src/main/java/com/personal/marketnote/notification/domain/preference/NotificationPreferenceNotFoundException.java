package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class NotificationPreferenceNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "알림 수신 설정을 찾을 수 없습니다.";

    public NotificationPreferenceNotFoundException() {
        super(MESSAGE);
    }
}
