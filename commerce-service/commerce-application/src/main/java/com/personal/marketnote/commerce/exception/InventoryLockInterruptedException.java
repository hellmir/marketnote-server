package com.personal.marketnote.commerce.exception;

public class InventoryLockInterruptedException extends RuntimeException {
    private static final String MESSAGE = "재고 락 처리 중 인터럽트가 발생했습니다.";

    public InventoryLockInterruptedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}

