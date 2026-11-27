package com.personal.marketnote.fulfillment.configuration;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.exception.InvalidFulfillmentAuthPropertiesException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fulfillment Auth 메타데이터 시작 검증기.
 *
 * <p>{@code vendor.fassto.auth.sensitive-paths} 설정이 비어 있으면 앱 시작을 차단(fail-closed)한다.
 * 또한 sensitive-paths에 등록된 경로가 {@link FulfillmentAuthProperties}의 {@code *Path} 필드 값과
 * 일치하는지 reflection으로 검증하여, yml 변경 시 sensitive-paths 동기화 누락에 의한
 * silent regression(raw body 평문 노출)을 시작 단계에서 차단한다.</p>
 */
@Component
public class FulfillmentAuthPropertiesValidator {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentAuthPropertiesValidator.class);

    private final FulfillmentAuthProperties properties;

    public FulfillmentAuthPropertiesValidator(FulfillmentAuthProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateProperties() {
        Set<String> sensitivePaths = properties.getSensitivePaths();
        if (FormatValidator.hasNoValue(sensitivePaths)) {
            throw new InvalidFulfillmentAuthPropertiesException(
                "vendor.fassto.auth.sensitive-paths 값이 설정되지 않았습니다. "
                    + "raw body 노출 방지를 위해 마스킹 대상 경로를 1개 이상 등록해야 합니다."
            );
        }

        Set<String> definedPaths = collectDefinedPaths(properties);
        Set<String> orphans = sensitivePaths.stream()
            .filter(path -> !definedPaths.contains(path))
            .collect(Collectors.toUnmodifiableSet());
        if (!orphans.isEmpty()) {
            throw new InvalidFulfillmentAuthPropertiesException(
                "vendor.fassto.auth.sensitive-paths에 등록된 경로가 Path 정의와 불일치합니다. orphans=" + orphans
            );
        }

        log.info("Fulfillment Auth 설정 검증 완료: sensitive-paths {} 등록.", sensitivePaths);
    }

    private Set<String> collectDefinedPaths(FulfillmentAuthProperties properties) {
        return Arrays.stream(FulfillmentAuthProperties.class.getDeclaredFields())
            .filter(field -> field.getType() == String.class)
            .filter(field -> field.getName().endsWith("Path"))
            .map(field -> readStringField(properties, field))
            .filter(FormatValidator::hasValue)
            .collect(Collectors.toUnmodifiableSet());
    }

    private String readStringField(FulfillmentAuthProperties properties, Field field) {
        try {
            field.setAccessible(true);
            return (String) field.get(properties);
        } catch (IllegalAccessException iae) {
            throw new InvalidFulfillmentAuthPropertiesException(
                "FulfillmentAuthProperties 필드 reflection 접근 실패: " + field.getName(), iae
            );
        }
    }
}
