package com.personal.marketnote.commerce.exception;

public class ReturnPgRefundFailedException extends RuntimeException {
    public ReturnPgRefundFailedException(Long orderId, Throwable cause) {
        super("반품 PG 환불 실패. orderId=" + orderId, cause);
    }
}
