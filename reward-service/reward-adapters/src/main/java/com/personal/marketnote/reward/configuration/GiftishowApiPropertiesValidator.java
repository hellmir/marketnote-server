package com.personal.marketnote.reward.configuration;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.configuration.exception.InvalidGiftishowApiPropertiesException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Giftishow API 메타데이터 시작 검증기.
 *
 * <p>{@code vendor.giftishow.api.sensitive-paths} 설정이 비어 있으면 앱 시작을 차단(fail-closed)한다.
 * 향후 PIN 관련 신규 엔드포인트가 {@link GiftishowApiProperties.Api}에 추가될 때
 * sensitivePaths 등록 누락을 구성 단계에서 감지하여 응답 본문(PIN 평문) 노출 silent regression을 방지한다.</p>
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
        log.info("Giftishow API 설정 검증 완료: sensitive-paths {} 등록.", sensitivePaths);
    }
}
