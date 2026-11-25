package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;

/**
 * Logstash ShortenedThrowableConverter that post-masks rendered stack traces
 * with {@link PiiMaskingConverter#mask(String)} so PII tokens embedded in
 * exception messages are scrubbed before reaching CloudWatch.
 *
 * <p>Wired into the JSON encoder via {@code <stackTrace><throwableConverter ...>} block.
 */
public class MaskingThrowableConverter extends ShortenedThrowableConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String rendered = super.convert(event);
        return PiiMaskingConverter.mask(rendered);
    }
}
