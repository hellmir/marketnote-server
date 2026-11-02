package com.personal.marketnote.user.port.in.result;

public record ApplyUserPenaltyResult(Long userId, int penaltyCount, Long historyId) {
    public static ApplyUserPenaltyResult of(Long userId, int penaltyCount, Long historyId) {
        return new ApplyUserPenaltyResult(userId, penaltyCount, historyId);
    }
}
