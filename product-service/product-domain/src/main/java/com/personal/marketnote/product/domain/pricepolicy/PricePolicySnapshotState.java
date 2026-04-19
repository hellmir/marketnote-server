package com.personal.marketnote.product.domain.pricepolicy;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.product.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PricePolicySnapshotState {
    private final Long id;
    private final Long productId;
    private final Product product;
    private final Long price;
    private final Long discountPrice;
    private final BigDecimal discountRate;
    private final Long accumulatedPoint;
    private final BigDecimal accumulationRate;
    private final Long popularity;
    private final EntityStatus status;
    private final Long orderNum;
    private final List<Long> optionIds;
    private final List<ProductOption> productOptions;
}

