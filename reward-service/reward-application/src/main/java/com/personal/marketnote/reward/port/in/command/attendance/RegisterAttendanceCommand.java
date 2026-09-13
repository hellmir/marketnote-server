package com.personal.marketnote.reward.port.in.command.attendance;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RegisterAttendanceCommand(
        Long userId,
        LocalDateTime attendedAt
) {
}
