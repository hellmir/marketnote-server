package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logback pattern layout converter for PII masking.
 *
 * <p>Masks email, nickname, phone, name tokens in log messages at appender level
 * so throw-site code can keep emitting raw values via structured key-value pairs
 * (e.g. {@code log.warn("...", keyValue("email", email))}).
 *
 * <p>Masking rules:
 * <ul>
 *     <li>email — {@code t***@example.com} (local-part first char + *** + @ + domain)</li>
 *     <li>nickname — {@code 홍**} (first char + N stars matching the remaining chars)</li>
 *     <li>phone — {@code 010-****-1234} (middle 4 digits masked)</li>
 *     <li>name — same as nickname</li>
 * </ul>
 *
 * <p>null/blank/malformed values pass through unchanged (defensive).
 *
 * <p>Registered via {@code <conversionRule>} in logback-spring.xml.
 */
public class PiiMaskingConverter extends ClassicConverter {

    private static final String TOKEN_TERMINATOR = "[\\s,;)}\\]\"']";

    // Value captured up to the next terminator or end of line.
    private static final String VALUE_GROUP = "([^\\s,;)}\\]\"']+)";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("email=" + VALUE_GROUP);
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("nickname=" + VALUE_GROUP);
    private static final Pattern PHONE_PATTERN = Pattern.compile("phone=" + VALUE_GROUP);
    private static final Pattern NAME_PATTERN = Pattern.compile("(?<![A-Za-z])name=" + VALUE_GROUP);

    private static final Pattern EMAIL_VALUE_PATTERN = Pattern.compile("^([^@]+)@([^@]+)$");
    private static final Pattern PHONE_VALUE_WITH_HYPHEN = Pattern.compile("^(\\d{2,4})-(\\d{3,4})-(\\d{4})$");
    private static final Pattern PHONE_VALUE_NO_HYPHEN_9 = Pattern.compile("^(02)(\\d{3})(\\d{4})$");
    private static final Pattern PHONE_VALUE_NO_HYPHEN_10 = Pattern.compile("^(\\d{3})(\\d{3})(\\d{4})$");
    private static final Pattern PHONE_VALUE_NO_HYPHEN_11 = Pattern.compile("^(\\d{3})(\\d{4})(\\d{4})$");
    private static final Pattern TRAILING_PUNCT_PATTERN = Pattern.compile("[.,:;!?]+$");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        return mask(message);
    }

    public static String mask(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String masked = maskToken(message, EMAIL_PATTERN, PiiMaskingConverter::maskEmail);
        masked = maskToken(masked, NICKNAME_PATTERN, PiiMaskingConverter::maskNickname);
        masked = maskToken(masked, PHONE_PATTERN, PiiMaskingConverter::maskPhone);
        masked = maskToken(masked, NAME_PATTERN, PiiMaskingConverter::maskName);
        return masked;
    }

    public static String maskEmail(String value) {
        if (isBlank(value)) {
            return value;
        }
        String[] split = splitTrailingPunct(value);
        String core = split[0];
        String trailing = split[1];
        Matcher matcher = EMAIL_VALUE_PATTERN.matcher(core);
        if (!matcher.matches()) {
            return value;
        }
        String localPart = matcher.group(1);
        String domain = matcher.group(2);
        if (localPart.isEmpty()) {
            return value;
        }
        return localPart.charAt(0) + "***@" + domain + trailing;
    }

    public static String maskNickname(String value) {
        return maskFirstCharThenStars(value);
    }

    public static String maskName(String value) {
        return maskFirstCharThenStars(value);
    }

    public static String maskPhone(String value) {
        if (isBlank(value)) {
            return value;
        }
        String[] split = splitTrailingPunct(value);
        String core = split[0];
        String trailing = split[1];
        Matcher matcher = matchPhone(core);
        if (matcher == null) {
            return value;
        }
        String prefix = matcher.group(1);
        String suffix = matcher.group(3);
        return prefix + "-****-" + suffix + trailing;
    }

    private static Matcher matchPhone(String value) {
        Matcher hyphen = PHONE_VALUE_WITH_HYPHEN.matcher(value);
        if (hyphen.matches()) {
            return hyphen;
        }
        Matcher eleven = PHONE_VALUE_NO_HYPHEN_11.matcher(value);
        if (eleven.matches()) {
            return eleven;
        }
        Matcher ten = PHONE_VALUE_NO_HYPHEN_10.matcher(value);
        if (ten.matches()) {
            return ten;
        }
        Matcher nine = PHONE_VALUE_NO_HYPHEN_9.matcher(value);
        if (nine.matches()) {
            return nine;
        }
        return null;
    }

    private static String maskFirstCharThenStars(String value) {
        if (isBlank(value)) {
            return value;
        }
        String[] split = splitTrailingPunct(value);
        String core = split[0];
        String trailing = split[1];
        int length = core.codePointCount(0, core.length());
        if (length <= 1) {
            return value;
        }
        int firstCharEnd = core.offsetByCodePoints(0, 1);
        String firstChar = core.substring(0, firstCharEnd);
        int remaining = length - 1;
        return firstChar + "*".repeat(remaining) + trailing;
    }

    private static String[] splitTrailingPunct(String value) {
        Matcher matcher = TRAILING_PUNCT_PATTERN.matcher(value);
        if (matcher.find()) {
            return new String[]{value.substring(0, matcher.start()), matcher.group()};
        }
        return new String[]{value, ""};
    }

    private static String maskToken(String input, Pattern pattern, java.util.function.UnaryOperator<String> masker) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder builder = null;
        while (matcher.find()) {
            if (builder == null) {
                builder = new StringBuilder();
            }
            String raw = matcher.group(1);
            String masked = masker.apply(raw);
            String replacement = matcher.group(0).replace(raw, masked);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        if (builder == null) {
            return input;
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
