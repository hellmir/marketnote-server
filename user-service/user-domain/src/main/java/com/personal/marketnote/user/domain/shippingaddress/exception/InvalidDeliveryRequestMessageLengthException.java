package com.personal.marketnote.user.domain.shippingaddress.exception;

public class InvalidDeliveryRequestMessageLengthException extends IllegalArgumentException {

    public InvalidDeliveryRequestMessageLengthException() {
        super("배송 요청사항 메시지는 최대 60자까지 입력할 수 있습니다.");
    }
}
