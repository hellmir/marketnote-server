package com.personal.marketnote.common.configuration.security;

import com.personal.marketnote.common.configuration.security.exception.SecurityConfigurationValidationException;
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
            "dev-secret-change-me", "abc", "def", "ghi",
            "change-me", "password", "root", "secret", "test"
    );

    @Value("${spring.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.jwt.admin-access-token:}")
    private String adminAccessToken;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.kafka.sasl.enabled:false}")
    private boolean kafkaSaslEnabled;

    @Value("${spring.kafka.sasl.username:}")
    private String kafkaSaslUsername;

    @Value("${spring.kafka.sasl.password:}")
    private String kafkaSaslPassword;

    @PostConstruct
    public void validateSecurityProperties() {
        List<String> violations = new ArrayList<>();

        validateRequired(violations, "spring.jwt.secret (JWT_SECRET_KEY)", jwtSecret);
        validateRequired(violations, "spring.jwt.admin-access-token (JWT_ADMIN_ACCESS_TOKEN)", adminAccessToken);
        validateRequired(violations, "spring.datasource.password (DB_PASSWORD)", dbPassword);

        if (kafkaSaslEnabled) {
            validateRequired(violations, "spring.kafka.sasl.username (KAFKA_SASL_USERNAME)", kafkaSaslUsername);
            validateRequired(violations, "spring.kafka.sasl.password (KAFKA_SASL_PASSWORD)", kafkaSaslPassword);
        }

        if (!violations.isEmpty()) {
            String message = String.join("\n  - ", violations);
            throw new SecurityConfigurationValidationException(message);
        }

        log.info("보안 설정 검증 완료: 필수 시크릿이 올바르게 설정되었습니다.");
    }

    private void validateRequired(List<String> violations, String propertyName, String value) {
        if (value == null || value.isBlank()) {
            violations.add(propertyName + " 값이 설정되지 않았습니다.");
        } else if (WEAK_DEFAULTS.contains(value.toLowerCase())) {
            violations.add(propertyName + " 값이 기본 플레이스홀더입니다. 강력한 값으로 변경하세요.");
        }
    }
}
