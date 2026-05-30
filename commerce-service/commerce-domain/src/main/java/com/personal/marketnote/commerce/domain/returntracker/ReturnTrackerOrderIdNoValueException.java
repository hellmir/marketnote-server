package com.personal.marketnote.commerce.domain.returntracker;

public class ReturnTrackerOrderIdNoValueException extends IllegalArgumentException {
    public ReturnTrackerOrderIdNoValueException() {
        super("ReturnTracker 생성 시 orderId는 필수입니다.");
    }
}
