package com.personal.marketnote.user.adapter.out.event.exception;

public class UserEventSerializationException extends RuntimeException {
    public UserEventSerializationException(String topic, Throwable cause) {
        super("ERR_USER_EVENT_01::유저 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
