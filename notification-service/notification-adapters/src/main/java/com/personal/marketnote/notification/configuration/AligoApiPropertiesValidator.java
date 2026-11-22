package com.personal.marketnote.notification.configuration;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.configuration.exception.InvalidAligoApiPropertiesException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aligo API 메타데이터 시작 검증기.
 *
 * <p>{@code aligo.api.sensitive-paths} 설정이 비어 있으면 앱 시작을 차단(fail-closed)한다.
 * 또한 sensitive-paths에 등록된 경로가 {@link AligoProperties.Api}의 {@code *Path} 필드 값과
 * 일치하는지 reflection으로 검증하여, yml 변경 시 sensitive-paths 동기화 누락에 의한
 * silent regression(raw body 평문 노출)을 시작 단계에서 차단한다.</p>
 */
@Component
public class AligoApiPropertiesValidator {

    private static final Logger log = LoggerFactory.getLogger(AligoApiPropertiesValidator.class);

    private final AligoProperties properties;

    public AligoApiPropertiesValidator(AligoProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateProperties() {
        Set<String> sensitivePaths = properties.getApi().getSensitivePaths();
        if (FormatValidator.hasNoValue(sensitivePaths)) {
            throw new InvalidAligoApiPropertiesException(
                    "aligo.api.sensitive-paths 값이 설정되지 않았습니다. "
                            + "raw body 노출 방지를 위해 마스킹 대상 경로를 1개 이상 등록해야 합니다."
            );
        }

        Set<String> definedPaths = collectDefinedPaths(properties.getApi());
        Set<String> orphans = sensitivePaths.stream()
                .filter(path -> !definedPaths.contains(path))
                .collect(Collectors.toUnmodifiableSet());
        if (!orphans.isEmpty()) {
            throw new InvalidAligoApiPropertiesException(
                    "aligo.api.sensitive-paths에 등록된 경로가 Api 정의와 불일치합니다. orphans=" + orphans
            );
        }

        log.info("Aligo API 설정 검증 완료: sensitive-paths {} 등록.", sensitivePaths);
    }

    private Set<String> collectDefinedPaths(AligoProperties.Api api) {
        return Arrays.stream(AligoProperties.Api.class.getDeclaredFields())
                .filter(field -> field.getType() == String.class)
                .filter(field -> field.getName().endsWith("Path"))
                .map(field -> readStringField(api, field))
                .filter(FormatValidator::hasValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    private String readStringField(AligoProperties.Api api, Field field) {
        try {
            field.setAccessible(true);
            return (String) field.get(api);
        } catch (IllegalAccessException iae) {
            throw new InvalidAligoApiPropertiesException(
                    "Api 필드 reflection 접근 실패: " + field.getName(), iae
            );
        }
    }
}
