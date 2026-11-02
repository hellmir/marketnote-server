package com.personal.marketnote.user.port.in.command;

import java.time.LocalDateTime;

public record ChangeUserStatusCommand(
        Long userId,
        Long adminId,
        String action,
        String reason,
        LocalDateTime deactivatedUntil
) {
}
