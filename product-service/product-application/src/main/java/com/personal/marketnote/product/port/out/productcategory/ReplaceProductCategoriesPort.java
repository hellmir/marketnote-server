package com.personal.marketnote.product.port.out.productcategory;

import java.util.List;

/**
 * 상품 카테고리 교체 포트
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 상품 카테고리 교체 기능을 제공합니다.
 */
public interface ReplaceProductCategoriesPort {
    /**
     * @param productId   상품 ID
     * @param categoryIds 카테고리 ID 목록
     * @return void
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 상품의 카테고리를 교체합니다.
     */
    void replaceProductCategories(Long productId, List<Long> categoryIds);
}
