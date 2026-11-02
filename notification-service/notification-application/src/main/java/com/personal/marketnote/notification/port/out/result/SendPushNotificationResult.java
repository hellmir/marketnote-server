package com.personal.marketnote.notification.port.out.result;

public record SendPushNotificationResult(
        boolean success,
        String messageId,
        String errorCode,
        boolean tokenInvalid
) {
    public static SendPushNotificationResult success(String messageId) {
        return new SendPushNotificationResult(true, messageId, null, false);
    }

    public static SendPushNotificationResult failure(String errorCode) {
        return new SendPushNotificationResult(false, null, errorCode, false);
    }

    public static SendPushNotificationResult tokenInvalid(String errorCode) {
        return new SendPushNotificationResult(false, null, errorCode, true);
    }
}
