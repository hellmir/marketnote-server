package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.InvalidGifticonOrderStatusFilterException;

import java.util.List;

public enum GifticonOrderStatusFilter {
    AVAILABLE,
    COMPLETED_OR_EXPIRED;

    public List<GifticonOrderStatus> toStatuses() {
        if (this == AVAILABLE) {
            return List.of(GifticonOrderStatus.ISSUED);
        }
        return List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED);
    }

    public static GifticonOrderStatusFilter from(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidGifticonOrderStatusFilterException(value);
        }
    }
}
