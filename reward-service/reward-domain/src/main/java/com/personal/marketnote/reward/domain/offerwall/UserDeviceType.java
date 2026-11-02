package com.personal.marketnote.reward.domain.offerwall;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserDeviceType {
    ANDROID("안드로이드"),
    IOS("IOS");

    private final String description;

    public boolean isAndroid() {
        return this == ANDROID;
    }

    public boolean isIos() {
        return this == IOS;
    }
}
