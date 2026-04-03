package com.personal.marketnote.product.mapper;

import com.personal.marketnote.product.domain.cart.CartProductCreateState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.port.in.command.AddCartProductCommand;

public class CartProductCommandToStateMapper {
    public static CartProductCreateState mapToState(
            AddCartProductCommand addCartProductCommand, PricePolicy pricePolicy
    ) {
        return
                CartProductCreateState.builder()
                        .userId(addCartProductCommand.userId())
                        .sharerKey(addCartProductCommand.sharerKey())
                        .pricePolicy(pricePolicy)
                        .imageUrl(addCartProductCommand.imageUrl())
                        .quantity(addCartProductCommand.quantity())
                        .build();
    }
}
