package com.personal.marketnote.reward.port.in.command.point;

import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GetUserPointHistoryCommand(
        Long userId,
        UserPointHistoryFilter filter,
        LocalDate startDate,
        LocalDate endDate,
        Long cursor,
        int pageSize
) {
}
