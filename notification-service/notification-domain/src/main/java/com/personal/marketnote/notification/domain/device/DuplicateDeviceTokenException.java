package com.personal.marketnote.notification.domain.device;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateDeviceTokenException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_DEVICE_TOKEN_04::이미 등록된 디바이스 ID입니다.";

    public DuplicateDeviceTokenException() {
        super(MESSAGE);
    }
}
