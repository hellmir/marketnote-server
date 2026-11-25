package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskingThrowableConverterTest {

    @Test
    @DisplayName("exception message에 email PII가 포함되면 마스킹된 stack trace 문자열을 반환한다")
    void exceptionMessageEmailMasked() {
        MaskingThrowableConverter converter = new MaskingThrowableConverter();
        converter.start();
        try {
            String rendered = converter.convert(eventWith(new RuntimeException("contact email=foo@bar.com")));

            assertThat(rendered).contains("f***@bar.com");
            assertThat(rendered).doesNotContain("foo@bar.com");
        } finally {
            converter.stop();
        }
    }

    @Test
    @DisplayName("exception cause chain의 PII도 마스킹된다")
    void causeChainMasked() {
        MaskingThrowableConverter converter = new MaskingThrowableConverter();
        converter.start();
        try {
            RuntimeException cause = new RuntimeException("nickname=홍길동 not found");
            RuntimeException top = new RuntimeException("validation failed", cause);

            String rendered = converter.convert(eventWith(top));

            assertThat(rendered).contains("홍**");
            assertThat(rendered).doesNotContain("홍길동");
        } finally {
            converter.stop();
        }
    }

    @Test
    @DisplayName("PII가 없는 일반 exception은 그대로 stack trace를 반환한다")
    void plainExceptionPassthrough() {
        MaskingThrowableConverter converter = new MaskingThrowableConverter();
        converter.start();
        try {
            String rendered = converter.convert(eventWith(new IllegalStateException("invalid state orderId=ORD-1")));

            assertThat(rendered).contains("invalid state orderId=ORD-1");
            assertThat(rendered).contains("IllegalStateException");
        } finally {
            converter.stop();
        }
    }

    private ILoggingEvent eventWith(Throwable throwable) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getThrowableProxy()).thenReturn(new ThrowableProxy(throwable));
        return event;
    }
}
