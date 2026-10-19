package com.personal.marketnote.fulfillment.domain.goods;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentGoodsRegistration {
    private Long id;
    private UUID productKey;
    private LocalDateTime createdAt;

    public static FulfillmentGoodsRegistration from(FulfillmentGoodsRegistrationCreateState state) {
        if (FormatValidator.hasNoValue(state.getProductKey())) {
            throw new FulfillmentQueryParameterNoValueException("productKey");
        }
        return FulfillmentGoodsRegistration.builder()
                .productKey(state.getProductKey())
                .build();
    }

    public static FulfillmentGoodsRegistration from(FulfillmentGoodsRegistrationSnapshotState state) {
        return FulfillmentGoodsRegistration.builder()
                .id(state.getId())
                .productKey(state.getProductKey())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
