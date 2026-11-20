package com.personal.marketnote.file.adapter.out.event.exception;

public class ImageEventSerializationException extends RuntimeException {
    public ImageEventSerializationException(String topic, Throwable cause) {
        super("ERR_IMAGE_EVENT_01::이미지 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
