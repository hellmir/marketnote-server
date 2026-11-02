package com.personal.marketnote.notification.domain.preference;

public class InvalidNotificationPreferenceException extends IllegalArgumentException {
    public InvalidNotificationPreferenceException(String message) {
        super("ERR_NOTIFICATION_PREFERENCE_01::" + message);
    }
}
