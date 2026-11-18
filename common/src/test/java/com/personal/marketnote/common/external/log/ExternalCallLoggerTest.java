package com.personal.marketnote.common.external.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExternalCallLogger 테스트")
class ExternalCallLoggerTest {

    private static final ExternalSystem SYSTEM = ExternalSystem.KCP;
    private static final String OPERATION = "approve";

    private MeterRegistry meterRegistry;
    private ExternalCallLogger externalCallLogger;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        externalCallLogger = new ExternalCallLogger(meterRegistry);

        logger = (Logger) LoggerFactory.getLogger(SYSTEM.loggerName());
        previousAdditive = logger.isAdditive();
        previousLevel = logger.getLevel();
        logger.setAdditive(false);
        logger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        logger.setAdditive(previousAdditive);
        logger.setLevel(previousLevel);
    }

    @Test
    @DisplayName("정상 호출 시 결과를 그대로 반환하고 success Timer를 기록한다")
    void shouldReturnResultAndRecordSuccessTimerOnNormalCall() {
        String result = externalCallLogger.record(
                SYSTEM, OPERATION, Map.of("orderKey", "ORD-1"), () -> "OK"
        );

        assertThat(result).isEqualTo("OK");
        Timer timer = meterRegistry.find("external.call")
                .tags("system", SYSTEM.metricTag(), "operation", OPERATION, "outcome", "success")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("예외 발생 시 동일 예외를 rethrow하고 failure Timer를 기록한다")
    void shouldRethrowAndRecordFailureTimerOnException() {
        IllegalStateException cause = new IllegalStateException("boom");

        assertThatThrownBy(() -> externalCallLogger.record(
                SYSTEM, OPERATION, Map.of(), () -> { throw cause; }
        )).isSameAs(cause);

        Timer timer = meterRegistry.find("external.call")
                .tags("system", SYSTEM.metricTag(), "operation", OPERATION, "outcome", "failure")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DEBUG 비활성화 시에도 Timer는 기록되고 결과는 정상 반환된다")
    void shouldRecordTimerEvenWhenDebugDisabled() {
        logger.setLevel(Level.INFO);

        String result = externalCallLogger.record(
                SYSTEM, OPERATION, Map.of(), () -> "OK"
        );

        assertThat(result).isEqualTo("OK");
        Timer timer = meterRegistry.find("external.call")
                .tags("system", SYSTEM.metricTag(), "operation", OPERATION, "outcome", "success")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(appender.list).isEmpty();
    }

    @Test
    @DisplayName("operation/tags가 로그에 포함된다")
    void shouldIncludeOperationAndTagsInLog() {
        externalCallLogger.record(
                SYSTEM, OPERATION, Map.of("orderKey", "ORD-1"), () -> "OK"
        );

        assertThat(appender.list).isNotEmpty();
        boolean hasOperation = appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains(OPERATION));
        boolean hasTagKey = appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("orderKey"));
        boolean hasTagValue = appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("ORD-1"));
        assertThat(hasOperation).isTrue();
        assertThat(hasTagKey).isTrue();
        assertThat(hasTagValue).isTrue();
    }

    @Test
    @DisplayName("예외 메시지가 ERROR 로그에 포함된다")
    void shouldIncludeExceptionMessageInErrorLog() {
        IllegalStateException cause = new IllegalStateException("boom-message");

        assertThatThrownBy(() -> externalCallLogger.record(
                SYSTEM, OPERATION, Map.of(), () -> { throw cause; }
        )).isSameAs(cause);

        boolean hasErrorWithMessage = appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("boom-message")
                        && event.getFormattedMessage().contains("IllegalStateException"));
        assertThat(hasErrorWithMessage).isTrue();
    }

    @Test
    @DisplayName("recordVoid는 결과 없이 정상 종료 시 Runnable을 실행하고 success Timer를 기록한다")
    void shouldRunRunnableAndRecordSuccessTimerOnVoidCall() {
        AtomicInteger counter = new AtomicInteger(0);

        externalCallLogger.recordVoid(
                SYSTEM, OPERATION, Map.of(), counter::incrementAndGet
        );

        assertThat(counter.get()).isEqualTo(1);
        Timer timer = meterRegistry.find("external.call")
                .tags("system", SYSTEM.metricTag(), "operation", OPERATION, "outcome", "success")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("recordVoid에서 예외 발생 시 동일 예외를 rethrow하고 failure Timer를 기록한다")
    void shouldRethrowAndRecordFailureTimerOnVoidException() {
        IllegalArgumentException cause = new IllegalArgumentException("invalid");

        assertThatThrownBy(() -> externalCallLogger.recordVoid(
                SYSTEM, OPERATION, Map.of(), () -> { throw cause; }
        )).isSameAs(cause);

        Timer timer = meterRegistry.find("external.call")
                .tags("system", SYSTEM.metricTag(), "operation", OPERATION, "outcome", "failure")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }
}
