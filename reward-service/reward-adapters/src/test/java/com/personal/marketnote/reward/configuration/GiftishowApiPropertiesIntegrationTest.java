package com.personal.marketnote.reward.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Giftishow API 설정의 yml ↔ Validator 정합성 통합 테스트.
 *
 * <p>application.yml과 동일 구조의 path 정의 + sensitive-paths 등록값으로 Spring context를 로딩하여
 * {@link GiftishowApiPropertiesValidator}의 {@code @PostConstruct} 검증 로직이 통과하는지 확인한다.
 * Spring context 로딩 자체가 Validator를 트리거하므로, 로딩 성공 = sensitive-paths가 Api의 {@code *Path}
 * 필드 값과 정합함을 의미한다.</p>
 */
@SpringBootTest(
        classes = {
                GiftishowApiPropertiesIntegrationTest.TestConfig.class
        },
        properties = {
                "vendor.giftishow.api.product-list-path=/bizApi/goods",
                "vendor.giftishow.api.product-detail-path=/bizApi/goods",
                "vendor.giftishow.api.brand-list-path=/bizApi/brand",
                "vendor.giftishow.api.brand-detail-path=/bizApi/brand",
                "vendor.giftishow.api.coupon-send-path=/bizApi/coupon",
                "vendor.giftishow.api.coupon-detail-path=/bizApi/coupon/detail",
                "vendor.giftishow.api.coupon-cancel-path=/bizApi/coupon/cancel",
                "vendor.giftishow.api.coupon-resend-path=/bizApi/coupon/resend",
                "vendor.giftishow.api.biz-money-balance-path=/bizApi/bizMoney",
                "vendor.giftishow.api.coupon-send-fail-cancel-path=/bizApi/coupon/sendFailCancel",
                "vendor.giftishow.api.sensitive-paths=/bizApi/coupon,/bizApi/coupon/detail,/bizApi/coupon/resend"
        }
)
@DisplayName("GiftishowApiProperties yml ↔ Validator 정합성 통합 테스트")
class GiftishowApiPropertiesIntegrationTest {

    @Autowired
    private GiftishowApiProperties properties;

    @Test
    @DisplayName("application.yml과 동일한 정합 설정으로 Spring context 로딩 시 Validator 통과")
    void shouldLoadContextWhenSensitivePathsConsistentWithDefinedPaths() {
        assertThat(properties.getApi().getSensitivePaths())
                .containsExactlyInAnyOrder(
                        "/bizApi/coupon",
                        "/bizApi/coupon/detail",
                        "/bizApi/coupon/resend"
                );
    }

    @Configuration
    @EnableConfigurationProperties(GiftishowApiProperties.class)
    @Import(GiftishowApiPropertiesValidator.class)
    static class TestConfig {
    }
}
