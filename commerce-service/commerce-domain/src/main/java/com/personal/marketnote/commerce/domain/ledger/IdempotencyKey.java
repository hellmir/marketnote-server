package com.personal.marketnote.commerce.domain.ledger;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;

public class IdempotencyKey {
    private static final String KEY_NO_VALUE_EXCEPTION = "멱등성 키는 필수입니다.";
    private static final String GENERATE_TYPE_NO_VALUE_EXCEPTION = "멱등성 키 타입은 필수입니다.";
    private static final String GENERATE_ID_NO_VALUE_EXCEPTION = "멱등성 키 식별자는 필수입니다.";
    private static final String KEY_DELIMITER = ":";

    private final String value;

    private IdempotencyKey(String value) {
        this.value = value;
    }

    public static IdempotencyKey of(String value) {
        validate(value);
        return new IdempotencyKey(value);
    }

    public static IdempotencyKey generate(String type, Long id) {
        if (FormatValidator.hasNoValue(type) || type.isBlank()) {
            throw new IdempotencyKeyNoValueException(GENERATE_TYPE_NO_VALUE_EXCEPTION);
        }
        if (FormatValidator.hasNoValue(id)) {
            throw new IdempotencyKeyNoValueException(GENERATE_ID_NO_VALUE_EXCEPTION);
        }
        return new IdempotencyKey(type + KEY_DELIMITER + id);
    }

    private static void validate(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new IdempotencyKeyNoValueException(KEY_NO_VALUE_EXCEPTION);
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
        IdempotencyKey that = (IdempotencyKey) o;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
