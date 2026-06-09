package com.personal.marketnote.notification.domain.template;

public enum NotificationType {
    ORDER_PAYMENT_COMPLETED("주문 결제 완료"),
    SHIPPING_STARTED("배송 시작"),
    SHIPPING_COMPLETED("배송 완료"),
    SHIPPING_DELAYED("배송 지연"),
    ORDER_CANCELLED("주문 취소 완료"),
    ORDER_CANCEL_FAILED("주문 취소 실패"),
    RETURN_REQUESTED("반품 신청"),
    RETURN_COMPLETED("반품 완료"),
    RETURN_REJECTED("반품 불가"),
    PURCHASE_CONFIRMATION_REQUEST("구매 확정 요청"),
    PURCHASE_CONFIRMATION_COMPLETED("구매 확정 완료"),
    REVIEW_WRITE_REQUEST("리뷰 작성 요청"),
    REVIEW_REPLY("리뷰 답글"),
    NOTICE("공지사항"),
    EVENT("이벤트"),
    ONE_ON_ONE_INQUIRY_REPLY("1:1 문의 답변"),
    PRODUCT_INQUIRY_REPLY("상품 문의 답변"),
    POINT_ACCRUAL("포인트 적립");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
