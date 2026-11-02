package com.personal.marketnote.fulfillment.domain.shipping;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.shipping.exception.TrackingNumberNoValueException;

import java.util.Objects;

public final class TrackingNumber {
    private final String value;

    private TrackingNumber(String value) {
        this.value = value;
    }

    public static TrackingNumber of(String value) {
        validateNotBlank(value);
        return new TrackingNumber(value);
    }

    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new TrackingNumberNoValueException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrackingNumber other)) {
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
        return "TrackingNumber{value=" + value + "}";
    }
}
