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
    CANCELLED("주문 취소"),
    SHIPPING("배송중"),
    DELIVERED("배송 완료"),
    PARTIALLY_CONFIRMED("부분 구매 확정"),
    CONFIRMED("구매 확정"),
    RETURN_REQUESTED("반품 요청됨"),
    RETURN_IN_PROGRESS("반품 진행 중"),
    RETURN_INSPECTING("반품 검수 중"),
    PARTIALLY_RETURNED("부분 반품됨"),
    RETURNED("반품 완료"),
    RETURN_REJECTED("반품 불가"),
    RETURN_RESHIPPING_REQUESTED("회송 요청됨"),
    RETURN_RESHIPPING("회송 중");

    private final String description;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);
    private static final Set<OrderStatus> BUYER_ALLOWED_STATUSES = EnumSet.of(
            CONFIRMED, CANCEL_REQUESTED, CANCELLED, RETURN_REQUESTED
    );

    static {
        ALLOWED_TRANSITIONS.put(PAYMENT_PENDING, EnumSet.of(PAID, FAILED, CANCELLED));
        ALLOWED_TRANSITIONS.put(PAID, EnumSet.of(PREPARING, CANCEL_REQUESTED));
        ALLOWED_TRANSITIONS.put(FAILED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(PREPARING, EnumSet.of(SHIPPING, CANCEL_REQUESTED));
        ALLOWED_TRANSITIONS.put(CANCEL_REQUESTED, EnumSet.of(CANCELLED));
        ALLOWED_TRANSITIONS.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(SHIPPING, EnumSet.of(DELIVERED, RETURN_REQUESTED));
        ALLOWED_TRANSITIONS.put(DELIVERED, EnumSet.of(CONFIRMED, RETURN_REQUESTED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_CONFIRMED, EnumSet.of(CONFIRMED, RETURN_REQUESTED));
        ALLOWED_TRANSITIONS.put(CONFIRMED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(RETURN_REQUESTED, EnumSet.of(RETURN_IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(RETURN_IN_PROGRESS, EnumSet.of(RETURNED, RETURN_INSPECTING));
        ALLOWED_TRANSITIONS.put(RETURN_INSPECTING, EnumSet.of(RETURNED, RETURN_REJECTED));
        ALLOWED_TRANSITIONS.put(PARTIALLY_RETURNED, EnumSet.of(RETURN_REQUESTED, RETURNED));
        ALLOWED_TRANSITIONS.put(RETURNED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(RETURN_REJECTED, EnumSet.of(RETURN_RESHIPPING_REQUESTED));
        ALLOWED_TRANSITIONS.put(RETURN_RESHIPPING_REQUESTED, EnumSet.of(RETURN_RESHIPPING));
        ALLOWED_TRANSITIONS.put(RETURN_RESHIPPING, EnumSet.noneOf(OrderStatus.class));
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

    public boolean isReturnInspecting() {
        return this == RETURN_INSPECTING;
    }

    public boolean isReturnRejected() {
        return this == RETURN_REJECTED;
    }

    public boolean isReturnReshippingRequested() {
        return this == RETURN_RESHIPPING_REQUESTED;
    }

    public boolean isReturnReshipping() {
        return this == RETURN_RESHIPPING;
    }

    public boolean requiresFulfillmentCancellation() {
        return this == PREPARING;
    }

    public boolean requiresPaymentRefund() {
        return this == PAID || this == PREPARING;
    }

    public boolean isNotPartialChanged() {
        return this != PARTIALLY_CONFIRMED && this != PARTIALLY_RETURNED;
    }

    /**
     * 구매자가 직접 변경할 수 있는 주문 상태인지 확인한다.
     * <p>
     * 구매자 허용 상태: CONFIRMED, CANCEL_REQUESTED, CANCELLED, RETURN_REQUESTED
     * </p>
     *
     * @return 구매자가 변경 가능한 상태이면 true
     */
    public boolean isBuyerAllowed() {
        return BUYER_ALLOWED_STATUSES.contains(this);
    }
}
