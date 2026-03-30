package com.personal.marketnote.fulfillment.domain.goods;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentGoodsRegistration {
    private Long id;
    private Long productId;
    private LocalDateTime createdAt;

    public static FulfillmentGoodsRegistration from(FulfillmentGoodsRegistrationCreateState state) {
        if (FormatValidator.hasNoValue(state.getProductId())) {
            throw new FulfillmentQueryParameterNoValueException("productId");
        }
        return FulfillmentGoodsRegistration.builder()
                .productId(state.getProductId())
                .build();
    }

    public static FulfillmentGoodsRegistration from(FulfillmentGoodsRegistrationSnapshotState state) {
        return FulfillmentGoodsRegistration.builder()
                .id(state.getId())
                .productId(state.getProductId())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
