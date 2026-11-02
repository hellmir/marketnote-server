package com.personal.marketnote.product.port.in.command;

public record UpdateCartProductQuantityCommand(
        Long userId,
        Long pricePolicyId,
        Short newQuantity
) {

    public static UpdateCartProductQuantityCommand of(
            Long userId, Long pricePolicyId, Short newQuantity
    ) {
        return new UpdateCartProductQuantityCommand(userId, pricePolicyId, newQuantity);
    }
}
