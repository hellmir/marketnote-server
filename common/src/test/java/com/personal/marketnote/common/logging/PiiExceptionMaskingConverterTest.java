package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiExceptionMaskingConverterTest {

    @Test
    @DisplayName("Exception 메시지에 email 토큰이 포함되면 마스킹된다")
    void masksEmailInExceptionMessage() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable throwable = new RuntimeException("user email=test@example.com failed");
        IThrowableProxy proxy = new ThrowableProxy(throwable);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).contains("email=t***@example.com");
        assertThat(result).doesNotContain("test@example.com");
    }

    @Test
    @DisplayName("Exception 메시지에 phone 토큰이 포함되면 마스킹된다")
    void masksPhoneInExceptionMessage() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable throwable = new RuntimeException("phone=010-1234-5678 not valid");
        IThrowableProxy proxy = new ThrowableProxy(throwable);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).contains("phone=010-****-5678");
        assertThat(result).doesNotContain("010-1234-5678");
    }

    @Test
    @DisplayName("Cause chain의 메시지도 마스킹된다")
    void masksMessageInCauseChain() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable cause = new IllegalStateException("phone=010-1234-5678");
        Throwable throwable = new RuntimeException("top level failure", cause);
        IThrowableProxy proxy = new ThrowableProxy(throwable);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).contains("phone=010-****-5678");
        assertThat(result).doesNotContain("010-1234-5678");
    }

    @Test
    @DisplayName("nickname 토큰이 포함된 Exception 메시지도 마스킹된다")
    void masksNicknameInExceptionMessage() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable throwable = new RuntimeException("nickname=홍길동 처리 실패");
        IThrowableProxy proxy = new ThrowableProxy(throwable);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).contains("nickname=홍**");
        assertThat(result).doesNotContain("홍길동");
    }

    @Test
    @DisplayName("Exception 메시지가 null이면 예외 없이 처리된다")
    void handlesNullMessage() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable throwable = new RuntimeException();
        IThrowableProxy proxy = new ThrowableProxy(throwable);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).isNotNull();
        assertThat(result).contains("RuntimeException");
    }

    @Test
    @DisplayName("PII가 없는 Exception 메시지는 변형되지 않는다")
    void preservesMessageWithoutPii() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable throwable = new RuntimeException("order processing failed orderId=123");
        IThrowableProxy proxy = new ThrowableProxy(throwable);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).contains("order processing failed orderId=123");
    }

    private PiiExceptionMaskingConverter newStartedConverter() {
        PiiExceptionMaskingConverter converter = new PiiExceptionMaskingConverter();
        converter.start();
        return converter;
    }
}
