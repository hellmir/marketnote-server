package com.personal.marketnote.common.logging;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.handler.SpanHandler;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tracing MDC + 자체 발급 requestId 공존 (D1-B 옵션 검증)")
class TracingMdcCoexistenceTest {

    private Tracing tracing;

    @BeforeEach
    void setUp() {
        MDC.clear();
        CurrentTraceContext currentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(MDCScopeDecorator.get())
                .build();
        tracing = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .sampler(Sampler.ALWAYS_SAMPLE)
                .addSpanHandler(SpanHandler.NOOP)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (tracing != null) {
            tracing.close();
        }
        MDC.clear();
    }

    @Test
    @DisplayName("Brave Tracing 활성화 시 새 span 생성과 함께 MDC traceId/spanId(W3C 16자 hex)가 set된다")
    void tracingPutsW3cTraceIdAndSpanIdToMdc() {
        // given
        brave.Span span = tracing.tracer().newTrace().start();

        try (brave.Tracer.SpanInScope ignored = tracing.tracer().withSpanInScope(span)) {
            // when
            String mdcTraceId = MDC.get("traceId");
            String mdcSpanId = MDC.get("spanId");

            // then
            assertThat(mdcTraceId)
                    .as("MDC traceId는 Tracing이 자동 set한다")
                    .isNotNull()
                    .hasSize(16)
                    .matches("^[0-9a-f]+$");
            assertThat(mdcSpanId)
                    .as("MDC spanId는 Tracing이 자동 set한다")
                    .isNotNull()
                    .hasSize(16)
                    .matches("^[0-9a-f]+$");
        } finally {
            span.finish();
        }
    }

    @Test
    @DisplayName("Sampling probability=1.0 (ALWAYS_SAMPLE) 환경에서 모든 span은 spanId가 비어있지 않다")
    void allSpansHaveNonEmptySpanIdWhenSamplingIsAlways() {
        // given / when
        for (int i = 0; i < 10; i++) {
            brave.Span span = tracing.tracer().newTrace().start();
            try (brave.Tracer.SpanInScope ignored = tracing.tracer().withSpanInScope(span)) {
                // then
                assertThat(MDC.get("spanId"))
                        .as("iteration %d", i)
                        .isNotNull()
                        .isNotBlank();
            } finally {
                span.finish();
            }
        }
    }

    @Test
    @DisplayName("Tracing 활성화되어도 자체 발급 requestId MDC 키는 보존된다 (D1-B 핵심 라운드트립)")
    void selfIssuedRequestIdCoexistsWithTracingTraceId() {
        // given — TraceIdMdcFilter가 MDC.put(requestId, ...) 한 상태
        String selfIssuedRequestId = "client-request-id-12345";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, selfIssuedRequestId);

        // when — Tracing이 새 span을 시작 (MDC.put(traceId, ...) MDC.put(spanId, ...))
        brave.Span span = tracing.tracer().newTrace().start();
        try (brave.Tracer.SpanInScope ignored = tracing.tracer().withSpanInScope(span)) {
            // then — 세 키가 모두 공존
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
            span.finish();
        }
    }

    @Test
    @DisplayName("Tracing span이 종료되면 MDC traceId/spanId가 제거되지만 requestId는 보존된다")
    void requestIdRemainsAfterSpanCloses() {
        // given
        String selfIssuedRequestId = "request-id-survives";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, selfIssuedRequestId);

        // when — span scope 진입 후 종료
        brave.Span span = tracing.tracer().newTrace().start();
        try (brave.Tracer.SpanInScope ignored = tracing.tracer().withSpanInScope(span)) {
            assertThat(MDC.get("traceId")).isNotNull();
        } finally {
            span.finish();
        }

        // then — span 종료 후 traceId/spanId는 제거, requestId는 보존
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
        // given
        brave.Span parent = tracing.tracer().newTrace().start();
        String parentTraceId;
        String parentSpanId;
        try (brave.Tracer.SpanInScope ignored = tracing.tracer().withSpanInScope(parent)) {
            parentTraceId = MDC.get("traceId");
            parentSpanId = MDC.get("spanId");

            // when — child span 생성
            brave.Span child = tracing.tracer().newChild(parent.context()).start();
            try (brave.Tracer.SpanInScope ignored2 = tracing.tracer().withSpanInScope(child)) {
                // then
                assertThat(MDC.get("traceId"))
                        .as("child span은 parent의 traceId를 상속한다")
                        .isEqualTo(parentTraceId);
                assertThat(MDC.get("spanId"))
                        .as("child span은 새 spanId를 발급한다")
                        .isNotNull()
                        .isNotEqualTo(parentSpanId);
            } finally {
                child.finish();
            }
        } finally {
            parent.finish();
        }
    }
}
