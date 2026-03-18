package com.personal.marketnote.product.domain.product;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum ProductSortProperty {
    ORDER_NUM("정렬 순서"),
    POPULARITY("인기도"),
    ACCUMULATED_POINT("적립금"),
    ACCUMULATED_POINT_RATE("적립률"),
    DISCOUNT_PRICE("할인 가격");

    private final String description;
    private final String camelCaseValue;

    ProductSortProperty(String description) {
        this.description = description;
        camelCaseValue = FormatConverter.snakeToCamel(this.name());
    }
}
