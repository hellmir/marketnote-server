package com.personal.marketnote.common.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MdcTaskDecorator")
class MdcTaskDecoratorTest {

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("부모 스레드 MDC traceId가 자식 스레드로 전파된다")
    void propagatesParentMdcToChildThread() throws InterruptedException {
        // given
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, "parent-trace");
        AtomicReference<String> capturedTraceId = new AtomicReference<>();
        Runnable wrapped = decorator.decorate(() -> capturedTraceId.set(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY)));

        // when
        Thread child = new Thread(wrapped);
        child.start();
        child.join();

        // then
        assertThat(capturedTraceId.get()).isEqualTo("parent-trace");
    }

    @Test
    @DisplayName("자식 스레드 실행 후 자식 스레드 MDC가 정리된다")
    void clearsChildMdcAfterExecution() throws InterruptedException {
        // given
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, "parent-trace");
        AtomicReference<String> mdcAfterRun = new AtomicReference<>();
        Runnable wrapped = decorator.decorate(() -> {
            // task 본문 실행 시점은 MDC가 채워져 있어야 함
        });

        // when
        Thread child = new Thread(() -> {
            wrapped.run();
            mdcAfterRun.set(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY));
        });
        child.start();
        child.join();

        // then
        assertThat(mdcAfterRun.get()).isNull();
    }

    @Test
    @DisplayName("부모 MDC가 비어있어도 자식 스레드에 신규 traceId가 생성된다")
    void generatesNewTraceIdWhenParentMdcEmpty() throws InterruptedException {
        // given
        MDC.clear();
        AtomicReference<String> capturedTraceId = new AtomicReference<>();
        Runnable wrapped = decorator.decorate(() -> capturedTraceId.set(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY)));

        // when
        Thread child = new Thread(wrapped);
        child.start();
        child.join();

        // then
        assertThat(capturedTraceId.get()).isNotNull();
        assertThat(capturedTraceId.get().length()).isEqualTo(36);
    }
}
