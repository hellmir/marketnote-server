package com.personal.marketnote.product.adapter.in.web.cart.mapper;

import com.personal.marketnote.product.adapter.in.web.cart.request.AddCartProductRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.UpdateCartProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.UpdateCartProductQuantityRequest;
import com.personal.marketnote.product.port.in.command.AddCartProductCommand;
import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;
import com.personal.marketnote.product.port.in.command.UpdateCartProductOptionCommand;
import com.personal.marketnote.product.port.in.command.UpdateCartProductQuantityCommand;

import java.util.List;

public class CartRequestToCommandMapper {
    public static AddCartProductCommand mapToCommand(Long userId, AddCartProductRequest request) {
        return AddCartProductCommand.builder()
                .userId(userId)
                .sharerKey(request.sharerKey())
                .pricePolicyId(request.pricePolicyId())
                .imageUrl(request.imageUrl())
                .quantity(request.quantity())
                .build();
    }

    public static UpdateCartProductQuantityCommand mapToCommand(Long userId, UpdateCartProductQuantityRequest request) {
        return UpdateCartProductQuantityCommand.of(
                userId,
                request.pricePolicyId(),
                request.newQuantity()
        );
    }

    public static UpdateCartProductOptionCommand mapToCommand(Long userId, UpdateCartProductOptionsRequest request) {
        return UpdateCartProductOptionCommand.of(
                userId,
                request.pricePolicyId(),
                request.newOptionIds()
        );
    }

    public static DeleteCartProductCommand mapToCommand(Long userId, List<Long> pricePolicyIds) {
        return DeleteCartProductCommand.of(userId, pricePolicyIds);
    }
}
