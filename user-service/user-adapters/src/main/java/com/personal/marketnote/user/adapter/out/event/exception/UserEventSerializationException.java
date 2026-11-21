package com.personal.marketnote.user.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class UserEventSerializationException extends BaseEventSerializationException {

    public UserEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_USER_EVENT_01", "유저 이벤트", topic, partitionKey, eventId, cause);
    }
}
