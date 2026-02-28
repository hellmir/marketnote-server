package com.personal.marketnote.commerce.exception;

public class NoUnsettledAllocationException extends IllegalStateException {
    public NoUnsettledAllocationException() {
        super("미정산 배분이 존재하지 않습니다.");
    }
}
