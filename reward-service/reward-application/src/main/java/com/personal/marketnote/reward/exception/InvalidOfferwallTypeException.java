package com.personal.marketnote.reward.exception;

public class InvalidOfferwallTypeException extends IllegalArgumentException {
    private static final String INVALID_OFFERWALL_TYPE_EXCEPTION_MESSAGE = "유효하지 않은 오퍼월 타입입니다. 전송된 오퍼월 타입: %s";

    public InvalidOfferwallTypeException(String offerwallType) {
        super(String.format(INVALID_OFFERWALL_TYPE_EXCEPTION_MESSAGE, offerwallType));
    }
}
