package com.personal.marketnote.common.domain.exception.illegalargument.novalue;

public class IpAddressNoValueException extends NoValueException {
    private static final String IP_ADDRESS_NO_VALUE_EXCEPTION_MESSAGE = "IP 주소는 필수값입니다.";

    public IpAddressNoValueException() {
        super(IP_ADDRESS_NO_VALUE_EXCEPTION_MESSAGE);
    }
}
