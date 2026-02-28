package com.personal.marketnote.commerce.exception;

public class SettlementAlreadyExistsException extends IllegalStateException {
    public SettlementAlreadyExistsException(Long sellerId, Integer year, Integer month) {
        super("이미 해당 기간의 정산이 존재합니다. sellerId=" + sellerId + ", year=" + year + ", month=" + month);
    }
}
