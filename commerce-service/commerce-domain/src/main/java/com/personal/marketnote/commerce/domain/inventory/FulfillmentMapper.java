package com.personal.marketnote.commerce.domain.inventory;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentMapper {
    private Long productId;
    private String wmsKey;
    private String wmsProductKey;
    private Integer stock;

    public static FulfillmentMapper from(FulfillmentMapperCreateState state) {
        return FulfillmentMapper.builder()
                .productId(state.getProductId())
                .wmsKey(state.getWmsKey())
                .wmsProductKey(state.getWmsProductKey())
                .stock(state.getStock())
                .build();
    }

    public static FulfillmentMapper from(FulfillmentMapperSnapshotState state) {
        return FulfillmentMapper.builder()
                .productId(state.getProductId())
                .wmsKey(state.getWmsKey())
                .wmsProductKey(state.getWmsProductKey())
                .stock(state.getStock())
                .build();
    }
}
