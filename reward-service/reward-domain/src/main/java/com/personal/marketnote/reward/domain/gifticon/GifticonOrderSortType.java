package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.InvalidGifticonOrderSortTypeException;

public enum GifticonOrderSortType {
    PURCHASE_LATEST,
    EXPIRY_SOONEST;

    public boolean isPurchaseLatest() {
        return this == PURCHASE_LATEST;
    }

    public boolean isExpirySoonest() {
        return this == EXPIRY_SOONEST;
    }

    public static GifticonOrderSortType from(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidGifticonOrderSortTypeException(value);
        }
    }
}
