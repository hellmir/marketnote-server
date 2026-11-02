package com.personal.marketnote.commerce.port.in.result.order;

public record GetOrderCountResult(long totalCount) {
    public static GetOrderCountResult of(long totalCount) {
        return new GetOrderCountResult(totalCount);
    }
}
