package com.personal.marketnote.commerce.domain.vendorcommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommerceVendorCommunicationSenderType {
    SERVER("서버에서 전송"),
    VENDOR("벤더에서 전송");

    private final String description;
}
