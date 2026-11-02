package com.personal.marketnote.reward.domain.exception;

public class UserPointHistoryAmountNoValueException extends RuntimeException {
    private static final String MESSAGE = "포인트 이력 금액은 필수값입니다.";

    public UserPointHistoryAmountNoValueException() {
        super(MESSAGE);
    }
}
