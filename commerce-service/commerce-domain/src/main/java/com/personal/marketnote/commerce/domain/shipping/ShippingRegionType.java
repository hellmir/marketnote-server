package com.personal.marketnote.commerce.domain.shipping;

import com.personal.marketnote.common.utility.FormatValidator;

public enum ShippingRegionType {
    NORMAL,
    JEJU,
    ISLAND,
    DELIVERY_IMPOSSIBLE;

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

    public static ShippingRegionType from(String regionType) {
        if (FormatValidator.hasNoValue(regionType)) {
            return NORMAL;
        }
        try {
            return valueOf(regionType);
        } catch (IllegalArgumentException ignored) {
            // 미정의 코드 — 기본값 NORMAL 폴백 (외부 입력 정화)
            return NORMAL;
        }
    }
}
