package com.personal.marketnote.reward.domain.exception;

import lombok.Getter;

@Getter
public class GifticonCouponSendFailedException extends RuntimeException {
    private static final String MESSAGE = "기프티콘 쿠폰 발송에 실패했습니다. errorCode=%s, errorMessage=%s";

    private final String errorCode;

    public GifticonCouponSendFailedException(String errorCode, String errorMessage) {
        super(String.format(MESSAGE, errorCode, errorMessage));
        this.errorCode = errorCode;
    }
}
