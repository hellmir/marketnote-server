package com.personal.marketnote.common.configuration.kafka.exception;

public class KafkaSslTruststoreNotFoundException extends IllegalStateException {

    public KafkaSslTruststoreNotFoundException(String location) {
        super("Kafka SSL truststore를 찾을 수 없습니다. 경로를 확인하세요: " + location);
    }

    public KafkaSslTruststoreNotFoundException(String location, Throwable cause) {
        super("Kafka SSL truststore를 로드할 수 없습니다. 경로를 확인하세요: " + location, cause);
    }
}
