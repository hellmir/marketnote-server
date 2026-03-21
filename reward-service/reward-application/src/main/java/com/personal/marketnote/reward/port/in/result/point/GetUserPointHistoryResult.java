package com.personal.marketnote.reward.port.in.result.point;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserPointHistoryResult(
        Long totalElements,
        boolean hasNext,
        Long nextCursor,
        List<UserPointHistoryByDateResult> histories
) {
    public static GetUserPointHistoryResult from(
            Long totalElements,
            boolean hasNext,
            Long nextCursor,
            List<UserPointHistory> histories
    ) {
        Map<LocalDate, List<UserPointHistory>> historiesByDate = new LinkedHashMap<>();

        for (UserPointHistory history : histories) {
            LocalDate date = FormatValidator.hasValue(history.getAccumulatedAt())
                    ? history.getAccumulatedAt().toLocalDate()
                    : LocalDate.now();

            historiesByDate.computeIfAbsent(date, key -> new ArrayList<>())
                    .add(history);
        }

        List<UserPointHistoryByDateResult> groupedHistories = historiesByDate.entrySet().stream()
                .map(entry -> UserPointHistoryByDateResult.of(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();

        return new GetUserPointHistoryResult(totalElements, hasNext, nextCursor, groupedHistories);
    }
}
