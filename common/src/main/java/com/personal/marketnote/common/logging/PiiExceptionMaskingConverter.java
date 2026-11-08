package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;

/**
 * Logback pattern layout converter for exception stack trace masking.
 *
 * <p>Extends {@link ThrowableProxyConverter} so logback automatically invokes it when a
 * {@link org.slf4j.Logger} call includes a {@link Throwable} argument. The base converter
 * serializes the full stack trace (including cause chain and each frame), and this class
 * post-processes the result with {@link PiiMaskingConverter#mask(String)} so any PII tokens
 * (email, nickname, phone, name) embedded in exception messages or cause messages are masked.
 *
 * <p>Registered via {@code <conversionRule conversionWord="piiEx" ...>} in logback-common.xml
 * and referenced in the PatternLayout as {@code %piiEx} (replacing default {@code %ex}).
 *
 * <p>Note: stack frame class/method names and line numbers are not PII, but they flow through
 * the masker untouched since {@link PiiMaskingConverter} only matches explicit token prefixes
 * ({@code email=}, {@code nickname=}, {@code phone=}, {@code name=}).
 */
public class PiiExceptionMaskingConverter extends ThrowableProxyConverter {

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        String rendered = super.throwableProxyToString(tp);
        return PiiMaskingConverter.mask(rendered);
    }
}
