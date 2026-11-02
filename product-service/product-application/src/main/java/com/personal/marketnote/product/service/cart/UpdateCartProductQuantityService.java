package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.port.in.command.UpdateCartProductQuantityCommand;
import com.personal.marketnote.product.port.in.usecase.cart.GetCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.cart.UpdateCartProductQuantityUseCase;
import com.personal.marketnote.product.port.out.cart.UpdateCartProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateCartProductQuantityService implements UpdateCartProductQuantityUseCase {
    private final GetCartProductUseCase getCartProductUseCase;
    private final UpdateCartProductPort updateCartProductPort;

    @Override
    public void updateCartProductQuantity(UpdateCartProductQuantityCommand command) {
        CartProduct cartProduct = getCartProductUseCase.getCartProduct(command.userId(), command.pricePolicyId());
        cartProduct.updateQuantity(command.newQuantity());
        updateCartProductPort.update(cartProduct, command.pricePolicyId());
    }
}
