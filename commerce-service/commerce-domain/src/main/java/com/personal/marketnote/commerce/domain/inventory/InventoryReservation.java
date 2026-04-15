package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.IdNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class InventoryReservation {
    private Long id;
    private Long orderId;
    private Long pricePolicyId;
    private int quantity;
    private LocalDateTime reservedAt;

    public static InventoryReservation from(InventoryReservationCreateState state) {
        if (FormatValidator.hasNoValue(state.getOrderId())) {
            throw new IdNoValueException("주문 ID는 필수값입니다.");
        }
        if (FormatValidator.hasNoValue(state.getPricePolicyId())) {
            throw new IdNoValueException("가격 정책 ID는 필수값입니다.");
        }
        if (state.getQuantity() <= 0) {
            throw new InvalidQuantityException(
                    String.format("예약 수량은 1 이상이어야 합니다. 전송된 수량: %d", state.getQuantity())
            );
        }
        return InventoryReservation.builder()
                .orderId(state.getOrderId())
                .pricePolicyId(state.getPricePolicyId())
                .quantity(state.getQuantity())
                .reservedAt(state.getReservedAt())
                .build();
    }

    public static InventoryReservation from(InventoryReservationSnapshotState state) {
        return InventoryReservation.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .pricePolicyId(state.getPricePolicyId())
                .quantity(state.getQuantity())
                .reservedAt(state.getReservedAt())
                .build();
    }
}
