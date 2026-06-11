package com.personal.marketnote.notification.domain.template;

public enum NotificationCategory {
    MANDATORY("필수", false, false, false),
    INFORMATIONAL("정보성", false, false, false),
    PROMOTIONAL("광고성", true, true, true);

    private final String description;
    private final boolean consentRequired;
    private final boolean nightRestricted;
    private final boolean adLabelRequired;

    NotificationCategory(String description, boolean consentRequired,
                          boolean nightRestricted, boolean adLabelRequired) {
        this.description = description;
        this.consentRequired = consentRequired;
        this.nightRestricted = nightRestricted;
        this.adLabelRequired = adLabelRequired;
    }

    public boolean requiresConsent() {
        return consentRequired;
    }

    public boolean hasNightRestriction() {
        return nightRestricted;
    }

    public boolean requiresAdLabel() {
        return adLabelRequired;
    }

    public String getDescription() {
        return description;
    }
}
