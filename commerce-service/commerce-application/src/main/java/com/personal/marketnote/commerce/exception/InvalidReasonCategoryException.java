package com.personal.marketnote.commerce.exception;

public class InvalidReasonCategoryException extends IllegalArgumentException {
    private static final String MESSAGE = "주문 상태 변경에 적합하지 않은 사유 카테고리입니다.";

    public InvalidReasonCategoryException() {
        super(MESSAGE);
    }
}
