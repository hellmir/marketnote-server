package com.personal.marketnote.commerce.exception;

import lombok.Getter;

@Getter
public class TrackingInfoRequiredException extends IllegalStateException {
    private static final String TRACKING_INFO_REQUIRED_EXCEPTION_MESSAGE
            = "배송 상태로 변경하려면 송장 정보(택배사, 송장번호)가 필수입니다. orderId=%d";

    public TrackingInfoRequiredException(Long orderId) {
        super(String.format(TRACKING_INFO_REQUIRED_EXCEPTION_MESSAGE, orderId));
    }
}
