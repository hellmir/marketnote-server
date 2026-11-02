package com.personal.marketnote.fulfillment.domain.servicecommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FulfillmentServiceCommunicationTargetType {
    GENERAL("일반"),
    COMMERCE_INVENTORY("커머스 재고");

    @SuppressWarnings("unused")
    private final String description;
}
