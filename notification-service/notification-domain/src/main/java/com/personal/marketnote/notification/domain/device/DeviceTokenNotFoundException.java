package com.personal.marketnote.notification.domain.device;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class DeviceTokenNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "디바이스 토큰을 찾을 수 없습니다.";

    public DeviceTokenNotFoundException() {
        super(MESSAGE);
    }
}
