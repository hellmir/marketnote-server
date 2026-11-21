package com.personal.marketnote.file.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class ImageEventSerializationException extends BaseEventSerializationException {

    public ImageEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_IMAGE_EVENT_01", "이미지 이벤트", topic, partitionKey, eventId, cause);
    }
}
