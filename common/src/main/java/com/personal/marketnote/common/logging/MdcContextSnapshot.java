package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public record MdcContextSnapshot(Map<String, String> contextMap) {

    public static MdcContextSnapshot capture() {
        Map<String, String> snapshot = MDC.getCopyOfContextMap();
        if (FormatValidator.isNull(snapshot)) {
            return new MdcContextSnapshot(null);
        }
        return new MdcContextSnapshot(Map.copyOf(snapshot));
    }

    public void applyOrInitialize() {
        if (FormatValidator.hasNoValue(contextMap)) {
            MDC.clear();
            MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, UUID.randomUUID().toString());
            return;
        }
        MDC.setContextMap(contextMap);
        if (FormatValidator.hasNoValue(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY))) {
            MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, UUID.randomUUID().toString());
        }
    }
}
