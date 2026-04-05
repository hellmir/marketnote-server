package com.personal.marketnote.reward.domain.gifticon;

import java.util.Map;

public enum GifticonOrderStatus {
    PENDING,
    ISSUED,
    USED,
    EXPIRED,
    CANCELLED,
    SEND_FAILED;

    private static final Map<String, GifticonOrderStatus> PIN_STATUS_MAP = Map.ofEntries(
            Map.entry("01", ISSUED),
            Map.entry("06", ISSUED),
            Map.entry("02", USED),
            Map.entry("08", EXPIRED),
            Map.entry("11", EXPIRED),
            Map.entry("03", CANCELLED),
            Map.entry("05", CANCELLED),
            Map.entry("07", CANCELLED),
            Map.entry("10", CANCELLED)
    );

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isIssued() {
        return this == ISSUED;
    }

    public boolean isUsed() {
        return this == USED;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isSendFailed() {
        return this == SEND_FAILED;
    }

    public boolean isAvailable() {
        return this == ISSUED;
    }

    public boolean isTerminal() {
        return this == USED || this == EXPIRED || this == CANCELLED;
    }

    public static GifticonOrderStatus fromPinStatus(String pinStatus) {
        return PIN_STATUS_MAP.getOrDefault(pinStatus, CANCELLED);
    }
}
