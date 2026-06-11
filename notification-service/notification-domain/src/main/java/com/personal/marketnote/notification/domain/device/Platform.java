package com.personal.marketnote.notification.domain.device;

import com.personal.marketnote.common.utility.FormatValidator;

public enum Platform {
    ANDROID,
    IOS;

    public static Platform from(String value) {
        if (FormatValidator.hasNoValue(value)) {
            throw new InvalidPlatformException("플랫폼 값이 비어있습니다.");
        }
        try {
            return Platform.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new InvalidPlatformException("지원하지 않는 플랫폼입니다.");
        }
    }
}
