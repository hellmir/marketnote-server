package com.personal.marketnote.product.port.in.usecase.option;

/**
 * 상품 옵션 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 삭제 기능을 제공합니다.
 */
public interface DeleteProductOptionsUseCase {
    /**
     * @param userId           사용자 ID
     * @param isAdmin          관리자 여부
     * @param productId        상품 ID
     * @param optionCategoryId 옵션 카테고리 ID
     * @Date 2026-02-16
     * @Author 성효빈
     * @Description 상품 옵션을 삭제합니다.
     */
    void deleteProductOptions(
            Long userId, boolean isAdmin, Long productId, Long optionCategoryId
    );
}
