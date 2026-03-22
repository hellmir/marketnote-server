package com.personal.marketnote.fulfillment.domain.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FasstoDeliveryRegistration {
    private Long id;
    private Long orderId;
    private LocalDateTime createdAt;

    public static FasstoDeliveryRegistration from(FasstoDeliveryRegistrationCreateState state) {
        if (FormatValidator.hasNoValue(state.getOrderId())) {
            throw new FasstoQueryParameterNoValueException("orderId");
        }
        return FasstoDeliveryRegistration.builder()
                .orderId(state.getOrderId())
                .build();
    }

    public static FasstoDeliveryRegistration from(FasstoDeliveryRegistrationSnapshotState state) {
        return FasstoDeliveryRegistration.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
