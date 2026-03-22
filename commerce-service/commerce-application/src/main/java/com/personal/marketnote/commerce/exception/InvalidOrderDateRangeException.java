package com.personal.marketnote.commerce.exception;

public class InvalidOrderDateRangeException extends IllegalArgumentException {
    public InvalidOrderDateRangeException() {
        super("종료일은 시작일 이후여야 합니다.");
    }
}
