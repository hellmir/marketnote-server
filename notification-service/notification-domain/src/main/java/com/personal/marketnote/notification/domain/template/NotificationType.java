package com.personal.marketnote.notification.domain.template;

public enum NotificationType {
    ORDER_PAYMENT_COMPLETED("주문 결제 완료", true),
    SHIPPING_STARTED("배송 시작", true),
    SHIPPING_COMPLETED("배송 완료", true),
    SHIPPING_DELAYED("배송 지연", true),
    ORDER_CANCELLED("주문 취소 완료", true),
    ORDER_CANCEL_FAILED("주문 취소 실패", true),
    RETURN_REQUESTED("반품 신청", true),
    RETURN_COMPLETED("반품 완료", true),
    RETURN_REJECTED("반품 불가", true),
    PURCHASE_CONFIRMATION_REQUEST("구매 확정 요청", true),
    PURCHASE_CONFIRMATION_COMPLETED("구매 확정 완료", true),
    REVIEW_WRITE_REQUEST("리뷰 작성 요청", true),
    REVIEW_REPLY("리뷰 답글", true),
    NOTICE("공지사항", true),
    EVENT("이벤트", true),
    ONE_ON_ONE_INQUIRY_REPLY("1:1 문의 답변", true),
    PRODUCT_INQUIRY_REPLY("상품 문의 답변", true),
    POINT_ACCRUAL("포인트 적립", true),
    CONSENT_REMINDER("수신 동의 재고지", true),
    MARKETING("마케팅", false);

    private final String description;
    private final boolean defaultEnabled;

    NotificationType(String description, boolean defaultEnabled) {
        this.description = description;
        this.defaultEnabled = defaultEnabled;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
}
