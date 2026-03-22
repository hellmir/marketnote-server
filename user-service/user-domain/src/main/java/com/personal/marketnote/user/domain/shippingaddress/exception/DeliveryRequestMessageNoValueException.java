package com.personal.marketnote.user.domain.shippingaddress.exception;

public class DeliveryRequestMessageNoValueException extends IllegalArgumentException {

    public DeliveryRequestMessageNoValueException() {
        super("직접입력 선택 시 배송 요청사항 메시지는 필수입니다.");
    }
}
