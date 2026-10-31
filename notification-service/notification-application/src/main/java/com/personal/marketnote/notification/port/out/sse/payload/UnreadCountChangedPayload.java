package com.personal.marketnote.notification.port.out.sse.payload;

public record UnreadCountChangedPayload(long unreadCount) {

    public static UnreadCountChangedPayload of(long unreadCount) {
        return new UnreadCountChangedPayload(unreadCount);
    }
}
