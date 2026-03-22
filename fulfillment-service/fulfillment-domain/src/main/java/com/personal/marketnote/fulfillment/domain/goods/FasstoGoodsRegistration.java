package com.personal.marketnote.fulfillment.domain.goods;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FasstoGoodsRegistration {
    private Long id;
    private Long productId;
    private LocalDateTime createdAt;

    public static FasstoGoodsRegistration from(FasstoGoodsRegistrationCreateState state) {
        if (FormatValidator.hasNoValue(state.getProductId())) {
            throw new FasstoQueryParameterNoValueException("productId");
        }
        return FasstoGoodsRegistration.builder()
                .productId(state.getProductId())
                .build();
    }

    public static FasstoGoodsRegistration from(FasstoGoodsRegistrationSnapshotState state) {
        return FasstoGoodsRegistration.builder()
                .id(state.getId())
                .productId(state.getProductId())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
