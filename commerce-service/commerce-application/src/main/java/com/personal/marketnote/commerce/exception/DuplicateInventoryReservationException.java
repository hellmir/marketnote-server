package com.personal.marketnote.commerce.exception;

public class DuplicateInventoryReservationException extends RuntimeException {
    public DuplicateInventoryReservationException(Long orderId) {
        super("이미 처리된 재고 예약입니다. orderId=" + orderId);
    }
}
