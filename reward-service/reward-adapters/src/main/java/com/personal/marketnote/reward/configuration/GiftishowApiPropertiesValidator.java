package com.personal.marketnote.reward.configuration;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.configuration.exception.InvalidGiftishowApiPropertiesException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Giftishow API 메타데이터 시작 검증기.
 *
 * <p>{@code vendor.giftishow.api.sensitive-paths} 설정이 비어 있으면 앱 시작을 차단(fail-closed)한다.
 * 또한 sensitive-paths에 등록된 경로가 {@link GiftishowApiProperties.Api}의 {@code *Path} 필드 값과
 * 일치하는지 reflection으로 검증하여, yml 변경(예: coupon-send-path 변경) 시 sensitive-paths 동기화
 * 누락에 의한 silent regression(PIN/raw body 평문 노출)을 시작 단계에서 차단한다.</p>
 */
@Component
public class GiftishowApiPropertiesValidator {

    private static final Logger log = LoggerFactory.getLogger(GiftishowApiPropertiesValidator.class);

    private final GiftishowApiProperties properties;

    public GiftishowApiPropertiesValidator(GiftishowApiProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateProperties() {
        Set<String> sensitivePaths = properties.getApi().getSensitivePaths();
        if (FormatValidator.hasNoValue(sensitivePaths)) {
            throw new InvalidGiftishowApiPropertiesException(
                "vendor.giftishow.api.sensitive-paths 값이 설정되지 않았습니다. "
                    + "PIN/raw body 노출 방지를 위해 마스킹 대상 경로를 1개 이상 등록해야 합니다."
            );
        }

        Set<String> definedPaths = collectDefinedPaths(properties.getApi());
        Set<String> orphans = sensitivePaths.stream()
            .filter(path -> !definedPaths.contains(path))
            .collect(Collectors.toUnmodifiableSet());
        if (!orphans.isEmpty()) {
            throw new InvalidGiftishowApiPropertiesException(
                "vendor.giftishow.api.sensitive-paths에 등록된 경로가 Api 정의와 불일치합니다. orphans=" + orphans
            );
        }

        log.info("Giftishow API 설정 검증 완료: sensitive-paths {} 등록.", sensitivePaths);
    }

    private Set<String> collectDefinedPaths(GiftishowApiProperties.Api api) {
        return Arrays.stream(GiftishowApiProperties.Api.class.getDeclaredFields())
            .filter(field -> field.getType() == String.class)
            .filter(field -> field.getName().endsWith("Path"))
            .map(field -> readStringField(api, field))
            .filter(FormatValidator::hasValue)
            .collect(Collectors.toUnmodifiableSet());
    }

    private String readStringField(GiftishowApiProperties.Api api, Field field) {
        try {
            field.setAccessible(true);
            return (String) field.get(api);
        } catch (IllegalAccessException iae) {
            throw new InvalidGiftishowApiPropertiesException(
                "Api 필드 reflection 접근 실패: " + field.getName(), iae
            );
        }
    }
}
