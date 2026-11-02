package com.personal.marketnote.commerce.domain.inventory;

public class InvalidInventoryReservationQuantityException extends IllegalStateException {
    private static final String MESSAGE = "예약 확정 수량이 현재 예약 수량을 초과합니다. 현재 예약: %d, 확정 요청: %d";

    public InvalidInventoryReservationQuantityException(int currentReserved, int requestedQuantity) {
        super(String.format(MESSAGE, currentReserved, requestedQuantity));
    }
}
