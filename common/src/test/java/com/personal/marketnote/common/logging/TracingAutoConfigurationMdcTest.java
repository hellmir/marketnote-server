package com.personal.marketnote.common.logging;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot 3.5 + micrometer-tracing-bridge-brave 자동 구성 라운드트립 회귀 방지.
 *
 * <p>{@link TracingMdcCoexistenceTest}는 Brave 라이브러리 직접 조립 단위 테스트인 반면,
 * 본 테스트는 Spring Boot 자동 구성을 통과시켜 운영 런타임과 동일한 빈 그래프에서
 * MDC 자동 주입 동작을 검증한다.
 */
@SpringBootTest(
        classes = TracingAutoConfigurationMdcTest.AutoConfigTestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(properties = {
        "management.tracing.sampling.probability=1.0",
        "management.tracing.propagation.type=w3c",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration," +
                "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration"
})
class TracingAutoConfigurationMdcTest {

    @Autowired
    private Tracer tracer;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Spring Boot autoconfig 환경에서 Tracer 빈이 주입되고 새 span 생성 시 MDC traceId/spanId가 set된다")
    void tracerBeanInjectedAndMdcPopulatedOnSpanScope() {
        // given
        assertThat(tracer)
                .as("micrometer-tracing-bridge-brave 자동 구성으로 Tracer 빈이 주입되어야 한다")
                .isNotNull();

        // when — 새 span 생성 + scope 진입
        Span span = tracer.nextSpan().name("test-span").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            String mdcTraceId = MDC.get("traceId");
            String mdcSpanId = MDC.get("spanId");

            // then — MDC에 W3C traceId/spanId가 자동 set
            assertThat(mdcTraceId)
                    .as("Spring Boot autoconfig가 MDC traceId를 자동 set한다")
                    .isNotNull()
                    .isNotBlank()
                    .matches("^[0-9a-f]+$");
            assertThat(mdcTraceId.length())
                    .as("W3C traceId는 16자 또는 32자 hex이다")
                    .isIn(16, 32);
            assertThat(mdcSpanId)
                    .as("Spring Boot autoconfig가 MDC spanId를 자동 set한다")
                    .isNotNull()
                    .isNotBlank()
                    .hasSize(16)
                    .matches("^[0-9a-f]+$");
        } finally {
            span.end();
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class AutoConfigTestApp {
    }
}
