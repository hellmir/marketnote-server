package com.personal.marketnote.product.port.in.command;

import java.util.List;

public record UpdateCartProductOptionCommand(
        Long userId,
        Long pricePolicyId,
        List<Long> newOptionIds
) {

    public static UpdateCartProductOptionCommand of(
            Long userId, Long pricePolicyId, List<Long> newOptionIds
    ) {
        return new UpdateCartProductOptionCommand(userId, pricePolicyId, newOptionIds);
    }
}
