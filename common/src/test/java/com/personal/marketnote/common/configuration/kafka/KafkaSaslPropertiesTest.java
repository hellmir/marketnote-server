package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.configuration.kafka.exception.KafkaSaslPasswordNotConfiguredException;
import com.personal.marketnote.common.configuration.kafka.exception.KafkaSaslUsernameNotConfiguredException;
import com.personal.marketnote.common.configuration.kafka.exception.KafkaSslTruststoreNotFoundException;
import com.personal.marketnote.common.configuration.kafka.exception.UnsupportedKafkaSaslMechanismException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaSaslPropertiesTest {

    private KafkaSaslProperties properties;
    private Map<String, Object> props;

    @BeforeEach
    void setUp() {
        properties = new KafkaSaslProperties();
        props = new HashMap<>();
    }

    @Test
    @DisplayName("SASL이 비활성화이면 어떤 속성도 추가하지 않는다")
    void shouldNotApplyAnythingWhenDisabled() {
        properties.setEnabled(false);

        properties.applyTo(props);

        assertThat(props).isEmpty();
    }

    @Test
    @DisplayName("SASL 활성화 + 사용자명이 미설정이면 KafkaSaslUsernameNotConfiguredException을 던진다")
    void shouldThrowWhenUsernameNotConfigured() {
        properties.setEnabled(true);
        properties.setUsername(null);
        properties.setPassword("password");

        assertThatThrownBy(() -> properties.applyTo(props))
            .isInstanceOf(KafkaSaslUsernameNotConfiguredException.class);
    }

    @Test
    @DisplayName("SASL 활성화 + 비밀번호가 미설정이면 KafkaSaslPasswordNotConfiguredException을 던진다")
    void shouldThrowWhenPasswordNotConfigured() {
        properties.setEnabled(true);
        properties.setUsername("user");
        properties.setPassword(null);

        assertThatThrownBy(() -> properties.applyTo(props))
            .isInstanceOf(KafkaSaslPasswordNotConfiguredException.class);
    }

    @Test
    @DisplayName("SCRAM-SHA-256 메커니즘이면 ScramLoginModule을 사용하는 JAAS 설정을 생성한다")
    void shouldBuildJaasWithScramLoginModuleForScram256() {
        properties.setEnabled(true);
        properties.setMechanism("SCRAM-SHA-256");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("kafka-user");
        properties.setPassword("kafka-pass");

        properties.applyTo(props);

        assertThat(props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)).isEqualTo("SASL_SSL");
        assertThat(props.get(SaslConfigs.SASL_MECHANISM)).isEqualTo("SCRAM-SHA-256");
        String jaas = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertThat(jaas).contains("org.apache.kafka.common.security.scram.ScramLoginModule");
        assertThat(jaas).contains("username=\"kafka-user\"");
        assertThat(jaas).contains("password=\"kafka-pass\"");
    }

    @Test
    @DisplayName("SCRAM-SHA-512 메커니즘이면 ScramLoginModule을 사용하는 JAAS 설정을 생성한다")
    void shouldBuildJaasWithScramLoginModuleForScram512() {
        properties.setEnabled(true);
        properties.setMechanism("SCRAM-SHA-512");
        properties.setProtocol("SASL_PLAINTEXT");
        properties.setUsername("user");
        properties.setPassword("pass");

        properties.applyTo(props);

        String jaas = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertThat(jaas).contains("org.apache.kafka.common.security.scram.ScramLoginModule");
    }

    @Test
    @DisplayName("PLAIN 메커니즘이면 PlainLoginModule을 사용하는 JAAS 설정을 생성한다")
    void shouldBuildJaasWithPlainLoginModuleForPlain() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pass");

        properties.applyTo(props);

        String jaas = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertThat(jaas).contains("org.apache.kafka.common.security.plain.PlainLoginModule");
    }

    @Test
    @DisplayName("지원하지 않는 메커니즘이면 UnsupportedKafkaSaslMechanismException을 던진다")
    void shouldThrowWhenMechanismUnsupported() {
        properties.setEnabled(true);
        properties.setMechanism("GSSAPI");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pass");

        assertThatThrownBy(() -> properties.applyTo(props))
            .isInstanceOf(UnsupportedKafkaSaslMechanismException.class);
    }

    @Test
    @DisplayName("사용자명에 백슬래시와 큰따옴표가 포함되면 JAAS 설정에서 이스케이프한다")
    void shouldEscapeBackslashAndQuoteInUsername() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("us\\er\"name");
        properties.setPassword("pass");

        properties.applyTo(props);

        String jaas = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertThat(jaas).contains("username=\"us\\\\er\\\"name\"");
    }

    @Test
    @DisplayName("비밀번호에 백슬래시와 큰따옴표가 포함되면 JAAS 설정에서 이스케이프한다")
    void shouldEscapeBackslashAndQuoteInPassword() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pa\\ss\"word");

        properties.applyTo(props);

        String jaas = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertThat(jaas).contains("password=\"pa\\\\ss\\\"word\"");
    }

    @Test
    @DisplayName("프로토콜이 SASL_SSL이 아니면 SSL Truststore 속성을 추가하지 않는다")
    void shouldNotApplySslPropsWhenProtocolNotSaslSsl() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_PLAINTEXT");
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setSslTruststoreLocation("/tmp/truststore.jks");

        properties.applyTo(props);

        assertThat(props).doesNotContainKey(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG);
    }

    @Test
    @DisplayName("SASL_SSL이지만 truststore 위치 미설정이면 Truststore 속성을 추가하지 않는다")
    void shouldNotApplySslPropsWhenTruststoreLocationEmpty() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setSslTruststoreLocation(null);

        properties.applyTo(props);

        assertThat(props).doesNotContainKey(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG);
    }

    @Test
    @DisplayName("SASL_SSL + 절대경로 truststore 위치이면 그대로 props에 적용한다")
    void shouldApplyAbsolutePathTruststore() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setSslTruststoreLocation("/tmp/truststore.jks");
        properties.setSslTruststorePassword("trust-pass");

        properties.applyTo(props);

        assertThat(props.get(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG)).isEqualTo("/tmp/truststore.jks");
        assertThat(props.get(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG)).isEqualTo("trust-pass");
        assertThat(props.get(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG)).isEqualTo("");
    }

    @Test
    @DisplayName("SASL_SSL + truststore 비밀번호 미설정이면 비밀번호 속성을 추가하지 않는다")
    void shouldNotIncludeTruststorePasswordWhenEmpty() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setSslTruststoreLocation("/tmp/truststore.jks");
        properties.setSslTruststorePassword(null);

        properties.applyTo(props);

        assertThat(props.get(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG)).isEqualTo("/tmp/truststore.jks");
        assertThat(props).doesNotContainKey(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG);
    }

    @Test
    @DisplayName("SASL_SSL + classpath: 위치이면 임시 파일로 추출하여 props에 절대경로로 적용한다")
    void shouldExtractClasspathTruststoreToTempFile() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setSslTruststoreLocation("classpath:test-truststore.jks");

        properties.applyTo(props);

        String location = (String) props.get(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG);
        assertThat(location).endsWith(".jks");
        assertThat(location).doesNotContain("classpath:");
        assertThat(java.nio.file.Files.exists(java.nio.file.Paths.get(location))).isTrue();
    }

    @Test
    @DisplayName("SASL_SSL + classpath: 위치에 해당 리소스가 없으면 KafkaSslTruststoreNotFoundException을 던진다")
    void shouldThrowWhenClasspathTruststoreMissing() {
        properties.setEnabled(true);
        properties.setMechanism("PLAIN");
        properties.setProtocol("SASL_SSL");
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setSslTruststoreLocation("classpath:non-existent-truststore.jks");

        assertThatThrownBy(() -> properties.applyTo(props))
            .isInstanceOf(KafkaSslTruststoreNotFoundException.class);
    }
}
