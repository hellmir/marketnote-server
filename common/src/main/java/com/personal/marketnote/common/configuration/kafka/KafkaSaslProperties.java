package com.personal.marketnote.common.configuration.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = "password")
@ConfigurationProperties(prefix = "spring.kafka.sasl")
public class KafkaSaslProperties {

    private static final Set<String> SUPPORTED_MECHANISMS = Set.of(
            "SCRAM-SHA-256", "SCRAM-SHA-512", "PLAIN"
    );

    private boolean enabled = false;
    private String mechanism = "SCRAM-SHA-256";
    private String protocol = "SASL_PLAINTEXT";
    private String username;
    private String password;

    public void applyTo(Map<String, Object> props) {
        if (!enabled) {
            return;
        }
        validateCredentials();
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, protocol);
        props.put(SaslConfigs.SASL_MECHANISM, mechanism);
        props.put(SaslConfigs.SASL_JAAS_CONFIG, buildJaasConfig());
    }

    private void validateCredentials() {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException(
                    "Kafka SASL 인증이 활성화되었으나 username이 설정되지 않았습니다. KAFKA_SASL_USERNAME 환경변수를 확인하세요.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Kafka SASL 인증이 활성화되었으나 password가 설정되지 않았습니다. KAFKA_SASL_PASSWORD 환경변수를 확인하세요.");
        }
    }

    private String buildJaasConfig() {
        String loginModule = resolveLoginModule();
        String sanitizedUsername = username.replace("\\", "\\\\").replace("\"", "\\\"");
        String sanitizedPassword = password.replace("\\", "\\\\").replace("\"", "\\\"");
        return loginModule + " required "
                + "username=\"" + sanitizedUsername + "\" "
                + "password=\"" + sanitizedPassword + "\";";
    }

    private String resolveLoginModule() {
        if (mechanism.startsWith("SCRAM-SHA")) {
            return "org.apache.kafka.common.security.scram.ScramLoginModule";
        }
        if ("PLAIN".equals(mechanism)) {
            return "org.apache.kafka.common.security.plain.PlainLoginModule";
        }
        throw new IllegalStateException(
                "지원하지 않는 SASL mechanism입니다: " + mechanism
                        + ". 지원 목록: " + SUPPORTED_MECHANISMS);
    }
}
