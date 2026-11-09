package com.personal.marketnote.product.port.in.result.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductCatalogOptionSnapshot {
    private final Long id;
    private final String content;

    @JsonCreator
    public ProductCatalogOptionSnapshot(
            @JsonProperty("id") Long id,
            @JsonProperty("content") String content
    ) {
        this.id = id;
        this.content = content;
    }

    public Long id() {
        return id;
    }

    public String content() {
        return content;
    }
}
