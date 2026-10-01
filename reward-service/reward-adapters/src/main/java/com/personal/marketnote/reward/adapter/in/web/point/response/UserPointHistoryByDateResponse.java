package com.personal.marketnote.reward.adapter.in.web.point.response;

import com.personal.marketnote.reward.port.in.result.point.UserPointHistoryByDateResult;

import java.time.LocalDate;
import java.util.List;

public record UserPointHistoryByDateResponse(LocalDate date, Integer count, List<UserPointHistoryResponse> histories) {
    public static UserPointHistoryByDateResponse from(UserPointHistoryByDateResult result) {
        return new UserPointHistoryByDateResponse(
                result.date(),
                result.count(),
                result.histories().stream()
                        .map(UserPointHistoryResponse::from)
                        .toList()
        );
    }
}

