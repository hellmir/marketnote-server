package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.notification.domain.template.NotificationCategory;

import java.time.LocalDateTime;

public class NightTimeNotificationPolicy {

    private static final int NIGHT_START_HOUR = 21;
    private static final int MORNING_HOUR = 8;

    private NightTimeNotificationPolicy() {
    }

    public static boolean isNightTime(LocalDateTime now) {
        int hour = now.getHour();
        return hour >= NIGHT_START_HOUR || hour < MORNING_HOUR;
    }

    public static LocalDateTime calculateNextMorning(LocalDateTime now) {
        if (now.getHour() >= NIGHT_START_HOUR) {
            return now.toLocalDate().plusDays(1).atTime(MORNING_HOUR, 0);
        }
        return now.toLocalDate().atTime(MORNING_HOUR, 0);
    }

    public static LocalDateTime resolveScheduledAt(NotificationCategory category,
                                                    LocalDateTime now,
                                                    LocalDateTime requestedScheduledAt) {
        if (!category.hasNightRestriction()) {
            return requestedScheduledAt;
        }
        if (!isNightTime(now)) {
            return requestedScheduledAt;
        }
        return calculateNextMorning(now);
    }
}
