package com.personal.marketnote.common.domain.deliveryrequestmessage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidDeliveryRequestMessageLengthException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.DeliveryRequestMessageNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;

public final class DeliveryRequestMessage {
    private static final int MAX_LENGTH = 60;
    private static final String NO_VALUE_MESSAGE = "배송 요청사항 메시지는 필수입니다.";
    private static final String INVALID_LENGTH_MESSAGE = "배송 요청사항 메시지는 최대 60자까지 입력할 수 있습니다.";

    private final String value;

    private DeliveryRequestMessage(String value) {
        this.value = value;
    }

    @JsonCreator
    public static DeliveryRequestMessage of(String value) {
        validateNotBlank(value);
        validateLength(value);
        return new DeliveryRequestMessage(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new DeliveryRequestMessageNoValueException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateLength(String value) {
        if (value.length() > MAX_LENGTH) {
            throw new InvalidDeliveryRequestMessageLengthException(INVALID_LENGTH_MESSAGE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliveryRequestMessage other)) {
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
        return "DeliveryRequestMessage{value=" + value + "}";
    }
}
