package com.personal.marketnote.product.port.in.command;

import java.util.List;

public record GetMyOrderingProductsQuery(
        List<OrderingItemQuery> orderingItemQueries
) {
    public static GetMyOrderingProductsQuery from(List<OrderingItemQuery> queries) {
        return new GetMyOrderingProductsQuery(queries);
    }
}
