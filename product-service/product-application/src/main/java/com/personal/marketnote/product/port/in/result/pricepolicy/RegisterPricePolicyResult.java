package com.personal.marketnote.product.port.in.result.pricepolicy;

public record RegisterPricePolicyResult(Long id) {
    public static RegisterPricePolicyResult of(Long id) {
        return new RegisterPricePolicyResult(id);
    }
}
