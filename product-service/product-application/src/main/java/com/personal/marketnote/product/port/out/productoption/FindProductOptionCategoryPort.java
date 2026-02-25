package com.personal.marketnote.product.port.out.productoption;

import com.personal.marketnote.product.domain.option.ProductOptionCategory;

import java.util.List;

/**
 * 상품 옵션 카테고리 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 카테고리 조회 관련 기능을 제공합니다.
 */
public interface FindProductOptionCategoryPort {
    /**
     * @param productId 상품 ID
     * @return 활성 옵션 카테고리 및 하위 옵션 목록 {@link List<ProductOptionCategory>}
     * @Date 2026-01-01
     * @Author 성효빈
     * @Description 상품 ID로 활성 옵션 카테고리 및 하위 옵션 목록을 조회합니다.
     */
    List<ProductOptionCategory> findActiveWithOptionsByProductId(Long productId);
}
