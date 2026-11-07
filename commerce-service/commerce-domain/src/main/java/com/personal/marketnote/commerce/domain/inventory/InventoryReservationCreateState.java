package com.personal.marketnote.commerce.domain.inventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryReservationCreateState {
    private final Long orderId;
    private final Long pricePolicyId;
    private final int quantity;
    private final LocalDateTime reservedAt;

    public static InventoryReservationCreateState of(
            Long orderId, Long pricePolicyId, int quantity, LocalDateTime reservedAt
    ) {
        return InventoryReservationCreateState.builder()
                .orderId(orderId)
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .reservedAt(reservedAt)
                .build();
    }
}
