package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskingMdcJsonProviderTest {

    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("includeMdcKeyName 화이트리스트에 등록된 키만 JSON 필드로 출력한다")
    void onlyWhitelistedKeysEmitted() throws Exception {
        Map<String, String> mdc = new LinkedHashMap<>();
        mdc.put("requestId", "req-1");
        mdc.put("traceId", "abc");
        mdc.put("ignoredKey", "should-not-leak");

        MaskingMdcJsonProvider provider = new MaskingMdcJsonProvider();
        provider.addIncludeMdcKeyName("requestId");
        provider.addIncludeMdcKeyName("traceId");

        Map<String, Object> parsed = parse(renderWith(provider, mdc));
        assertThat(parsed).containsEntry("requestId", "req-1").containsEntry("traceId", "abc");
        assertThat(parsed).doesNotContainKey("ignoredKey");
    }

    @Test
    @DisplayName("MDC email 키는 maskByKey로 마스킹된 값을 출력한다")
    void emailKeyMasked() throws Exception {
        Map<String, String> mdc = Map.of("email", "foo@bar.com");

        MaskingMdcJsonProvider provider = new MaskingMdcJsonProvider();
        provider.addIncludeMdcKeyName("email");

        Map<String, Object> parsed = parse(renderWith(provider, mdc));
        assertThat(parsed).containsEntry("email", "f***@bar.com");
    }

    @Test
    @DisplayName("MDC phone 키는 maskByKey로 마스킹된 값을 출력한다")
    void phoneKeyMasked() throws Exception {
        Map<String, String> mdc = Map.of("phone", "010-1234-5678");

        MaskingMdcJsonProvider provider = new MaskingMdcJsonProvider();
        provider.addIncludeMdcKeyName("phone");

        Map<String, Object> parsed = parse(renderWith(provider, mdc));
        assertThat(parsed).containsEntry("phone", "010-****-5678");
    }

    @Test
    @DisplayName("PII 화이트리스트 외 키는 원본 값을 그대로 출력한다")
    void nonPiiKeyPassthrough() throws Exception {
        Map<String, String> mdc = Map.of("userId", "12345", "orderId", "ORD-1");

        MaskingMdcJsonProvider provider = new MaskingMdcJsonProvider();
        provider.addIncludeMdcKeyName("userId");
        provider.addIncludeMdcKeyName("orderId");

        Map<String, Object> parsed = parse(renderWith(provider, mdc));
        assertThat(parsed).containsEntry("userId", "12345").containsEntry("orderId", "ORD-1");
    }

    @Test
    @DisplayName("MDC가 비어있으면 어떤 필드도 출력하지 않는다")
    void emptyMdcEmitsNoFields() throws Exception {
        MaskingMdcJsonProvider provider = new MaskingMdcJsonProvider();
        provider.addIncludeMdcKeyName("requestId");

        Map<String, Object> parsed = parse(renderWith(provider, Map.of()));
        assertThat(parsed).isEmpty();
    }

    @Test
    @DisplayName("MDC에 화이트리스트 키 값이 null이면 해당 필드를 출력하지 않는다")
    void nullValueSkipped() throws Exception {
        Map<String, String> mdc = new LinkedHashMap<>();
        mdc.put("requestId", "req-1");

        MaskingMdcJsonProvider provider = new MaskingMdcJsonProvider();
        provider.addIncludeMdcKeyName("requestId");
        provider.addIncludeMdcKeyName("traceId");

        Map<String, Object> parsed = parse(renderWith(provider, mdc));
        assertThat(parsed).containsEntry("requestId", "req-1").doesNotContainKey("traceId");
    }

    private String renderWith(MaskingMdcJsonProvider provider, Map<String, String> mdc) throws Exception {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(mdc);

        StringWriter writer = new StringWriter();
        try (JsonGenerator gen = jsonFactory.createGenerator(writer)) {
            gen.writeStartObject();
            provider.writeTo(gen, event);
            gen.writeEndObject();
        }
        return writer.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String json) throws Exception {
        return objectMapper.readValue(json, Map.class);
    }
}
