package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskingMessageJsonProviderTest {

    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("formatted message에 email PII 토큰이 포함되면 마스킹된 message 필드를 출력한다")
    void emailTokenMasked() throws Exception {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("user email=foo@bar.com login");

        String json = renderProvider(event);

        Map<String, Object> parsed = parse(json);
        assertThat(parsed).containsEntry("message", "user email=f***@bar.com login");
    }

    @Test
    @DisplayName("formatted message에 phone PII 토큰이 포함되면 마스킹된다")
    void phoneTokenMasked() throws Exception {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("phone=010-1234-5678 verified");

        String json = renderProvider(event);

        Map<String, Object> parsed = parse(json);
        assertThat(parsed).containsEntry("message", "phone=010-****-5678 verified");
    }

    @Test
    @DisplayName("formatted message가 null이면 message 필드를 출력하지 않는다")
    void nullMessageOmitted() throws Exception {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(null);

        String json = renderProvider(event);

        Map<String, Object> parsed = parse(json);
        assertThat(parsed).doesNotContainKey("message");
    }

    @Test
    @DisplayName("PII가 없는 일반 message는 그대로 출력한다")
    void plainMessagePassthrough() throws Exception {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("order created orderId=12345");

        String json = renderProvider(event);

        Map<String, Object> parsed = parse(json);
        assertThat(parsed).containsEntry("message", "order created orderId=12345");
    }

    @Test
    @DisplayName("setFieldName으로 출력 필드명을 커스터마이즈할 수 있다")
    void customFieldName() throws Exception {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("hello");

        MaskingMessageJsonProvider provider = new MaskingMessageJsonProvider();
        provider.setFieldName("msg");
        String json = renderWith(provider, event);

        Map<String, Object> parsed = parse(json);
        assertThat(parsed).containsEntry("msg", "hello");
        assertThat(parsed).doesNotContainKey("message");
    }

    private String renderProvider(ILoggingEvent event) throws Exception {
        return renderWith(new MaskingMessageJsonProvider(), event);
    }

    private String renderWith(MaskingMessageJsonProvider provider, ILoggingEvent event) throws Exception {
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
