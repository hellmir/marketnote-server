package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.InvalidGoodsStatusException;

public enum GoodsStatus {
    SALE("SALE"),
    SUSPENDED("SUS");

    private final String dbValue;

    GoodsStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public boolean isSale() {
        return this == SALE;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public static GoodsStatus from(String value) {
        if (FormatValidator.hasNoValue(value)) {
            throw new InvalidGoodsStatusException(value);
        }
        for (GoodsStatus status : values()) {
            if (status.name().equals(value) || status.dbValue.equals(value)) {
                return status;
            }
        }
        throw new InvalidGoodsStatusException(value);
    }
}
