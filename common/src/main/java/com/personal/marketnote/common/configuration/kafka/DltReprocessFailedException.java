package com.personal.marketnote.common.configuration.kafka;

public class DltReprocessFailedException extends RuntimeException {
    public DltReprocessFailedException(String originalTopic) {
        super("DLT 메시지 재처리 중 오류가 발생했습니다. originalTopic=" + originalTopic);
    }
}
