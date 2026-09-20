package com.personal.marketnote.fulfillment.domain.shipping;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.shipping.exception.CarrierCodeNoValueException;

import java.util.Objects;

public final class CarrierCode {
    private final String value;

    private CarrierCode(String value) {
        this.value = value;
    }

    public static CarrierCode of(String value) {
        validateNotBlank(value);
        return new CarrierCode(value);
    }

    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new CarrierCodeNoValueException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarrierCode other)) {
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
        return "CarrierCode{value=" + value + "}";
    }
}
