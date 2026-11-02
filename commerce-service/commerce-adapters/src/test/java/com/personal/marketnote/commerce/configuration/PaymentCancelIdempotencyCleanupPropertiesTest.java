package com.personal.marketnote.commerce.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentCancelIdempotencyCleanupProperties 바인딩 테스트")
class PaymentCancelIdempotencyCleanupPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    @DisplayName("프로퍼티가 설정되지 않으면 기본값(ttlHours=24, cron=0 0 3 * * *)이 바인딩된다")
    void shouldBindDefaultValuesWhenNoPropertySet() {
        contextRunner.run(context -> {
            PaymentCancelIdempotencyCleanupProperties properties =
                    context.getBean(PaymentCancelIdempotencyCleanupProperties.class);

            assertThat(properties.getTtlHours()).isEqualTo(24L);
            assertThat(properties.getCron()).isEqualTo("0 0 3 * * *");
        });
    }

    @Test
    @DisplayName("프로퍼티가 설정되면 해당 값으로 바인딩된다")
    void shouldBindCustomValuesWhenPropertiesSet() {
        contextRunner
                .withPropertyValues(
                        "commerce.payment.idempotency-cleanup.ttl-hours=12",
                        "commerce.payment.idempotency-cleanup.cron=0 0 2 * * *"
                )
                .run(context -> {
                    PaymentCancelIdempotencyCleanupProperties properties =
                            context.getBean(PaymentCancelIdempotencyCleanupProperties.class);

                    assertThat(properties.getTtlHours()).isEqualTo(12L);
                    assertThat(properties.getCron()).isEqualTo("0 0 2 * * *");
                });
    }

    @Configuration
    @EnableConfigurationProperties(PaymentCancelIdempotencyCleanupProperties.class)
    static class TestConfig {
    }
}
