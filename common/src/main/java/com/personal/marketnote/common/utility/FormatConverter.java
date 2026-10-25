package com.personal.marketnote.common.utility;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidIdException;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.ParsingBooleanException;
import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.*;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.BooleanUtils.FALSE;
import static org.apache.commons.lang3.BooleanUtils.TRUE;

public class FormatConverter {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static Long parseId(String id) {
        try {
            return parseToLong(id);
        } catch (ParsingLongException e) {
            throw new InvalidIdException(id);
        }
    }

    public static Long parseToLong(String number) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException nfe) {
            throw new ParsingLongException(number);
        }
    }

    public static Integer parseToInteger(String number) throws ParsingIntegerException {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException nfe) {
            throw new ParsingIntegerException(number);
        }
    }

    public static Short parseToShort(String number) throws ParsingShortException {
        try {
            return Short.parseShort(number);
        } catch (NumberFormatException nfe) {
            throw new ParsingShortException(number);
        }
    }

    public static Byte parseToByte(String number) throws ParsingByteException {
        try {
            return Byte.parseByte(number);
        } catch (NumberFormatException nfe) {
            throw new ParsingByteException(number);
        }
    }

    public static Double parseToDouble(String primeNumber) {
        try {
            return Double.parseDouble(primeNumber);
        } catch (NumberFormatException nfe) {
            throw new ParsingDoubleException(primeNumber);
        }
    }

    public static Float parseToFloat(String primeNumber) {
        try {
            return Float.parseFloat(primeNumber);
        } catch (NumberFormatException nfe) {
            throw new ParsingFloatException(primeNumber);
        }
    }

    public static boolean parseToBoolean(String value) {
        if (!value.equals(TRUE) && !value.equals(FALSE)) {
            throw new ParsingBooleanException(value);
        }

        return Boolean.parseBoolean(value);
    }

    public static String toUpperCase(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return value;
        }

        return value.toUpperCase();
    }

    public static String toLowerCase(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return value;
        }

        return value.toLowerCase();
    }

    public static String snakeToCamel(String name) {
        if (FormatValidator.hasNoValue(name)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (char c : name.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
                continue;
            }

            if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
                continue;
            }

            result.append(Character.toLowerCase(c));
        }

        return result.toString();
    }

    public static String sanitizeFileName(String filename) {
        if (filename == null) {
            return null;
        }
        return filename.replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "");
    }

    public static String parseToNumberTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static LocalDateTime parseToLocalDateTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }

        try {
            long epochSeconds = Long.parseLong(time);
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), DEFAULT_ZONE_ID);
        } catch (NumberFormatException nfe) {
        }

        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
        }

        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
        }

        return null;
    }
}
