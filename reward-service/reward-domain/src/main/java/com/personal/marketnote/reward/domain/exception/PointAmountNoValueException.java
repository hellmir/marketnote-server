package com.personal.marketnote.reward.domain.exception;

public class PointAmountNoValueException extends IllegalArgumentException {
    public PointAmountNoValueException(String message) {
        super(message);
    }
}
