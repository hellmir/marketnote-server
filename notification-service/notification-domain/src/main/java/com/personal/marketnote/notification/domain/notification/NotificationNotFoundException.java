package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class NotificationNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "알림을 찾을 수 없습니다.";

    public NotificationNotFoundException() {
        super(MESSAGE);
    }
}
