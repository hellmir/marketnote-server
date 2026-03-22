package com.personal.marketnote.user.exception;

public class TooManyOtherAddressesException extends IllegalArgumentException {
    private static final String MESSAGE = "%s:: 기타 배송지는 최대 %d개까지 등록할 수 있습니다.";

    public TooManyOtherAddressesException(String code, long maxCount) {
        super(String.format(MESSAGE, code, maxCount));
    }
}
