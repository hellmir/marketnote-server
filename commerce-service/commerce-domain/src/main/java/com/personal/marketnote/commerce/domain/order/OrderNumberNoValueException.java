package com.personal.marketnote.commerce.domain.order;

public class OrderNumberNoValueException extends RuntimeException {
    public OrderNumberNoValueException(String message) {
        super(message);
    }
}
