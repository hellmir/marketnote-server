package com.personal.marketnote.fulfillment.domain.delivery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FulfillmentWorkStatus {
    NOT_REGISTERED("출고 미접수"),
    REGISTERED("출고 접수"),
    PICKING("피킹 진행 중"),
    PICKED("피킹 완료"),
    PACKING("포장 중"),
    RELEASED("출고 완료");

    private final String description;

    public boolean isNotRegistered() {
        return this == NOT_REGISTERED;
    }

    public boolean isRegistered() {
        return this == REGISTERED;
    }

    public boolean isPicking() {
        return this == PICKING;
    }

    public boolean isPicked() {
        return this == PICKED;
    }

    public boolean isPacking() {
        return this == PACKING;
    }

    public boolean isReleased() {
        return this == RELEASED;
    }
}
