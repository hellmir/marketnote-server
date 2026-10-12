package com.personal.marketnote.community.exception;

/**
 * 리뷰 작성이 불가능한 주문 상태일 때 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-10-11
 * @Description 배송 완료 이전 상태의 주문에 리뷰를 작성하려 할 때 발생합니다.
 */
public class InvalidOrderStatusForReviewException extends IllegalStateException {
    private static final String INVALID_ORDER_STATUS_FOR_REVIEW_EXCEPTION_MESSAGE
            = "ERR_REVIEW_02::리뷰 작성이 불가능한 주문 상태입니다.";

    public InvalidOrderStatusForReviewException() {
        super(INVALID_ORDER_STATUS_FOR_REVIEW_EXCEPTION_MESSAGE);
    }
}
