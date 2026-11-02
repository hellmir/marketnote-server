package com.personal.marketnote.common.configuration.kafka.exception;

public class KafkaSaslUsernameNotConfiguredException extends IllegalStateException {

    public KafkaSaslUsernameNotConfiguredException() {
        super("Kafka SASL 인증이 활성화되었으나 username이 설정되지 않았습니다. KAFKA_SASL_USERNAME 환경변수를 확인하세요.");
    }
}
