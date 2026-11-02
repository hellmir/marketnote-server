package com.personal.marketnote.product.domain.option;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOptionCreateState {
    private final ProductOptionCategory category;
    private final String content;
}

