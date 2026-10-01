package com.personal.marketnote.reward.port.in.result.point;

import com.personal.marketnote.reward.domain.point.UserPointHistory;

import java.time.LocalDate;
import java.util.List;

public record UserPointHistoryByDateResult(LocalDate date, Integer count, List<UserPointHistoryResult> histories) {
    public static UserPointHistoryByDateResult of(LocalDate date, List<UserPointHistory> histories) {
        List<UserPointHistoryResult> historyResults = histories.stream()
                .map(UserPointHistoryResult::from)
                .toList();

        return new UserPointHistoryByDateResult(date, historyResults.size(), historyResults);
    }
}

