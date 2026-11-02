package com.personal.marketnote.reward.exception;

import lombok.Getter;

@Getter
public class DuplicateOfferwallRewardException extends IllegalStateException {
    private static final String DUPLICATE_OFFERWALL_REWARD_EXCEPTION_MESSAGE = "이미 해당 캠페인에 대한 리워드가 지급되었습니다. 전송된 캠페인 ID: %s";

    public DuplicateOfferwallRewardException(String campaignId) {
        super(String.format(DUPLICATE_OFFERWALL_REWARD_EXCEPTION_MESSAGE, campaignId));
    }
}
