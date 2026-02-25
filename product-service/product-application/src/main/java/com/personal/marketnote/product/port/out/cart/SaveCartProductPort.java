package com.personal.marketnote.product.port.out.cart;

import com.personal.marketnote.product.domain.cart.CartProduct;

/**
 * 장바구니 상품 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 장바구니 상품 저장 기능을 제공합니다.
 */
public interface SaveCartProductPort {
    /**
     * @param cartProduct 저장할 장바구니 상품
     * @return 저장된 장바구니 상품 {@link CartProduct}
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 장바구니 상품을 저장합니다.
     */
    CartProduct save(CartProduct cartProduct);
}
