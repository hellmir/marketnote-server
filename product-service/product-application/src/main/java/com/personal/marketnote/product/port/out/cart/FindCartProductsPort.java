package com.personal.marketnote.product.port.out.cart;

import com.personal.marketnote.product.domain.cart.CartProduct;

import java.util.List;

/**
 * 장바구니 상품 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 장바구니 상품 목록 조회 기능을 제공합니다.
 */
public interface FindCartProductsPort {
    /**
     * @param userId 사용자 ID
     * @return 장바구니 상품 목록 {@link List}&lt;{@link CartProduct}&gt;
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 사용자 ID로 장바구니 상품 목록을 조회합니다.
     */
    List<CartProduct> findByUserId(Long userId);
}
