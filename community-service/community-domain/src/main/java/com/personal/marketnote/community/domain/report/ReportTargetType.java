package com.personal.marketnote.community.domain.report;

import lombok.Getter;

@Getter
public enum ReportTargetType {
    REVIEW("리뷰"),
    POST("게시글");

    private final String description;

    ReportTargetType(String description) {
        this.description = description;
    }

    public boolean isReview() {
        return this == REVIEW;
    }
}
