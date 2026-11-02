package com.personal.marketnote.community.exception;

import lombok.Getter;

@Getter
public class ReviewAlreadyExistsException extends IllegalStateException {
    private static final String REVIEW_ALREADY_EXISTS_EXCEPTION_MESSAGE
            = "이미 해당 주문에 대한 리뷰가 존재합니다. 전송된 주문 ID: %d, 가격 정책 ID: %d";

    public ReviewAlreadyExistsException(Long orderId, Long pricePolicyId) {
        super(String.format(REVIEW_ALREADY_EXISTS_EXCEPTION_MESSAGE, orderId, pricePolicyId));
    }
}
