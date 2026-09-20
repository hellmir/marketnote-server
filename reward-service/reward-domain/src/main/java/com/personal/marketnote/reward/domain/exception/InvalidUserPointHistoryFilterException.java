package com.personal.marketnote.reward.domain.exception;

public class InvalidUserPointHistoryFilterException extends IllegalArgumentException {
    private static final String MESSAGE = "유효하지 않은 포인트 이력 필터입니다. value=%s";

    public InvalidUserPointHistoryFilterException(String value) {
        super(String.format(MESSAGE, value));
    }
}
