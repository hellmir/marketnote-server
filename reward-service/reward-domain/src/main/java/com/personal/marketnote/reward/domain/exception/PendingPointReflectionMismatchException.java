package com.personal.marketnote.reward.domain.exception;

public class PendingPointReflectionMismatchException extends RuntimeException {
    private static final String MESSAGE = "적립 예정 포인트 이력 반영 건수가 일치하지 않습니다. 조회: %d건, 반영: %d건";

    public PendingPointReflectionMismatchException(int expected, int actual) {
        super(String.format(MESSAGE, expected, actual));
    }
}
