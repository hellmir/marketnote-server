package com.personal.marketnote.product.port.in.usecase.pricepolicy;

/**
 * 가격 정책 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 가격 정책 삭제 기능을 제공합니다.
 */
public interface DeletePricePolicyUseCase {
    /**
     * @param userId        사용자 ID
     * @param isAdmin       관리자 여부
     * @param productId     상품 ID
     * @param pricePolicyId 가격 정책 ID
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 가격 정책을 삭제합니다.
     */
    void delete(Long userId, boolean isAdmin, Long productId, Long pricePolicyId);
}
