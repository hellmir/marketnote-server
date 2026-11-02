package com.personal.marketnote.reward.domain.point;

public class NegativeUserPointAmountException extends IllegalArgumentException {
    private static final String MESSAGE = "포인트는 0 미만이 될 수 없습니다. 차감 후 포인트: %d";

    public NegativeUserPointAmountException(long amount) {
        super(String.format(MESSAGE, amount));
    }
}
