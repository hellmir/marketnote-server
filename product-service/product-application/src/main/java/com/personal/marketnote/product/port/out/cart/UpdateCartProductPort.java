package com.personal.marketnote.product.port.out.cart;

import com.personal.marketnote.product.domain.cart.CartProduct;

/**
 * 장바구니 상품 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 장바구니 상품 수정 기능을 제공합니다.
 */
public interface UpdateCartProductPort {
    /**
     * @param cartProduct           수정할 장바구니 상품
     * @param originalPricePolicyId 기존 가격 정책 ID
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 장바구니 상품을 수정합니다.
     */
    void update(CartProduct cartProduct, Long originalPricePolicyId);
}
