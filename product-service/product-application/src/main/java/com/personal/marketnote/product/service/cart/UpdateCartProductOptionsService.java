package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.exception.CartProductAlreadyExistsException;
import com.personal.marketnote.product.port.in.command.UpdateCartProductOptionCommand;
import com.personal.marketnote.product.port.in.usecase.cart.GetCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.cart.UpdateCartProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePolicyUseCase;
import com.personal.marketnote.product.port.out.cart.UpdateCartProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateCartProductOptionsService implements UpdateCartProductOptionsUseCase {
    private final GetCartProductUseCase getCartProductUseCase;
    private final GetPricePolicyUseCase getPricePolicyUseCase;
    private final UpdateCartProductPort updateCartProductPort;

    @Override
    public void updateCartProductOptions(UpdateCartProductOptionCommand command) {
        Long userId = command.userId();
        Long originalPolicyId = command.pricePolicyId();
        CartProduct cartProduct = getCartProductUseCase.getCartProduct(userId, originalPolicyId);

        PricePolicy pricePolicy = getPricePolicyUseCase.getPricePolicy(command.newOptionIds());
        cartProduct.updatePricePolicy(pricePolicy);

        Long newPolicyId = pricePolicy.getId();
        boolean pricePolicyChanged = !originalPolicyId.equals(newPolicyId);
        if (pricePolicyChanged && getCartProductUseCase.existsByUserIdAndPolicyId(userId, newPolicyId)) {
            throw new CartProductAlreadyExistsException(userId, newPolicyId);
        }

        updateCartProductPort.update(cartProduct, originalPolicyId);
    }
}
