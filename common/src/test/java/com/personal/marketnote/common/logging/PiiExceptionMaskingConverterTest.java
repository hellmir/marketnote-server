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

    @Test
    @DisplayName("cause chain 5단계까지 재귀 마스킹한다")
    void masksAcrossFiveLevelCauseChain() {
        PiiExceptionMaskingConverter converter = newStartedConverter();

        Throwable level5 = new RuntimeException("level5 email=five@example.com");
        Throwable level4 = new RuntimeException("level4 email=four@example.com", level5);
        Throwable level3 = new RuntimeException("level3 email=three@example.com", level4);
        Throwable level2 = new RuntimeException("level2 email=two@example.com", level3);
        Throwable level1 = new RuntimeException("level1 email=one@example.com", level2);
        IThrowableProxy proxy = new ThrowableProxy(level1);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).contains("email=o***@example.com");
        assertThat(result).contains("email=t***@example.com");
        assertThat(result).contains("email=f***@example.com");
        assertThat(result).doesNotContain("one@example.com");
        assertThat(result).doesNotContain("two@example.com");
        assertThat(result).doesNotContain("three@example.com");
        assertThat(result).doesNotContain("four@example.com");
        assertThat(result).doesNotContain("five@example.com");
    }

    @Test
    @DisplayName("suppressed exception 메시지도 마스킹된다")
    void masksSuppressedExceptionMessage() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable parent = new RuntimeException("parent failure");
        parent.addSuppressed(new IllegalStateException("suppressed email=leak@example.com"));
        IThrowableProxy proxy = new ThrowableProxy(parent);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).contains("email=l***@example.com");
        assertThat(result).doesNotContain("leak@example.com");
    }

    @Test
    @DisplayName("여러 suppressed 예외 및 중첩된 suppressed cause 메시지도 모두 마스킹된다")
    void masksMultipleAndNestedSuppressed() {
        PiiExceptionMaskingConverter converter = newStartedConverter();
        Throwable parent = new RuntimeException("parent failure");
        Throwable suppressed1 = new IllegalStateException("suppressed1 email=a@example.com");
        Throwable nestedCause = new IllegalStateException("nested phone=010-1234-5678");
        Throwable suppressed2 = new IllegalStateException("suppressed2 nickname=홍길동", nestedCause);
        parent.addSuppressed(suppressed1);
        parent.addSuppressed(suppressed2);
        IThrowableProxy proxy = new ThrowableProxy(parent);

        String result = converter.throwableProxyToString(proxy);

        assertThat(result).doesNotContain("a@example.com");
        assertThat(result).doesNotContain("010-1234-5678");
        assertThat(result).doesNotContain("홍길동");
        assertThat(result).contains("email=a***@example.com");
        assertThat(result).contains("phone=010-****-5678");
        assertThat(result).contains("nickname=홍**");
    }

    private PiiExceptionMaskingConverter newStartedConverter() {
        PiiExceptionMaskingConverter converter = new PiiExceptionMaskingConverter();
        converter.start();
        return converter;
    }
}
