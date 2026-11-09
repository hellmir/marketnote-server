package com.personal.marketnote.product.port.in.result.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductCatalogTagSnapshot {
    private final Long id;
    private final String name;
    private final Long orderNum;

    @JsonCreator
    public ProductCatalogTagSnapshot(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("orderNum") Long orderNum
    ) {
        this.id = id;
        this.name = name;
        this.orderNum = orderNum;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Long orderNum() {
        return orderNum;
    }
}
