package com.personal.marketnote.product.domain.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSnapshotState {
    private final Long id;
    private final Long sellerId;
    private final String name;
    private final String brandName;
    private final String detail;
    private final PricePolicy defaultPricePolicy;
    private final Integer sales;
    private final Long viewCount;
    private final Long popularity;
    private final boolean findAllOptionsYn;
    private final List<ProductTag> productTags;
    private final Long orderNum;
    private final EntityStatus status;
}

