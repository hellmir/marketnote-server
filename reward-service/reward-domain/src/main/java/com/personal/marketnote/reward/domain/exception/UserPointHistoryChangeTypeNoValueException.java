package com.personal.marketnote.reward.domain.exception;

public class UserPointHistoryChangeTypeNoValueException extends RuntimeException {
    private static final String MESSAGE = "포인트 이력의 변경 유형은 필수입니다.";

    public UserPointHistoryChangeTypeNoValueException() {
        super(MESSAGE);
    }
}
