package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.port.in.result.cart.GetMyCartProductsResult;
import com.personal.marketnote.product.port.in.usecase.cart.GetMyCartProductsUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.cart.FindCartProductsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetMyCartProductsService implements GetMyCartProductsUseCase {
    private final GetProductInventoryUseCase getProductInventoryUseCase;
    private final FindCartProductsPort findCartProductsPort;

    @Override
    public GetMyCartProductsResult getMyCartProducts(Long userId) {
        List<CartProduct> cartProducts = findCartProductsPort.findByUserId(userId);
        Map<Long, Integer> inventories = getProductInventoryUseCase.getProductStocks(
                cartProducts.stream()
                        .map(CartProduct::getPricePolicyId)
                        .toList()
        );

        return GetMyCartProductsResult.from(cartProducts, inventories);
    }
}
