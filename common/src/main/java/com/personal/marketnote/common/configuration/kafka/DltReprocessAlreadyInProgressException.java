package com.personal.marketnote.common.configuration.kafka;

public class DltReprocessAlreadyInProgressException extends RuntimeException {
    public DltReprocessAlreadyInProgressException(String originalTopic) {
        super("해당 토픽의 DLT 재처리가 이미 진행 중입니다. originalTopic=" + originalTopic);
    }
}
