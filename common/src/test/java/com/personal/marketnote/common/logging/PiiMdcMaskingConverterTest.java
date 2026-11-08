package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PiiMdcMaskingConverterTest {

    @Test
    @DisplayName("MDC에 email 키가 있으면 마스킹된 값을 반환한다")
    void masksEmailFromMdc() {
        PiiMdcMaskingConverter converter = converterWithOption("email");
        ILoggingEvent event = eventWithMdc(Map.of("email", "test@example.com"));

        String result = converter.convert(event);

        assertThat(result).isEqualTo("t***@example.com");
    }

    @Test
    @DisplayName("MDC에 phone 키가 있으면 마스킹된 값을 반환한다")
    void masksPhoneFromMdc() {
        PiiMdcMaskingConverter converter = converterWithOption("phone");
        ILoggingEvent event = eventWithMdc(Map.of("phone", "010-1234-5678"));

        String result = converter.convert(event);

        assertThat(result).isEqualTo("010-****-5678");
    }

    @Test
    @DisplayName("MDC에 nickname 키가 있으면 마스킹된 값을 반환한다")
    void masksNicknameFromMdc() {
        PiiMdcMaskingConverter converter = converterWithOption("nickname");
        ILoggingEvent event = eventWithMdc(Map.of("nickname", "홍길동"));

        String result = converter.convert(event);

        assertThat(result).isEqualTo("홍**");
    }

    @Test
    @DisplayName("MDC에 name 키가 있으면 마스킹된 값을 반환한다")
    void masksNameFromMdc() {
        PiiMdcMaskingConverter converter = converterWithOption("name");
        ILoggingEvent event = eventWithMdc(Map.of("name", "홍길동"));

        String result = converter.convert(event);

        assertThat(result).isEqualTo("홍**");
    }

    @Test
    @DisplayName("MDC 키가 없으면 빈 문자열을 반환한다")
    void returnsEmptyWhenMdcKeyMissing() {
        PiiMdcMaskingConverter converter = converterWithOption("email");
        ILoggingEvent event = eventWithMdc(Map.of());

        String result = converter.convert(event);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("converter option이 없으면 빈 문자열을 반환한다")
    void returnsEmptyWhenNoOption() {
        PiiMdcMaskingConverter converter = new PiiMdcMaskingConverter();
        converter.start();
        ILoggingEvent event = eventWithMdc(Map.of("email", "test@example.com"));

        String result = converter.convert(event);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("MDC 값이 null이면 빈 문자열을 반환한다")
    void returnsEmptyWhenMdcValueNull() {
        PiiMdcMaskingConverter converter = converterWithOption("email");
        Map<String, String> mdc = new HashMap<>();
        mdc.put("email", null);
        ILoggingEvent event = eventWithMdc(mdc);

        String result = converter.convert(event);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("PII 토큰이 아닌 MDC 값은 원본 그대로 반환한다")
    void passesThroughNonPiiValue() {
        PiiMdcMaskingConverter converter = converterWithOption("traceId");
        ILoggingEvent event = eventWithMdc(Map.of("traceId", "abc-123"));

        String result = converter.convert(event);

        assertThat(result).isEqualTo("abc-123");
    }

    private PiiMdcMaskingConverter converterWithOption(String option) {
        PiiMdcMaskingConverter converter = new PiiMdcMaskingConverter() {
            @Override
            public String getFirstOption() {
                return option;
            }
        };
        converter.start();
        return converter;
    }

    private ILoggingEvent eventWithMdc(Map<String, String> mdc) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(mdc);
        return event;
    }
}
