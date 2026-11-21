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
    @DisplayName("sensitivePaths 모두가 Api 필드의 path 값과 매칭되면 검증을 통과한다")
    void shouldPassWhenSensitivePathsAllMatchDefinedPaths() {
        GiftishowApiProperties properties = newPropertiesWithApiPathsAndSensitivePaths(
                Set.of("/bizApi/coupon", "/bizApi/coupon/detail", "/bizApi/coupon/resend")
        );
        GiftishowApiPropertiesValidator validator = new GiftishowApiPropertiesValidator(properties);

        assertThatCode(validator::validateProperties)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sensitivePaths에 Api 필드와 매칭되지 않는 orphan path가 포함되면 앱 시작을 차단한다")
    void shouldFailWhenSensitivePathsContainOrphan() {
        GiftishowApiProperties properties = newPropertiesWithApiPathsAndSensitivePaths(
                Set.of("/bizApi/coupon", "/bizApi/coupon/orphan")
        );
        GiftishowApiPropertiesValidator validator = new GiftishowApiPropertiesValidator(properties);

        assertThatThrownBy(validator::validateProperties)
                .isInstanceOf(InvalidGiftishowApiPropertiesException.class)
                .hasMessageContaining("orphans=")
                .hasMessageContaining("/bizApi/coupon/orphan");
    }

    @Test
    @DisplayName("Api의 일부 path 필드가 null이어도 매칭되는 sensitivePaths만 등록되면 검증을 통과한다")
    void shouldPassWhenSomeApiPathFieldsAreNullButSensitivePathsMatchDefined() {
        GiftishowApiProperties properties = new GiftishowApiProperties();
        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setCouponSendPath("/bizApi/coupon");
        api.setCouponDetailPath("/bizApi/coupon/detail");
        api.setSensitivePaths(Set.of("/bizApi/coupon", "/bizApi/coupon/detail"));
        properties.setApi(api);

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

    private GiftishowApiProperties newPropertiesWithApiPathsAndSensitivePaths(Set<String> sensitivePaths) {
        GiftishowApiProperties properties = new GiftishowApiProperties();
        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setProductListPath("/bizApi/goods");
        api.setProductDetailPath("/bizApi/goods");
        api.setBrandListPath("/bizApi/brand");
        api.setBrandDetailPath("/bizApi/brand");
        api.setCouponSendPath("/bizApi/coupon");
        api.setCouponDetailPath("/bizApi/coupon/detail");
        api.setCouponCancelPath("/bizApi/coupon/cancel");
        api.setCouponResendPath("/bizApi/coupon/resend");
        api.setBizMoneyBalancePath("/bizApi/bizMoney");
        api.setCouponSendFailCancelPath("/bizApi/coupon/sendFailCancel");
        api.setSensitivePaths(sensitivePaths);
        properties.setApi(api);
        return properties;
    }
}
