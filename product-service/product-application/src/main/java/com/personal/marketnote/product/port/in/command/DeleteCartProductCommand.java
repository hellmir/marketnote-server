package com.personal.marketnote.product.port.in.command;

import java.util.List;

public record DeleteCartProductCommand(
        Long userId,
        List<Long> pricePolicyIds
) {
    public static DeleteCartProductCommand of(
            Long userId, List<Long> pricePolicyIds
    ) {
        return new DeleteCartProductCommand(userId, pricePolicyIds);
    }
}
