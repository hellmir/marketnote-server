package com.personal.marketnote.fulfillment.domain.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentDeliveryRegistration {
    private Long id;
    private Long orderId;
    private LocalDateTime createdAt;

    public static FulfillmentDeliveryRegistration from(FulfillmentDeliveryRegistrationCreateState state) {
        if (FormatValidator.hasNoValue(state.getOrderId())) {
            throw new FulfillmentQueryParameterNoValueException("orderId");
        }
        return FulfillmentDeliveryRegistration.builder()
                .orderId(state.getOrderId())
                .build();
    }

    public static FulfillmentDeliveryRegistration from(FulfillmentDeliveryRegistrationSnapshotState state) {
        return FulfillmentDeliveryRegistration.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
