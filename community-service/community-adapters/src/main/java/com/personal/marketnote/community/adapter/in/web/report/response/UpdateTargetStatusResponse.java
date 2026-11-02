package com.personal.marketnote.community.adapter.in.web.report.response;

public record UpdateTargetStatusResponse(
        boolean isVisible
) {
    public static UpdateTargetStatusResponse of(boolean isVisible) {
        return new UpdateTargetStatusResponse(isVisible);
    }
}
