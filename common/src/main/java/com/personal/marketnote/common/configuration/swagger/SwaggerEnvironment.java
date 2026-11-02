package com.personal.marketnote.common.configuration.swagger;

import com.personal.marketnote.common.utility.FormatValidator;

public enum SwaggerEnvironment {
    LIVE("prod", "Live"),
    QA("qa.test", "QA");

    private final String profile;
    private final String label;

    SwaggerEnvironment(String profile, String label) {
        this.profile = profile;
        this.label = label;
    }

    public static String resolveLabel(String[] activeProfiles) {
        if (FormatValidator.hasNoValue(activeProfiles) || activeProfiles.length == 0) {
            return "";
        }
        for (SwaggerEnvironment environment : values()) {
            if (containsProfile(activeProfiles, environment.profile)) {
                return environment.label;
            }
        }
        return activeProfiles[0];
    }

    private static boolean containsProfile(String[] activeProfiles, String target) {
        for (String profile : activeProfiles) {
            if (target.equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
