package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.exception.CartProductNotFoundException;
import com.personal.marketnote.product.port.in.result.cart.GetMyCartProductsResult;
import com.personal.marketnote.product.port.in.usecase.cart.GetCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.cart.FindCartProductPort;
import com.personal.marketnote.product.port.out.cart.FindCartProductsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetCartProductService implements GetCartProductUseCase {
    private final FindCartProductPort findCartProductPort;
    private final GetProductInventoryUseCase getProductInventoryUseCase;
    private final FindCartProductsPort findCartProductsPort;

    @Override
    public CartProduct getCartProduct(Long userId, Long pricePolicyId) {
        return findCartProductPort.findCartProductByUserIdAndPricePolicyId(userId, pricePolicyId)
                .orElseThrow(() -> {
                    log.warn("장바구니 상품을 찾을 수 없습니다. userId={}, pricePolicyId={}", userId, pricePolicyId);
                    return new CartProductNotFoundException();
                });
    }

    @Override
    public boolean existsByUserIdAndPolicyId(Long userId, Long policyId) {
        return findCartProductPort.existsByUserIdAndPolicyId(userId, policyId);
    }

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
