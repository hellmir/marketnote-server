package com.personal.marketnote.user.domain.shippingaddress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShippingAddressRegionType {
    NORMAL("일반"),
    JEJU("제주"),
    ISLAND("도서산간"),
    DELIVERY_IMPOSSIBLE("배송불가");

    private final String description;

    public boolean isNormal() {
        return this == NORMAL;
    }

    public boolean isJeju() {
        return this == JEJU;
    }

    public boolean isIsland() {
        return this == ISLAND;
    }

    public boolean isDeliveryImpossible() {
        return this == DELIVERY_IMPOSSIBLE;
    }
}
