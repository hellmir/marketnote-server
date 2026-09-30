package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.configuration.kafka.exception.KafkaSaslPasswordNotConfiguredException;
import com.personal.marketnote.common.configuration.kafka.exception.KafkaSaslUsernameNotConfiguredException;
import com.personal.marketnote.common.configuration.kafka.exception.KafkaSslTruststoreNotFoundException;
import com.personal.marketnote.common.configuration.kafka.exception.UnsupportedKafkaSaslMechanismException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"password", "sslTruststorePassword"})
@ConfigurationProperties(prefix = "spring.kafka.sasl")
public class KafkaSaslProperties {

    private static final String SASL_SSL_PROTOCOL = "SASL_SSL";
    private static final String CLASSPATH_PREFIX = "classpath:";

    private static final Set<String> SUPPORTED_MECHANISMS = Set.of(
            "SCRAM-SHA-256", "SCRAM-SHA-512", "PLAIN"
    );

    private boolean enabled = false;
    private String mechanism = "SCRAM-SHA-256";
    private String protocol = "SASL_SSL";
    private String username;
    private String password;
    private String sslTruststoreLocation;
    private String sslTruststorePassword;

    public void applyTo(Map<String, Object> props) {
        if (!enabled) {
            return;
        }
        validateCredentials();
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, protocol);
        props.put(SaslConfigs.SASL_MECHANISM, mechanism);
        props.put(SaslConfigs.SASL_JAAS_CONFIG, buildJaasConfig());
        applySslProperties(props);
    }

    private void applySslProperties(Map<String, Object> props) {
        if (!SASL_SSL_PROTOCOL.equals(protocol)) {
            return;
        }
        if (FormatValidator.hasNoValue(sslTruststoreLocation)) {
            return;
        }
        String resolvedPath = resolveTruststorePath(sslTruststoreLocation);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, resolvedPath);
        if (FormatValidator.hasValue(sslTruststorePassword)) {
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);
        }
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
    }

    private String resolveTruststorePath(String location) {
        if (!location.startsWith(CLASSPATH_PREFIX)) {
            return location;
        }
        String resourcePath = location.substring(CLASSPATH_PREFIX.length());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new KafkaSslTruststoreNotFoundException(location);
        }
        try (inputStream) {
            Path tempFile = Files.createTempFile("kafka-truststore-", ".jks");
            tempFile.toFile().deleteOnExit();
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new KafkaSslTruststoreNotFoundException(location, e);
        }
    }

    private void validateCredentials() {
        if (FormatValidator.hasNoValue(username)) {
            throw new KafkaSaslUsernameNotConfiguredException();
        }
        if (FormatValidator.hasNoValue(password)) {
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
