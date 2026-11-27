package com.personal.marketnote.common.configuration.security;

import com.personal.marketnote.common.configuration.security.exception.SecurityConfigurationValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityPropertiesValidatorTest {

    private SecurityPropertiesValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SecurityPropertiesValidator();
        setCommonRequiredFields(validator);
    }

    @Test
    @DisplayName("기프티콘 PIN 검증 비활성화 + 키 미설정 시 검증을 통과한다")
    void shouldPassWhenGifticonPinValidationDisabledAndKeyEmpty() {
        ReflectionTestUtils.setField(validator, "gifticonPinValidationEnabled", false);
        ReflectionTestUtils.setField(validator, "gifticonPinEncryptKey", "");

        assertThatCode(() -> validator.validateSecurityProperties())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("기프티콘 PIN 검증 활성화 + 강한 키 설정 시 검증을 통과한다")
    void shouldPassWhenGifticonPinValidationEnabledAndStrongKey() {
        ReflectionTestUtils.setField(validator, "gifticonPinValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "gifticonPinEncryptKey", "strong-encrypt-key-value");

        assertThatCode(() -> validator.validateSecurityProperties())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("기프티콘 PIN 검증 활성화 + 키 미설정 시 검증에 실패한다")
    void shouldFailWhenGifticonPinValidationEnabledAndKeyEmpty() {
        ReflectionTestUtils.setField(validator, "gifticonPinValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "gifticonPinEncryptKey", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("gifticon.pin.encrypt-key");
    }

    @Test
    @DisplayName("기프티콘 PIN 검증 활성화 + 약한 기본값 설정 시 검증에 실패한다")
    void shouldFailWhenGifticonPinValidationEnabledAndWeakDefault() {
        ReflectionTestUtils.setField(validator, "gifticonPinValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "gifticonPinEncryptKey", "dev-pin-key-change-me");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("gifticon.pin.encrypt-key");
    }

    @Test
    @DisplayName("기프티콘 PIN 검증 활성화 + 32바이트 약한 기본값 설정 시 검증에 실패한다")
    void shouldFailWhenGifticonPinValidationEnabledAndWeak32BytesDefault() {
        ReflectionTestUtils.setField(validator, "gifticonPinValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "gifticonPinEncryptKey", "dev-pin-key-change-me-0123456789");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("gifticon.pin.encrypt-key");
    }

    @Test
    @DisplayName("Kafka SASL 비활성화 시 검증에 실패한다")
    void shouldFailWhenKafkaSaslDisabled() {
        ReflectionTestUtils.setField(validator, "kafkaSaslEnabled", false);

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.kafka.sasl.enabled");
    }

    @Test
    @DisplayName("Kafka SASL 프로토콜이 SASL_PLAINTEXT이면 검증에 실패한다")
    void shouldFailWhenKafkaSaslProtocolIsPlaintext() {
        ReflectionTestUtils.setField(validator, "kafkaSaslProtocol", "SASL_PLAINTEXT");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.kafka.sasl.protocol");
    }

    @Test
    @DisplayName("Kafka SASL 프로토콜이 설정되지 않으면 검증에 실패한다")
    void shouldFailWhenKafkaSaslProtocolEmpty() {
        ReflectionTestUtils.setField(validator, "kafkaSaslProtocol", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.kafka.sasl.protocol");
    }

    @Test
    @DisplayName("Kafka SASL 활성화 + SASL_SSL 프로토콜 + 자격증명 설정 시 검증을 통과한다")
    void shouldPassWhenKafkaSaslEnabledWithSaslSslAndCredentials() {
        ReflectionTestUtils.setField(validator, "kafkaSaslEnabled", true);
        ReflectionTestUtils.setField(validator, "kafkaSaslProtocol", "SASL_SSL");
        ReflectionTestUtils.setField(validator, "kafkaSaslUsername", "kafka-user");
        ReflectionTestUtils.setField(validator, "kafkaSaslPassword", "kafka-password");

        assertThatCode(() -> validator.validateSecurityProperties())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Kafka SASL 활성화 + 사용자명 미설정 시 검증에 실패한다")
    void shouldFailWhenKafkaSaslEnabledAndUsernameEmpty() {
        ReflectionTestUtils.setField(validator, "kafkaSaslUsername", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.kafka.sasl.username");
    }

    @Test
    @DisplayName("운영 환경에서 spring.data.redis.password가 미설정이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenRedisPasswordEmpty() {
        ReflectionTestUtils.setField(validator, "redisPassword", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.data.redis.password");
    }

    @Test
    @DisplayName("운영 환경에서 spring.data.redis.password가 null이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenRedisPasswordNull() {
        ReflectionTestUtils.setField(validator, "redisPassword", null);

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.data.redis.password");
    }

    @Test
    @DisplayName("운영 환경에서 spring.data.redis.password가 password 약한 기본값이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenRedisPasswordIsWeakDefault() {
        ReflectionTestUtils.setField(validator, "redisPassword", "password");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.data.redis.password");
    }

    @Test
    @DisplayName("운영 환경에서 spring.data.redis.password가 강한 값이면 검증 통과")
    void shouldPassWhenRedisPasswordIsStrong() {
        ReflectionTestUtils.setField(validator, "redisPassword", "strong-redis-password-value");

        assertThatCode(() -> validator.validateSecurityProperties())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("spring.jwt.secret이 미설정이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenJwtSecretEmpty() {
        ReflectionTestUtils.setField(validator, "jwtSecret", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.jwt.secret");
    }

    @Test
    @DisplayName("spring.jwt.secret이 약한 기본값(dev-secret-change-me)이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenJwtSecretIsWeakDefault() {
        ReflectionTestUtils.setField(validator, "jwtSecret", "dev-secret-change-me");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.jwt.secret");
    }

    @Test
    @DisplayName("spring.jwt.admin-access-token이 미설정이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenAdminAccessTokenEmpty() {
        ReflectionTestUtils.setField(validator, "adminAccessToken", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.jwt.admin-access-token");
    }

    @Test
    @DisplayName("spring.datasource.password가 미설정이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenDbPasswordEmpty() {
        ReflectionTestUtils.setField(validator, "dbPassword", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.datasource.password");
    }

    @Test
    @DisplayName("spring.hmac.secret-key가 미설정이면 SecurityConfigurationValidationException 발생")
    void shouldFailWhenHmacSecretKeyEmpty() {
        ReflectionTestUtils.setField(validator, "hmacSecretKey", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.hmac.secret-key");
    }

    @Test
    @DisplayName("Kafka SASL 활성화 + 비밀번호 미설정 시 검증에 실패한다")
    void shouldFailWhenKafkaSaslEnabledAndPasswordEmpty() {
        ReflectionTestUtils.setField(validator, "kafkaSaslPassword", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("spring.kafka.sasl.password");
    }

    @Test
    @DisplayName("Aligo 검증 활성화 + 키와 사용자ID가 모두 강한 값이면 검증을 통과한다")
    void shouldPassWhenAligoValidationEnabledAndStrongValues() {
        ReflectionTestUtils.setField(validator, "aligoValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "aligoApiKey", "real-aligo-key-value");
        ReflectionTestUtils.setField(validator, "aligoUserId", "aligo-user");

        assertThatCode(() -> validator.validateSecurityProperties())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Aligo 검증 활성화 + API 키 미설정 시 검증에 실패한다")
    void shouldFailWhenAligoValidationEnabledAndApiKeyEmpty() {
        ReflectionTestUtils.setField(validator, "aligoValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "aligoApiKey", "");
        ReflectionTestUtils.setField(validator, "aligoUserId", "aligo-user");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("aligo.api-key");
    }

    @Test
    @DisplayName("Aligo 검증 활성화 + 사용자ID 미설정 시 검증에 실패한다")
    void shouldFailWhenAligoValidationEnabledAndUserIdEmpty() {
        ReflectionTestUtils.setField(validator, "aligoValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "aligoApiKey", "real-aligo-key-value");
        ReflectionTestUtils.setField(validator, "aligoUserId", "");

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("aligo.user-id");
    }

    @Test
    @DisplayName("OAuth2 allowed-audiences 검증 비활성화 시 모든 audience 미설정이어도 검증을 통과한다")
    void shouldPassWhenOauth2AllowedAudiencesValidationDisabled() {
        ReflectionTestUtils.setField(validator, "oauth2AllowedAudiencesValidationEnabled", false);

        assertThatCode(() -> validator.validateSecurityProperties())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("OAuth2 allowed-audiences 검증 활성화 + 모든 벤더에 강한 값 설정 시 검증을 통과한다")
    void shouldPassWhenOauth2AllowedAudiencesEnabledAndStrongValues() {
        ReflectionTestUtils.setField(validator, "oauth2AllowedAudiencesValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "kakaoAllowedAudiences", List.of("real-kakao-audience"));
        ReflectionTestUtils.setField(validator, "googleAllowedAudiences", List.of("real-google-audience"));
        ReflectionTestUtils.setField(validator, "appleAllowedAudiences", List.of("real-apple-audience"));

        assertThatCode(() -> validator.validateSecurityProperties())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("OAuth2 allowed-audiences 검증 활성화 + 카카오 audience 리스트가 null이면 검증에 실패한다")
    void shouldFailWhenKakaoAllowedAudiencesNull() {
        ReflectionTestUtils.setField(validator, "oauth2AllowedAudiencesValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "kakaoAllowedAudiences", null);
        ReflectionTestUtils.setField(validator, "googleAllowedAudiences", List.of("real-google-audience"));
        ReflectionTestUtils.setField(validator, "appleAllowedAudiences", List.of("real-apple-audience"));

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("oauth2.kakao.allowed-audiences");
    }

    @Test
    @DisplayName("OAuth2 allowed-audiences 검증 활성화 + 구글 audience 모든 값이 빈 문자열이면 검증에 실패한다")
    void shouldFailWhenGoogleAllowedAudiencesAllEmpty() {
        ReflectionTestUtils.setField(validator, "oauth2AllowedAudiencesValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "kakaoAllowedAudiences", List.of("real-kakao-audience"));
        ReflectionTestUtils.setField(validator, "googleAllowedAudiences", List.of("", "  "));
        ReflectionTestUtils.setField(validator, "appleAllowedAudiences", List.of("real-apple-audience"));

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("oauth2.google.allowed-audiences");
    }

    @Test
    @DisplayName("OAuth2 allowed-audiences 검증 활성화 + 애플 audience에 와일드카드(*)가 있으면 검증에 실패한다")
    void shouldFailWhenAppleAllowedAudiencesContainsWildcard() {
        ReflectionTestUtils.setField(validator, "oauth2AllowedAudiencesValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "kakaoAllowedAudiences", List.of("real-kakao-audience"));
        ReflectionTestUtils.setField(validator, "googleAllowedAudiences", List.of("real-google-audience"));
        ReflectionTestUtils.setField(validator, "appleAllowedAudiences", List.of("*"));

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("와일드카드");
    }

    @Test
    @DisplayName("OAuth2 allowed-audiences 검증 활성화 + 카카오 audience에 약한 기본값이 있으면 검증에 실패한다")
    void shouldFailWhenKakaoAllowedAudiencesContainsWeakDefault() {
        ReflectionTestUtils.setField(validator, "oauth2AllowedAudiencesValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "kakaoAllowedAudiences", List.of("change-me"));
        ReflectionTestUtils.setField(validator, "googleAllowedAudiences", List.of("real-google-audience"));
        ReflectionTestUtils.setField(validator, "appleAllowedAudiences", List.of("real-apple-audience"));

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("기본 플레이스홀더");
    }

    @Test
    @DisplayName("OAuth2 allowed-audiences 검증 활성화 + 구글 audience 리스트에 빈 문자열이 일부 포함되면 검증에 실패한다")
    void shouldFailWhenGoogleAllowedAudiencesContainsBlankElement() {
        ReflectionTestUtils.setField(validator, "oauth2AllowedAudiencesValidationEnabled", true);
        ReflectionTestUtils.setField(validator, "kakaoAllowedAudiences", List.of("real-kakao-audience"));
        ReflectionTestUtils.setField(validator, "googleAllowedAudiences", List.of("real-google-audience", ""));
        ReflectionTestUtils.setField(validator, "appleAllowedAudiences", List.of("real-apple-audience"));

        assertThatThrownBy(() -> validator.validateSecurityProperties())
            .isInstanceOf(SecurityConfigurationValidationException.class)
            .hasMessageContaining("빈 값");
    }

    private void setCommonRequiredFields(SecurityPropertiesValidator validator) {
        ReflectionTestUtils.setField(validator, "jwtSecret", "valid-jwt-secret");
        ReflectionTestUtils.setField(validator, "adminAccessToken", "valid-admin-token");
        ReflectionTestUtils.setField(validator, "dbPassword", "valid-db-password");
        ReflectionTestUtils.setField(validator, "redisPassword", "valid-redis-password");
        ReflectionTestUtils.setField(validator, "hmacSecretKey", "valid-hmac-key");
        ReflectionTestUtils.setField(validator, "kafkaSaslEnabled", true);
        ReflectionTestUtils.setField(validator, "kafkaSaslProtocol", "SASL_SSL");
        ReflectionTestUtils.setField(validator, "kafkaSaslUsername", "kafka-user");
        ReflectionTestUtils.setField(validator, "kafkaSaslPassword", "kafka-password");
        ReflectionTestUtils.setField(validator, "gifticonPinValidationEnabled", false);
        ReflectionTestUtils.setField(validator, "gifticonPinEncryptKey", "");
    }
}
