package com.personal.marketnote.product.port.out.cart;

import java.util.List;

/**
 * 장바구니 상품 삭제 포트
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 장바구니 상품 삭제 기능을 제공합니다.
 */
public interface DeleteCartProductPort {
    /**
     * @param userId         사용자 ID
     * @param pricePolicyIds 삭제할 가격 정책 ID 목록
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 사용자의 장바구니에서 지정된 가격 정책에 해당하는 상품을 삭제합니다.
     */
    void delete(Long userId, List<Long> pricePolicyIds);

    /**
     * @param userId 사용자 ID
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 사용자의 장바구니에 있는 모든 상품을 삭제합니다.
     */
    void deleteAll(Long userId);
}
