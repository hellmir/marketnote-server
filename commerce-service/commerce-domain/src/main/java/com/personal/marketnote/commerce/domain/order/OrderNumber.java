package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RandomCodeGenerator;

import java.util.Objects;

public class OrderNumber {
    private static final String ORDER_NUMBER_NO_VALUE_EXCEPTION = "주문번호는 필수입니다.";

    private final String value;

    private OrderNumber(String value) {
        this.value = value;
    }

    public static OrderNumber of(String value) {
        validate(value);
        return new OrderNumber(value);
    }

    public static OrderNumber generate() {
        return new OrderNumber(RandomCodeGenerator.generateOrderNumber());
    }

    private static void validate(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new OrderNumberNoValueException(ORDER_NUMBER_NO_VALUE_EXCEPTION);
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (FormatValidator.hasNoValue(o) || getClass() != o.getClass()) {
            return false;
        }
        OrderNumber that = (OrderNumber) o;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
