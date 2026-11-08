package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Logback pattern layout converter for MDC value masking.
 *
 * <p>Extends {@link ClassicConverter} and reads the MDC key from the pattern option,
 * then masks the resolved <b>bare</b> value (not a {@code key=value} token) according
 * to the key name:
 * <ul>
 *     <li>{@code email}              → {@link PiiMaskingConverter#maskEmail(String)}</li>
 *     <li>{@code phone}              → {@link PiiMaskingConverter#maskPhone(String)}</li>
 *     <li>{@code nickname}           → {@link PiiMaskingConverter#maskNickname(String)}</li>
 *     <li>{@code name}               → {@link PiiMaskingConverter#maskName(String)}</li>
 *     <li>any other key              → value passes through unchanged (non-PII context)</li>
 * </ul>
 *
 * <p>Usage in logback pattern:
 * <pre>
 *     %piiMdc{email}     — reads MDC("email") and returns {@code t***@example.com}
 *     %piiMdc{phone}     — reads MDC("phone") and returns {@code 010-****-5678}
 *     %piiMdc{userId}    — reads MDC("userId") and returns raw value (no masking)
 * </pre>
 *
 * <p>Behavior:
 * <ul>
 *     <li>Option missing — returns empty string (matches logback {@code %X{}} default).</li>
 *     <li>MDC key missing or value null — returns empty string.</li>
 * </ul>
 *
 * <p>Registered via {@code <conversionRule>} in logback-common.xml.
 */
public class PiiMdcMaskingConverter extends ClassicConverter {

    /**
     * Allowed PII MDC keys — case-sensitive, lowercase single-word convention.
     *
     * <p>MDC key naming rules:
     * <ul>
     *     <li>Lowercase only: {@code email}, {@code phone}, {@code nickname}, {@code name}</li>
     *     <li>Variants pass through unchanged: {@code userEmail}, {@code user_email}, {@code phoneNumber}, {@code NAME}</li>
     *     <li>Callers must use the canonical key to opt into masking</li>
     * </ul>
     */
    private static final Set<String> ALLOWED_PII_KEYS = Set.of("email", "phone", "nickname", "name");

    @Override
    public String convert(ILoggingEvent event) {
        String key = getFirstOption();
        if (key == null || key.isEmpty()) {
            return "";
        }
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc == null) {
            return "";
        }
        String value = mdc.get(key);
        if (value == null) {
            return "";
        }
        return resolveMasker(key).apply(value);
    }

    private UnaryOperator<String> resolveMasker(String key) {
        if (!ALLOWED_PII_KEYS.contains(key)) {
            return UnaryOperator.identity();
        }
        if ("email".equals(key)) {
            return PiiMaskingConverter::maskEmail;
        }
        if ("phone".equals(key)) {
            return PiiMaskingConverter::maskPhone;
        }
        if ("nickname".equals(key)) {
            return PiiMaskingConverter::maskNickname;
        }
        if ("name".equals(key)) {
            return PiiMaskingConverter::maskName;
        }
        return UnaryOperator.identity();
    }
}
