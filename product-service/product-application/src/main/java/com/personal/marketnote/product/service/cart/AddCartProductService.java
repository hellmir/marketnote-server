package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.mapper.CartProductCommandToStateMapper;
import com.personal.marketnote.product.port.in.command.AddCartProductCommand;
import com.personal.marketnote.product.port.in.usecase.cart.AddCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePolicyUseCase;
import com.personal.marketnote.product.port.out.cart.FindCartProductPort;
import com.personal.marketnote.product.port.out.cart.SaveCartProductPort;
import com.personal.marketnote.product.port.out.cart.UpdateCartProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class AddCartProductService implements AddCartProductUseCase {
    private final GetPricePolicyUseCase getPricePolicyUseCase;
    private final FindCartProductPort findCartProductPort;
    private final UpdateCartProductPort updateCartProductPort;
    private final SaveCartProductPort saveCartProductPort;

    @Override
    public void addCartProduct(AddCartProductCommand command) {
        PricePolicy pricePolicy = getPricePolicyUseCase.getPricePolicy(command.pricePolicyId());

        Optional<CartProduct> existingCartProduct
                = findCartProductPort.findCartProductByUserIdAndPricePolicyId(command.userId(), command.pricePolicyId());

        if (existingCartProduct.isPresent()) {
            CartProduct cartProduct = existingCartProduct.get();
            cartProduct.addQuantity(command.quantity());
            updateCartProductPort.update(cartProduct, command.pricePolicyId());
            return;
        }

        saveCartProductPort.save(
                CartProduct.from(CartProductCommandToStateMapper.mapToState(command, pricePolicy))
        );
    }
}
