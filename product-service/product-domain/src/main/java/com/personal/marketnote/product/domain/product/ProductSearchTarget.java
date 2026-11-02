package com.personal.marketnote.product.domain.product;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum ProductSearchTarget {
    NAME("상품명"),
    BRAND_NAME("브랜드명");

    private final String description;
    private final String camelCaseValue;

    ProductSearchTarget(String description) {
        this.description = description;
        camelCaseValue = FormatConverter.snakeToCamel(this.name());
    }
}
