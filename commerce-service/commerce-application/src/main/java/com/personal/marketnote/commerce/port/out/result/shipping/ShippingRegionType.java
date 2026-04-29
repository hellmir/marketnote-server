package com.personal.marketnote.commerce.port.out.result.shipping;

import com.personal.marketnote.common.utility.FormatValidator;

public enum ShippingRegionType {
    NORMAL,
    JEJU,
    ISLAND;

    public boolean isNormal() {
        return this == NORMAL;
    }

    public boolean isJeju() {
        return this == JEJU;
    }

    public boolean isIsland() {
        return this == ISLAND;
    }

    public static ShippingRegionType from(String regionType) {
        if (FormatValidator.hasNoValue(regionType)) {
            return NORMAL;
        }
        try {
            return valueOf(regionType);
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
}
