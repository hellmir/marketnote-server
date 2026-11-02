package com.personal.marketnote.product.adapter.out.persistence.fulfillment.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fulfillment_goods_element_read_models")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentGoodsElementReadModelJpaEntity extends BaseGeneralEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_goods_id", nullable = false)
    private FulfillmentGoodsReadModelJpaEntity fulfillmentGoods;

    private String goodsCode;
    private String customerGoodsCode;
    private String goodsBarcode;
    private String goodsName;
    private String goodsType;
    private String goodsTypeName;
    private Integer quantity;

    public static FulfillmentGoodsElementReadModelJpaEntity of(
            String goodsCode, String customerGoodsCode, String goodsBarcode,
            String goodsName, String goodsType, String goodsTypeName, Integer quantity
    ) {
        return FulfillmentGoodsElementReadModelJpaEntity.builder()
                .goodsCode(goodsCode)
                .customerGoodsCode(customerGoodsCode)
                .goodsBarcode(goodsBarcode)
                .goodsName(goodsName)
                .goodsType(goodsType)
                .goodsTypeName(goodsTypeName)
                .quantity(quantity)
                .build();
    }

    public void assignFulfillmentGoods(FulfillmentGoodsReadModelJpaEntity fulfillmentGoods) {
        this.fulfillmentGoods = fulfillmentGoods;
    }
}
