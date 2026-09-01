package com.personal.marketnote.notification.domain.notification;

public class InvalidNotificationException extends IllegalArgumentException {

    public InvalidNotificationException(String message) {
        super(message);
    }
}
