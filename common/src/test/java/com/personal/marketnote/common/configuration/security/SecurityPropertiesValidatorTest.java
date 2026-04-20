package com.personal.marketnote.common.configuration.security;

import com.personal.marketnote.common.configuration.security.exception.SecurityConfigurationValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

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

    private void setCommonRequiredFields(SecurityPropertiesValidator validator) {
        ReflectionTestUtils.setField(validator, "jwtSecret", "valid-jwt-secret");
        ReflectionTestUtils.setField(validator, "adminAccessToken", "valid-admin-token");
        ReflectionTestUtils.setField(validator, "dbPassword", "valid-db-password");
        ReflectionTestUtils.setField(validator, "redisPassword", "valid-redis-password");
        ReflectionTestUtils.setField(validator, "hmacSecretKey", "valid-hmac-key");
        ReflectionTestUtils.setField(validator, "kafkaSaslEnabled", false);
        ReflectionTestUtils.setField(validator, "kafkaSaslUsername", "");
        ReflectionTestUtils.setField(validator, "kafkaSaslPassword", "");
        ReflectionTestUtils.setField(validator, "gifticonPinValidationEnabled", false);
        ReflectionTestUtils.setField(validator, "gifticonPinEncryptKey", "");
    }
}
