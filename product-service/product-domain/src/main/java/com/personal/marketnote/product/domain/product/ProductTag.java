package com.personal.marketnote.product.domain.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductTag {
    private Long id;
    private Long productId;
    private String name;
    private Long orderNum;
    private EntityStatus status;

    @JsonCreator
    private static ProductTag jsonCreator(
            @JsonProperty("id") Long id,
            @JsonProperty("productId") Long productId,
            @JsonProperty("name") String name,
            @JsonProperty("orderNum") Long orderNum,
            @JsonProperty("status") EntityStatus status
    ) {
        return new ProductTag(id, productId, name, orderNum, status);
    }

    public static ProductTag from(ProductTagCreateState state) {
        return ProductTag.builder()
                .productId(state.getProductId())
                .name(state.getName())
                .status(EntityStatus.ACTIVE)
                .build();
    }

    public static ProductTag from(ProductTagSnapshotState state) {
        return ProductTag.builder()
                .id(state.getId())
                .productId(state.getProductId())
                .name(state.getName())
                .orderNum(state.getOrderNum())
                .status(state.getStatus())
                .build();
    }
}
