package com.personal.marketnote.notification.domain.aligo;

import java.util.Map;
import java.util.Set;

public enum AligoTemplateInspectionStatus {

    REG("등록"),
    REQ("검수요청"),
    APR("승인"),
    REJ("반려");

    private static final Map<AligoTemplateInspectionStatus, Set<AligoTemplateInspectionStatus>> ALLOWED_TRANSITIONS = Map.of(
            REG, Set.of(REQ),
            REQ, Set.of(APR, REJ),
            REJ, Set.of(REG, REQ),
            APR, Set.of()
    );

    private final String description;

    AligoTemplateInspectionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(AligoTemplateInspectionStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isRegistered() {
        return this == REG;
    }

    public boolean isRequested() {
        return this == REQ;
    }

    public boolean isApproved() {
        return this == APR;
    }

    public boolean isRejected() {
        return this == REJ;
    }

    public boolean canModify() {
        return isRegistered() || isRejected();
    }

    public boolean canDelete() {
        return canModify();
    }

    public boolean canRequestInspection() {
        return canTransitionTo(REQ);
    }

    public static AligoTemplateInspectionStatus from(String code) {
        for (AligoTemplateInspectionStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new InvalidAligoTemplateException("알 수 없는 inspStatus: " + code);
    }
}
