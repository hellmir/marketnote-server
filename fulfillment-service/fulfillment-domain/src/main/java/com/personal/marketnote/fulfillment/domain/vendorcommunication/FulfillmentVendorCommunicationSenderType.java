package com.personal.marketnote.fulfillment.domain.vendorcommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FulfillmentVendorCommunicationSenderType {
    SERVER("서버에서 전송"),
    VENDOR("벤더에서 전송");

    private final String description;
}
