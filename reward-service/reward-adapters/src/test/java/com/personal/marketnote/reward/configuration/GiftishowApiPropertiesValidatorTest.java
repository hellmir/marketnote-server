package com.personal.marketnote.reward.configuration;

import com.personal.marketnote.reward.configuration.exception.InvalidGiftishowApiPropertiesException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GiftishowApiPropertiesValidator 시작 검증 테스트")
class GiftishowApiPropertiesValidatorTest {

    @Test
    @DisplayName("sensitivePaths가 비어 있으면 앱 시작을 차단한다")
    void shouldFailWhenSensitivePathsIsEmpty() {
        GiftishowApiProperties properties = newPropertiesWithSensitivePaths(Set.of());
        GiftishowApiPropertiesValidator validator = new GiftishowApiPropertiesValidator(properties);

        assertThatThrownBy(validator::validateProperties)
                .isInstanceOf(InvalidGiftishowApiPropertiesException.class)
                .hasMessageContaining("vendor.giftishow.api.sensitive-paths");
    }

    @Test
    @DisplayName("sensitivePaths가 null이면 앱 시작을 차단한다")
    void shouldFailWhenSensitivePathsIsNull() {
        GiftishowApiProperties properties = newPropertiesWithSensitivePaths(null);
        GiftishowApiPropertiesValidator validator = new GiftishowApiPropertiesValidator(properties);

        assertThatThrownBy(validator::validateProperties)
                .isInstanceOf(InvalidGiftishowApiPropertiesException.class)
                .hasMessageContaining("vendor.giftishow.api.sensitive-paths");
    }

    @Test
    @DisplayName("sensitivePaths에 1개 이상의 path가 등록되어 있으면 검증을 통과한다")
    void shouldPassWhenSensitivePathsIsRegistered() {
        GiftishowApiProperties properties = newPropertiesWithSensitivePaths(
                Set.of("/bizApi/coupon", "/bizApi/coupon/detail", "/bizApi/coupon/resend")
        );
        GiftishowApiPropertiesValidator validator = new GiftishowApiPropertiesValidator(properties);

        assertThatCode(validator::validateProperties)
                .doesNotThrowAnyException();
    }

    private GiftishowApiProperties newPropertiesWithSensitivePaths(Set<String> sensitivePaths) {
        GiftishowApiProperties properties = new GiftishowApiProperties();
        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setSensitivePaths(sensitivePaths);
        properties.setApi(api);
        return properties;
    }
}
