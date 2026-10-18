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
    CANCEL_REQUESTED("취소 요청"),
    PARTIALLY_CANCEL_REQUESTED("부분 취소 요청"),
    CANCELLED("주문 취소"),
    SHIPPING("배송중"),
    DELIVERED("배송 완료"),
    PARTIALLY_CONFIRMED("부분 구매 확정"),
    CONFIRMED("구매 확정"),
    RETURN_REQUESTED("반품 요청됨"),
    RETURN_REJECTED("반품 불가"),
    RETURN_IN_PROGRESS("반품 진행 중"),
    PARTIALLY_CANCELLED("부분 취소"),
    PARTIALLY_RETURNED("부분 반품됨"),
    RETURNED("반품 완료");

    private final String description;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PAYMENT_PENDING, EnumSet.of(PAID, FAILED, CANCELLED, PARTIALLY_CANCELLED));
        ALLOWED_TRANSITIONS.put(PAID, EnumSet.of(PREPARING, CANCEL_REQUESTED, PARTIALLY_CANCEL_REQUESTED, CANCELLED, PARTIALLY_CANCELLED));
        ALLOWED_TRANSITIONS.put(FAILED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(PREPARING, EnumSet.of(SHIPPING, CANCEL_REQUESTED, PARTIALLY_CANCEL_REQUESTED, CANCELLED, PARTIALLY_CANCELLED));
        ALLOWED_TRANSITIONS.put(CANCEL_REQUESTED, EnumSet.of(CANCELLED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_CANCEL_REQUESTED, EnumSet.of(CANCELLED, PARTIALLY_CANCELLED, CANCEL_REQUESTED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_CANCELLED, EnumSet.of(CANCEL_REQUESTED, PARTIALLY_CANCEL_REQUESTED, CANCELLED, PARTIALLY_CANCELLED));
        ALLOWED_TRANSITIONS.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(SHIPPING, EnumSet.of(DELIVERED, RETURN_REQUESTED));
        ALLOWED_TRANSITIONS.put(DELIVERED, EnumSet.of(CONFIRMED, RETURN_REQUESTED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_CONFIRMED, EnumSet.of(CONFIRMED, RETURN_REQUESTED));
        ALLOWED_TRANSITIONS.put(CONFIRMED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(RETURN_REQUESTED, EnumSet.of(RETURN_IN_PROGRESS, RETURN_REJECTED));
        ALLOWED_TRANSITIONS.put(RETURN_REJECTED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(RETURN_IN_PROGRESS, EnumSet.of(RETURNED, RETURN_REJECTED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_RETURNED, EnumSet.of(RETURN_REQUESTED, RETURNED));
        ALLOWED_TRANSITIONS.put(RETURNED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(target);
    }

    public boolean isTerminal() {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).isEmpty();
    }

    public boolean isReturned() {
        return this == RETURNED;
    }

    public static OrderStatus getPartiallyConfirmed() {
        return PARTIALLY_CONFIRMED;
    }

    public static OrderStatus getPartiallyReturned() {
        return PARTIALLY_RETURNED;
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

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    public boolean isShipping() {
        return this == SHIPPING;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean isPreparing() {
        return this == PREPARING;
    }

    public boolean isCancelRequested() {
        return this == CANCEL_REQUESTED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isReturnRequested() {
        return this == RETURN_REQUESTED;
    }

    public boolean isReturnRejected() {
        return this == RETURN_REJECTED;
    }

    public boolean requiresFulfillmentCancellation() {
        return this == PREPARING;
    }

    public boolean requiresPaymentRefund() {
        return this == PAID || this == PREPARING;
    }

    public boolean isPartiallyCancelled() {
        return this == PARTIALLY_CANCELLED;
    }

    public boolean isPartiallyCancelRequested() {
        return this == PARTIALLY_CANCEL_REQUESTED;
    }

    public static OrderStatus getPartiallyCancelled() {
        return PARTIALLY_CANCELLED;
    }

    public static OrderStatus getPartiallyCancelRequested() {
        return PARTIALLY_CANCEL_REQUESTED;
    }

    public boolean canPartiallyCancel() {
        return this == PAYMENT_PENDING || this == PAID || this == PREPARING || this == PARTIALLY_CANCELLED;
    }

    public boolean isNotPartialChanged() {
        return this != PARTIALLY_CONFIRMED
                && this != PARTIALLY_RETURNED
                && this != PARTIALLY_CANCELLED
                && this != PARTIALLY_CANCEL_REQUESTED;
    }

    public boolean isReviewable() {
        return this == DELIVERED
                || this == PARTIALLY_CONFIRMED
                || this == CONFIRMED
                || this == PARTIALLY_RETURNED
                || this == RETURNED;
    }
}
