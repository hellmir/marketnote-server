package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;

import java.io.IOException;

/**
 * Logstash JsonProvider that emits the formatted log message under a JSON field
 * with PII tokens masked via {@link PiiMaskingConverter#mask(String)}.
 *
 * <p>Default field name is {@code message}.
 */
public class MaskingMessageJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

    public static final String DEFAULT_FIELD_NAME = "message";

    public MaskingMessageJsonProvider() {
        setFieldName(DEFAULT_FIELD_NAME);
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        String formatted = event.getFormattedMessage();
        if (formatted == null) {
            return;
        }
        generator.writeStringField(getFieldName(), PiiMaskingConverter.mask(formatted));
    }
}
