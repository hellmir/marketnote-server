package com.personal.marketnote.common.configuration.kafka.exception;

import java.util.Set;

public class UnsupportedKafkaSaslMechanismException extends IllegalStateException {

    public UnsupportedKafkaSaslMechanismException(String mechanism, Set<String> supportedMechanisms) {
        super("지원하지 않는 SASL mechanism입니다: " + mechanism + ". 지원 목록: " + supportedMechanisms);
    }
}
