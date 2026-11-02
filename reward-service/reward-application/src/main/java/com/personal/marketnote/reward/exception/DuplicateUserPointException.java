package com.personal.marketnote.reward.exception;

public class DuplicateUserPointException extends IllegalStateException {
    private static final String MESSAGE = "이미 회원 포인트 정보가 생성되어 있습니다. 회원 ID: %d";

    public DuplicateUserPointException(Long userId) {
        super(String.format(MESSAGE, userId));
    }
}
