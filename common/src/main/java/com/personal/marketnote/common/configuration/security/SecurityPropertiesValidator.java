package com.personal.marketnote.common.configuration.security;

import com.personal.marketnote.common.configuration.security.exception.SecurityConfigurationValidationException;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@Profile({"qa.test", "prod"})
public class SecurityPropertiesValidator {
    private static final Logger log = LoggerFactory.getLogger(SecurityPropertiesValidator.class);

    private static final Set<String> WEAK_DEFAULTS = Set.of(
            "dev-secret-change-me", "dev-hmac-secret-change-me",
            "dev-pin-key-change-me",
            "abc", "def", "ghi",
            "change-me", "password", "root", "secret", "test",
            "0000", "1234567890123456"
    );

    private static final String REQUIRED_KAFKA_SASL_PROTOCOL = "SASL_SSL";

    @Value("${spring.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.jwt.admin-access-token:}")
    private String adminAccessToken;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.hmac.secret-key:}")
    private String hmacSecretKey;

    @Value("${spring.kafka.sasl.enabled:false}")
    private boolean kafkaSaslEnabled;

    @Value("${spring.kafka.sasl.protocol:}")
    private String kafkaSaslProtocol;

    @Value("${spring.kafka.sasl.username:}")
    private String kafkaSaslUsername;

    @Value("${spring.kafka.sasl.password:}")
    private String kafkaSaslPassword;

    @Value("${security.validation.gifticon-pin-enabled:false}")
    private boolean gifticonPinValidationEnabled;

    @Value("${gifticon.pin.encrypt-key:}")
    private String gifticonPinEncryptKey;

    @Value("${security.validation.oauth2-allowed-audiences-enabled:false}")
    private boolean oauth2AllowedAudiencesValidationEnabled;

    @Value("${oauth2.kakao.allowed-audiences:}")
    private List<String> kakaoAllowedAudiences;

    @Value("${oauth2.google.allowed-audiences:}")
    private List<String> googleAllowedAudiences;

    @Value("${oauth2.apple.allowed-audiences:}")
    private List<String> appleAllowedAudiences;

    @PostConstruct
    public void validateSecurityProperties() {
        List<String> violations = new ArrayList<>();

        validateRequired(violations, "spring.jwt.secret (JWT_SECRET_KEY)", jwtSecret);
        validateRequired(violations, "spring.jwt.admin-access-token (JWT_ADMIN_ACCESS_TOKEN)", adminAccessToken);
        validateRequired(violations, "spring.datasource.password (DB_PASSWORD)", dbPassword);
        validateRequired(violations, "spring.hmac.secret-key (HMAC_SECRET_KEY)", hmacSecretKey);

        validateKafkaSasl(violations);

        if (gifticonPinValidationEnabled) {
            validateRequired(violations, "gifticon.pin.encrypt-key (GIFTICON_PIN_ENCRYPT_KEY)", gifticonPinEncryptKey);
        }

        if (oauth2AllowedAudiencesValidationEnabled) {
            validateAllowedAudiences(
                    violations, "oauth2.kakao.allowed-audiences (KAKAO_ALLOWED_AUDIENCES)", kakaoAllowedAudiences
            );
            validateAllowedAudiences(
                    violations, "oauth2.google.allowed-audiences (GOOGLE_ALLOWED_AUDIENCES)", googleAllowedAudiences
            );
            validateAllowedAudiences(
                    violations, "oauth2.apple.allowed-audiences (APPLE_ALLOWED_AUDIENCES)", appleAllowedAudiences
            );
        }

        if (!violations.isEmpty()) {
            String message = String.join("\n  - ", violations);
            throw new SecurityConfigurationValidationException(message);
        }

        log.info("보안 설정 검증 완료: 필수 시크릿이 올바르게 설정되었습니다.");
    }

    private void validateRequired(List<String> violations, String propertyName, String value) {
        if (FormatValidator.hasNoValue(value)) {
            violations.add(propertyName + " 값이 설정되지 않았습니다.");
            return;
        }
        if (WEAK_DEFAULTS.contains(value.toLowerCase())) {
            violations.add(propertyName + " 값이 기본 플레이스홀더입니다. 강력한 값으로 변경하세요.");
        }
    }

    private void validateAllowedAudiences(List<String> violations, String propertyName, List<String> values) {
        if (values == null || values.isEmpty()) {
            violations.add(propertyName + " 값이 설정되지 않았습니다.");
            return;
        }
        boolean allEmpty = values.stream().allMatch(FormatValidator::hasNoValue);
        if (allEmpty) {
            violations.add(propertyName + " 값이 모두 비어있습니다.");
            return;
        }
        for (String value : values) {
            if (FormatValidator.hasNoValue(value)) {
                violations.add(propertyName + " 목록에 빈 값이 포함되어 있습니다.");
                continue;
            }
            String trimmed = value.trim();
            if (WEAK_DEFAULTS.contains(trimmed.toLowerCase())) {
                violations.add(propertyName + " 값에 기본 플레이스홀더('" + trimmed + "')가 포함되어 있습니다.");
            }
            if ("*".equals(trimmed)) {
                violations.add(propertyName + " 값에 와일드카드 '*'는 허용되지 않습니다.");
            }
        }
    }

    private void validateKafkaSasl(List<String> violations) {
        if (!kafkaSaslEnabled) {
            violations.add("spring.kafka.sasl.enabled (KAFKA_SASL_ENABLED) 값이 true여야 합니다. 운영 환경에서 Kafka SASL을 활성화하세요.");
            return;
        }
        String currentProtocol = FormatValidator.hasValue(kafkaSaslProtocol) ? kafkaSaslProtocol : "(미설정)";
        if (!REQUIRED_KAFKA_SASL_PROTOCOL.equals(kafkaSaslProtocol)) {
            violations.add("spring.kafka.sasl.protocol (KAFKA_SASL_PROTOCOL) 값이 " + REQUIRED_KAFKA_SASL_PROTOCOL
                    + "이어야 합니다. 현재 값: " + currentProtocol
            );
        }
        validateRequired(violations, "spring.kafka.sasl.username (KAFKA_SASL_USERNAME)", kafkaSaslUsername);
        validateRequired(violations, "spring.kafka.sasl.password (KAFKA_SASL_PASSWORD)", kafkaSaslPassword);
    }
}
