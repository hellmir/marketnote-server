package com.personal.marketnote.common.logging;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
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
 * Spring Boot 3.5 + micrometer-tracing-bridge-otel 자동 구성 라운드트립 회귀 방지.
 *
 * <p>본 테스트는 Spring Boot 자동 구성을 통과시켜 운영 런타임과 동일한 빈 그래프에서
 * MDC 자동 주입 동작 + 자체 발급 requestId 공존(D1-B 옵션) 라운드트립을 검증한다.
 *
 * <p>OTLP exporter는 endpoint 환경변수 미설정 시 no-op 자동 구성되어 본 테스트 실행에 영향 없음.
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
        assertThat(tracer)
            .as("micrometer-tracing-bridge-otel 자동 구성으로 Tracer 빈이 주입되어야 한다")
            .isNotNull();

        Span span = tracer.nextSpan().name("test-span").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            String mdcTraceId = MDC.get("traceId");
            String mdcSpanId = MDC.get("spanId");

            assertThat(mdcTraceId)
                .as("Spring Boot autoconfig가 MDC traceId를 자동 set한다")
                .isNotNull()
                .isNotBlank()
                .matches("^[0-9a-f]+$");
            assertThat(mdcTraceId.length())
                .as("W3C traceId는 32자 hex이다 (OTel 128-bit)")
                .isEqualTo(32);
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

    @Test
    @DisplayName("Sampling probability=1.0 환경에서 모든 span은 spanId가 비어있지 않다")
    void allSpansHaveNonEmptySpanIdWhenSamplingIsAlways() {
        for (int i = 0; i < 10; i++) {
            Span span = tracer.nextSpan().name("iter-" + i).start();
            try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
                assertThat(MDC.get("spanId"))
                    .as("iteration %d", i)
                    .isNotNull()
                    .isNotBlank();
            } finally {
                span.end();
            }
        }
    }

    @Test
    @DisplayName("Tracing 활성화되어도 자체 발급 requestId MDC 키는 보존된다 (D1-B 핵심 라운드트립)")
    void selfIssuedRequestIdCoexistsWithTracingTraceId() {
        String selfIssuedRequestId = "client-request-id-12345";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, selfIssuedRequestId);

        Span span = tracer.nextSpan().name("coexistence-span").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY))
                .as("자체 발급 requestId는 Tracing 활성화 후에도 보존된다")
                .isEqualTo(selfIssuedRequestId);
            assertThat(MDC.get("traceId"))
                .as("W3C traceId는 자체 발급 requestId와 다른 키로 set된다")
                .isNotNull()
                .isNotEqualTo(selfIssuedRequestId);
            assertThat(MDC.get("spanId"))
                .as("W3C spanId는 정상 set된다")
                .isNotNull();
        } finally {
            span.end();
        }
    }

    @Test
    @DisplayName("Tracing span scope가 종료되면 MDC traceId/spanId가 제거되지만 requestId는 보존된다")
    void requestIdRemainsAfterSpanCloses() {
        String selfIssuedRequestId = "request-id-survives";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, selfIssuedRequestId);

        Span span = tracer.nextSpan().name("scoped-span").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            assertThat(MDC.get("traceId")).isNotNull();
        } finally {
            span.end();
        }

        assertThat(MDC.get("traceId"))
            .as("span scope 종료 후 W3C traceId는 제거된다")
            .isNull();
        assertThat(MDC.get("spanId"))
            .as("span scope 종료 후 W3C spanId는 제거된다")
            .isNull();
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY))
            .as("자체 발급 requestId는 Tracing scope와 무관하게 보존된다")
            .isEqualTo(selfIssuedRequestId);
    }

    @Test
    @DisplayName("동일 trace 내 child span 생성 시 traceId는 동일하고 spanId는 새로 발급된다")
    void childSpanInheritsTraceIdAndGetsNewSpanId() {
        Span parent = tracer.nextSpan().name("parent").start();
        String parentTraceId;
        String parentSpanId;
        try (Tracer.SpanInScope ignored = tracer.withSpan(parent)) {
            parentTraceId = MDC.get("traceId");
            parentSpanId = MDC.get("spanId");

            TraceContext parentContext = parent.context();
            Span child = tracer.spanBuilder().setParent(parentContext).name("child").start();
            try (Tracer.SpanInScope ignored2 = tracer.withSpan(child)) {
                assertThat(MDC.get("traceId"))
                    .as("child span은 parent의 traceId를 상속한다")
                    .isEqualTo(parentTraceId);
                assertThat(MDC.get("spanId"))
                    .as("child span은 새 spanId를 발급한다")
                    .isNotNull()
                    .isNotEqualTo(parentSpanId);
            } finally {
                child.end();
            }
        } finally {
            parent.end();
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class AutoConfigTestApp {
    }
}
