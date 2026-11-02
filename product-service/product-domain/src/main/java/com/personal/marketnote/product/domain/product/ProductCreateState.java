package com.personal.marketnote.product.domain.product;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCreateState {
    private final Long sellerId;
    private final String name;
    private final String brandName;
    private final String detail;
    private final boolean findAllOptionsYn;
    private final List<ProductTagCreateState> tags;
}

