package com.personal.marketnote.notification.configuration;

public class FirebaseInitializationException extends RuntimeException {

    public FirebaseInitializationException(Throwable cause) {
        super("ERR_FIREBASE_01::Firebase 초기화에 실패했습니다.", cause);
    }
}
