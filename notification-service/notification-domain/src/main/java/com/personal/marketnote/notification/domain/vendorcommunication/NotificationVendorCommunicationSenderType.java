package com.personal.marketnote.notification.domain.vendorcommunication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationVendorCommunicationSenderType {
    SERVER("서버에서 전송"),
    VENDOR("벤더에서 전송");

    private final String description;
}
