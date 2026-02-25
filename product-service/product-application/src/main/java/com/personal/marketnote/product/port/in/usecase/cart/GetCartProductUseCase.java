package com.personal.marketnote.product.port.in.usecase.cart;

import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.port.in.result.cart.GetMyCartProductsResult;

/**
 * 장바구니 상품 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 장바구니 상품 조회 관련 기능을 제공합니다.
 */
public interface GetCartProductUseCase {
    /**
     * @param userId   회원 ID
     * @param policyId 가격 정책 ID
     * @return 장바구니 상품 존재 여부
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 회원의 장바구니 상품 존재 여부를 조회합니다.
     */
    boolean existsByUserIdAndPolicyId(Long userId, Long policyId);

    /**
     * @param userId        회원 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 장바구니 상품 {@link CartProduct}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 회원의 장바구니 상품을 조회합니다.
     */
    CartProduct getCartProduct(Long userId, Long pricePolicyId);

    /**
     * @param userId 회원 ID
     * @return 장바구니 상품 목록 {@link GetMyCartProductsResult}
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 회원의 장바구니 상품 목록을 조회합니다.
     */
    GetMyCartProductsResult getMyCartProducts(Long userId);
}
