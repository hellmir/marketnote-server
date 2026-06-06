package com.personal.marketnote.fulfillment.domain.shipping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum ShippingStatus {
    PREPARING("배송 준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송 완료"),
    CANCELLED("추적 종료"),
    RETURN_SHIPPING("회수 배송중"),
    RETURN_DELIVERED("회수 완료");

    private final String description;

    private static final Set<ShippingStatus> POLLING_TARGET_STATUSES = EnumSet.of(
            PREPARING, SHIPPING, RETURN_SHIPPING
    );

    private static final Set<ShippingStatus> TERMINAL_STATUSES = EnumSet.of(
            DELIVERED, CANCELLED, RETURN_DELIVERED
    );

    private static final Map<ShippingStatus, Set<ShippingStatus>> ALLOWED_TRANSITIONS = Map.of(
            PREPARING, EnumSet.of(SHIPPING, CANCELLED),
            SHIPPING, EnumSet.of(DELIVERED, CANCELLED),
            DELIVERED, EnumSet.of(RETURN_SHIPPING),
            RETURN_SHIPPING, EnumSet.of(RETURN_DELIVERED),
            CANCELLED, EnumSet.noneOf(ShippingStatus.class),
            RETURN_DELIVERED, EnumSet.noneOf(ShippingStatus.class)
    );

    public boolean isPreparing() {
        return this == PREPARING;
    }

    public boolean isShipping() {
        return this == SHIPPING;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isReturnShipping() {
        return this == RETURN_SHIPPING;
    }

    public boolean isReturnDelivered() {
        return this == RETURN_DELIVERED;
    }

    public boolean isPollingTarget() {
        return POLLING_TARGET_STATUSES.contains(this);
    }

    public boolean isTerminal() {
        return TERMINAL_STATUSES.contains(this);
    }

    public boolean canTransitionTo(ShippingStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(ShippingStatus.class)).contains(target);
    }
}
