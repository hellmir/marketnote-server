package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("AsyncSchedulerLoggingAspect 테스트")
class AsyncSchedulerLoggingAspectTest {

    private final AsyncSchedulerLoggingAspect aspect = new AsyncSchedulerLoggingAspect();

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(AsyncSchedulerLoggingAspect.class);
        previousAdditive = logger.isAdditive();
        previousLevel = logger.getLevel();
        logger.setAdditive(false);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        appender.stop();
        logger.setAdditive(previousAdditive);
        logger.setLevel(previousLevel);
    }

    private ProceedingJoinPoint mockJoinPoint(String shortString) {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn(shortString);
        when(pjp.getSignature()).thenReturn(signature);
        return pjp;
    }

    @Test
    @DisplayName("DEBUG 비활성 시 proceed만 호출하고 로그를 남기지 않는다")
    void shouldNotLogWhenDebugDisabled() throws Throwable {
        logger.setLevel(Level.INFO);
        ProceedingJoinPoint pjp = mockJoinPoint("Foo.bar()");
        when(pjp.proceed()).thenReturn("ok");

        Object result = aspect.logAsyncOrScheduled(pjp);

        assertThat(result).isEqualTo("ok");
        verify(pjp, times(1)).proceed();
        assertThat(appender.list).isEmpty();
    }

    @Test
    @DisplayName("DEBUG 활성 + 정상 종료 시 Enter/Exit DEBUG 로그를 남기고 결과를 반환한다")
    void shouldLogEnterAndExitWhenDebugEnabledAndNoException() throws Throwable {
        logger.setLevel(Level.DEBUG);
        ProceedingJoinPoint pjp = mockJoinPoint("Foo.bar()");
        when(pjp.proceed()).thenReturn("ok");

        Object result = aspect.logAsyncOrScheduled(pjp);

        assertThat(result).isEqualTo("ok");
        verify(pjp, times(1)).proceed();
        assertThat(appender.list).hasSize(2);
        assertThat(appender.list).allSatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
            assertThat(event.getThrowableProxy()).isNull();
        });
        assertThat(appender.list.get(0).getFormattedMessage()).isEqualTo("Enter: Foo.bar()");
        assertThat(appender.list.get(1).getFormattedMessage()).startsWith("Exit: Foo.bar() (took ");
        assertThat(appender.list.get(1).getFormattedMessage()).endsWith(" ms)");
    }

    @Test
    @DisplayName("DEBUG 활성 + 예외 발생 시 Enter/Exception DEBUG 로그를 남기고 예외를 그대로 재전파한다")
    void shouldLogEnterAndExceptionWhenDebugEnabledAndProceedThrows() throws Throwable {
        logger.setLevel(Level.DEBUG);
        ProceedingJoinPoint pjp = mockJoinPoint("Foo.bar()");
        IllegalStateException cause = new IllegalStateException("boom");
        when(pjp.proceed()).thenThrow(cause);

        assertThatThrownBy(() -> aspect.logAsyncOrScheduled(pjp))
            .isSameAs(cause);

        verify(pjp, times(1)).proceed();
        assertThat(appender.list).hasSize(2);
        assertThat(appender.list).allSatisfy(event -> assertThat(event.getLevel()).isEqualTo(Level.DEBUG));
        assertThat(appender.list.get(0).getFormattedMessage()).isEqualTo("Enter: Foo.bar()");
        assertThat(appender.list.get(1).getFormattedMessage()).startsWith("Exception in Foo.bar() (took ");
        assertThat(appender.list.get(1).getFormattedMessage()).contains("cause=IllegalStateException");
        assertThat(appender.list.get(1).getThrowableProxy()).isNotNull();
        assertThat(appender.list.get(1).getThrowableProxy().getClassName())
            .isEqualTo(IllegalStateException.class.getName());
    }
}
