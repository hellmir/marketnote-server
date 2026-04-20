package com.personal.marketnote.commerce.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum OrderStatus {
    PAYMENT_PENDING("결제 대기"),
    PAID("결제 완료"),
    FAILED("결제 실패"),
    PREPARING("상품 준비중"),
    PREPARED("상품 준비 완료"),
    CANCEL_REQUESTED("주문 취소 요청됨"),
    CANCELLED("주문 취소"),
    SHIPPING("배송중"),
    DELIVERED("배송 완료"),
    PARTIALLY_CONFIRMED("부분 구매 확정"),
    CONFIRMED("구매 확정"),
    REFUND_REQUESTED("환불 요청됨"),
    REFUND_RECALLING("환불 회수 중"),
    REFUND_SHIPPING("환불 배송 중"),
    PARTIALLY_REFUNDED("부분 환불됨"),
    REFUNDED("환불 완료");

    private final String description;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);
    private static final Set<OrderStatus> BUYER_ALLOWED_STATUSES = EnumSet.of(
            CANCEL_REQUESTED, CONFIRMED, REFUND_REQUESTED
    );

    static {
        ALLOWED_TRANSITIONS.put(PAYMENT_PENDING, EnumSet.of(PAID, FAILED, CANCEL_REQUESTED, CANCELLED));
        ALLOWED_TRANSITIONS.put(PAID, EnumSet.of(PREPARING, CANCEL_REQUESTED, CANCELLED));
        ALLOWED_TRANSITIONS.put(FAILED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(PREPARING, EnumSet.of(PREPARED, CANCEL_REQUESTED, CANCELLED));
        ALLOWED_TRANSITIONS.put(PREPARED, EnumSet.of(SHIPPING, CANCEL_REQUESTED, CANCELLED));
        ALLOWED_TRANSITIONS.put(CANCEL_REQUESTED, EnumSet.of(CANCELLED));
        ALLOWED_TRANSITIONS.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(SHIPPING, EnumSet.of(DELIVERED));
        ALLOWED_TRANSITIONS.put(DELIVERED, EnumSet.of(CONFIRMED, PARTIALLY_CONFIRMED, REFUND_REQUESTED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_CONFIRMED, EnumSet.of(CONFIRMED, REFUND_REQUESTED));
        ALLOWED_TRANSITIONS.put(CONFIRMED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(REFUND_REQUESTED, EnumSet.of(REFUND_RECALLING));
        ALLOWED_TRANSITIONS.put(REFUND_RECALLING, EnumSet.of(REFUND_SHIPPING));
        ALLOWED_TRANSITIONS.put(REFUND_SHIPPING, EnumSet.of(REFUNDED, PARTIALLY_REFUNDED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_REFUNDED, EnumSet.of(REFUND_REQUESTED, REFUNDED));
        ALLOWED_TRANSITIONS.put(REFUNDED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(target);
    }

    public boolean isTerminal() {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).isEmpty();
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }

    public static OrderStatus getPartiallyConfirmed() {
        return PARTIALLY_CONFIRMED;
    }

    public static OrderStatus getPartiallyRefunded() {
        return PARTIALLY_REFUNDED;
    }

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isMe(OrderStatus orderStatus) {
        return this == orderStatus;
    }

    public boolean isPending() {
        return this == PAYMENT_PENDING;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    public boolean isShipping() {
        return this == SHIPPING;
    }

    public boolean isRefundRequested() {
        return this == REFUND_REQUESTED;
    }

    public boolean isNotPartialChanged() {
        return this != PARTIALLY_CONFIRMED && this != PARTIALLY_REFUNDED;
    }

    /**
     * 구매자가 직접 변경할 수 있는 주문 상태인지 확인한다.
     * <p>
     * 구매자 허용 상태: CANCEL_REQUESTED, CONFIRMED, REFUND_REQUESTED
     * </p>
     *
     * @return 구매자가 변경 가능한 상태이면 true
     */
    public boolean isBuyerAllowed() {
        return BUYER_ALLOWED_STATUSES.contains(this);
    }
}
