package com.personal.marketnote.product.port.out.productcategory;

/**
 * 상품 카테고리 조회 포트
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 상품 카테고리 조회 관련 기능을 제공합니다.
 */
public interface FindProductCategoryPort {
    /**
     * @param categoryId 카테고리 ID
     * @return 카테고리 존재 여부 {@link boolean}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리 ID로 상품 카테고리의 존재 여부를 확인합니다.
     */
    boolean existsByCategoryId(Long categoryId);
}
