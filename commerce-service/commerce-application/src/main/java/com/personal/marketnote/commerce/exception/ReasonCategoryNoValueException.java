package com.personal.marketnote.commerce.exception;

public class ReasonCategoryNoValueException extends RuntimeException {

    public ReasonCategoryNoValueException() {
        super("반품 택배비 계산을 위해 반품 사유 카테고리가 필요합니다.");
    }
}
