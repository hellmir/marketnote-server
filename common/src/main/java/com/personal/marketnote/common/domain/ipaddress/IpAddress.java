package com.personal.marketnote.common.domain.ipaddress;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidIpAddressException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.IpAddressNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RegularExpressionConstant;

import java.util.Objects;
import java.util.regex.Pattern;

public final class IpAddress {
    private static final String INVALID_FORMAT_MESSAGE = "IP 주소 형식이 올바르지 않습니다.";
    private static final int MAX_LENGTH = 45;
    private static final Pattern IPV4_PATTERN = Pattern.compile(RegularExpressionConstant.IPV4_PATTERN);
    private static final Pattern IPV6_PATTERN = Pattern.compile(RegularExpressionConstant.IPV6_PATTERN);
    private static final String MASKED_TOSTRING = "IpAddress{value=***}";

    private final String value;

    private IpAddress(String value) {
        this.value = value;
    }

    @JsonCreator
    public static IpAddress of(String value) {
        validateNotBlank(value);
        validateLength(value);
        validateFormat(value);
        return new IpAddress(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new IpAddressNoValueException();
        }
    }

    private static void validateLength(String value) {
        if (value.length() > MAX_LENGTH) {
            throw new InvalidIpAddressException(INVALID_FORMAT_MESSAGE);
        }
    }

    private static void validateFormat(String value) {
        if (IPV4_PATTERN.matcher(value).matches()) {
            return;
        }
        if (IPV6_PATTERN.matcher(value).matches()) {
            return;
        }
        throw new InvalidIpAddressException(INVALID_FORMAT_MESSAGE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IpAddress other)) {
            return false;
        }
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return MASKED_TOSTRING;
    }
}
