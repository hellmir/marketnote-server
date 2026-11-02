package com.personal.marketnote.reward.domain.exception;

import lombok.Getter;

@Getter
public class GifticonInsufficientCashException extends RuntimeException {
    private static final String MESSAGE = "캐시가 부족합니다. 부족 금액=%d";

    private final Long shortfallAmount;

    public GifticonInsufficientCashException(Long shortfallAmount) {
        super(String.format(MESSAGE, shortfallAmount));
        this.shortfallAmount = shortfallAmount;
    }
}
