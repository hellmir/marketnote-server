package com.personal.marketnote.commerce.exception;

public class InvalidPickupRequestMessageException extends IllegalArgumentException {
    public InvalidPickupRequestMessageException() {
        super("ERR_PICKUP_01::회수 요청 타입이 직접 입력인 경우 회수 요청사항은 필수입니다.");
    }
}
