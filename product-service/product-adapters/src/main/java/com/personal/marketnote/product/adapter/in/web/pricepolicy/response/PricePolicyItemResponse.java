package com.personal.marketnote.product.adapter.in.web.pricepolicy.response;

import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyWithOptionsResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PricePolicyItemResponse {
    private Long id;
    private Long price;
    private Long discountPrice;
    private Long accumulatedPoint;
    private BigDecimal discountRate;
    private boolean basePolicy;
    private List<Long> optionIds;

    public static PricePolicyItemResponse from(GetProductPricePolicyWithOptionsResult result) {
        return PricePolicyItemResponse.builder()
                .id(result.id())
                .price(result.price())
                .discountPrice(result.discountPrice())
                .accumulatedPoint(result.accumulatedPoint())
                .discountRate(result.discountRate())
                .optionIds(result.optionIds())
                .build();
    }
}


