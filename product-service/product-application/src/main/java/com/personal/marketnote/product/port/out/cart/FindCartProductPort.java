package com.personal.marketnote.product.port.out.cart;

import com.personal.marketnote.product.domain.cart.CartProduct;

import java.util.Optional;

/**
 * 장바구니 상품 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 장바구니 상품 조회 관련 기능을 제공합니다.
 */
public interface FindCartProductPort {
    /**
     * @param userId        사용자 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 장바구니 상품 {@link Optional}&lt;{@link CartProduct}&gt;
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 사용자 ID와 가격 정책 ID로 장바구니 상품을 조회합니다.
     */
    Optional<CartProduct> findCartProductByUserIdAndPricePolicyId(Long userId, Long pricePolicyId);

    /**
     * @param userId        사용자 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 장바구니 상품 존재 여부 {@link boolean}
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 사용자 ID와 가격 정책 ID로 장바구니 상품의 존재 여부를 확인합니다.
     */
    boolean existsByUserIdAndPolicyId(Long userId, Long pricePolicyId);
}
