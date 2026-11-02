package com.personal.marketnote.reward.domain.exception;

public class InvalidPointAmountException extends IllegalArgumentException {
    public InvalidPointAmountException(String message) {
        super(message);
    }
}
