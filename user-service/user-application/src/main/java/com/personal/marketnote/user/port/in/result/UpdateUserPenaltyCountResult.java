package com.personal.marketnote.user.port.in.result;

public record UpdateUserPenaltyCountResult(Long userId, int penaltyCount, Long historyId) {
    public static UpdateUserPenaltyCountResult of(Long userId, int penaltyCount, Long historyId) {
        return new UpdateUserPenaltyCountResult(userId, penaltyCount, historyId);
    }
}
