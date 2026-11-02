package com.personal.marketnote.common.exception;

import java.io.IOException;

public class RewardServiceRequestFailedException extends ExternalOperationFailedException {
    private static final String REWARD_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE = "리워드 서비스 통신 중 오류가 발생했습니다.";

    public RewardServiceRequestFailedException(IOException cause) {
        super(String.format(REWARD_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE), cause);
    }
}
