package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.notification.domain.template.NotificationCategory;

public class PromotionalNotificationPolicy {

    private static final String AD_LABEL_PREFIX = "(광고) ";
    private static final String OPT_OUT_GUIDE_SUFFIX = "\n[수신거부:더보기>설정]";

    private PromotionalNotificationPolicy() {
    }

    public static String applyAdLabel(String title) {
        if (title.startsWith(AD_LABEL_PREFIX)) {
            return title;
        }
        return AD_LABEL_PREFIX + title;
    }

    public static String applyOptOutGuide(String body) {
        if (body.endsWith(OPT_OUT_GUIDE_SUFFIX)) {
            return body;
        }
        return body + OPT_OUT_GUIDE_SUFFIX;
    }

    public static boolean shouldApplyPolicy(NotificationCategory category) {
        return category.requiresAdLabel();
    }
}
