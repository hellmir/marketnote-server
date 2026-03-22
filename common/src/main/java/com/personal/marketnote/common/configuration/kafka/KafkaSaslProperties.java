package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.configuration.kafka.exception.KafkaSaslPasswordNotConfiguredException;
import com.personal.marketnote.common.configuration.kafka.exception.KafkaSaslUsernameNotConfiguredException;
import com.personal.marketnote.common.configuration.kafka.exception.UnsupportedKafkaSaslMechanismException;
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
            throw new KafkaSaslUsernameNotConfiguredException();
        }
        if (password == null || password.isBlank()) {
            throw new KafkaSaslPasswordNotConfiguredException();
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
        throw new UnsupportedKafkaSaslMechanismException(mechanism, SUPPORTED_MECHANISMS);
    }
}
