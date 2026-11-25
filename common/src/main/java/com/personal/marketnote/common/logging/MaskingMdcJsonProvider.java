package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logstash JsonProvider that emits whitelisted MDC keys as top-level JSON fields.
 *
 * <p>Each key is masked via {@link PiiMaskingConverter#maskByKey(String, String)}; non-PII
 * keys (requestId, traceId, spanId, correlationId, userId, orderId, sagaId, sagaStep,
 * errorCode 등) pass through unchanged. PII keys (email, phone, nickname, name) are masked.
 *
 * <p>Configured in logback-common.xml via {@code <includeMdcKeyName>} elements.
 */
public class MaskingMdcJsonProvider extends AbstractJsonProvider<ILoggingEvent> {

    private final List<String> includeMdcKeyNames = new ArrayList<>();

    public void addIncludeMdcKeyName(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        includeMdcKeyNames.add(name);
    }

    public List<String> getIncludeMdcKeyNames() {
        return includeMdcKeyNames;
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc == null || mdc.isEmpty()) {
            return;
        }
        for (String key : includeMdcKeyNames) {
            String value = mdc.get(key);
            if (value == null) {
                continue;
            }
            generator.writeStringField(key, PiiMaskingConverter.maskByKey(key, value));
        }
    }
}
