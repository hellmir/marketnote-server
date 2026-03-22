package com.personal.marketnote.common.configuration.kafka;

public class DltMessageNotFoundException extends RuntimeException {
    public DltMessageNotFoundException(String dltTopic, int partition, long offset) {
        super("DLT 메시지를 찾을 수 없습니다. dltTopic=" + dltTopic +
                ", partition=" + partition + ", offset=" + offset);
    }
}
