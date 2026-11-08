package com.personal.marketnote.user.exception;

public class TooManyOtherAddressesException extends IllegalArgumentException {
    private static final String MESSAGE
            = "ERR_SHIPPING_ADDRESS_OTHER_LIMIT_01::기타 배송지 등록 개수 제한을 초과하였습니다.";

    public TooManyOtherAddressesException() {
        super(MESSAGE);
    }
}
