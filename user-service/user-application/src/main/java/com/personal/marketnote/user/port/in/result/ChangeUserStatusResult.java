package com.personal.marketnote.user.port.in.result;

import java.time.LocalDateTime;

public record ChangeUserStatusResult(
        Long userId,
        String status,
        LocalDateTime deactivatedUntil,
        Long historyId
) {
    public static ChangeUserStatusResult of(Long userId, String status, LocalDateTime deactivatedUntil, Long historyId) {
        return new ChangeUserStatusResult(userId, status, deactivatedUntil, historyId);
    }
}
