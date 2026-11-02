package com.personal.marketnote.common.configuration.kafka.exception;

public class KafkaSaslPasswordNotConfiguredException extends IllegalStateException {

    public KafkaSaslPasswordNotConfiguredException() {
        super("Kafka SASL 인증이 활성화되었으나 password가 설정되지 않았습니다. KAFKA_SASL_PASSWORD 환경변수를 확인하세요.");
    }
}
