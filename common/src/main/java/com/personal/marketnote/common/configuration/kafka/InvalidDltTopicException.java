package com.personal.marketnote.common.configuration.kafka;

public class InvalidDltTopicException extends IllegalArgumentException {
    public InvalidDltTopicException(String topic) {
        super("허용되지 않은 DLT 토픽입니다. topic=" + topic);
    }
}
