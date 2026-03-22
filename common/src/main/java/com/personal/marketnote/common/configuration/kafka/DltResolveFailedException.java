package com.personal.marketnote.common.configuration.kafka;

public class DltResolveFailedException extends RuntimeException {
    public DltResolveFailedException(String topic, int partition, long offset) {
        super("DLT 메시지 해결 중 오류가 발생했습니다. topic=" + topic +
                ", partition=" + partition + ", offset=" + offset);
    }
}
