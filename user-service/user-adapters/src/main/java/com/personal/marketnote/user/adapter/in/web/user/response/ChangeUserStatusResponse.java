package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.ChangeUserStatusResult;

import java.time.LocalDateTime;

public record ChangeUserStatusResponse(Long userId, String status, LocalDateTime deactivatedUntil, Long historyId) {
    public static ChangeUserStatusResponse from(ChangeUserStatusResult result) {
        return new ChangeUserStatusResponse(
                result.userId(),
                result.status(),
                result.deactivatedUntil(),
                result.historyId()
        );
    }
}
