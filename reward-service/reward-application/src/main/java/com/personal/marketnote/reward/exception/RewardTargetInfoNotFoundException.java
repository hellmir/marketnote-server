package com.personal.marketnote.reward.exception;

import lombok.Getter;

@Getter
public class RewardTargetInfoNotFoundException extends IllegalStateException {
    public RewardTargetInfoNotFoundException(String message) {
        super(message);
    }
}
