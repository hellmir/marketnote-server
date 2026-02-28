package com.personal.marketnote.commerce.exception;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(String accountName) {
        super("비활성 계정과목에는 분개할 수 없습니다. 계정명: " + accountName);
    }
}
