package com.personal.marketnote.product.port.out.productoption;

import com.personal.marketnote.product.domain.option.ProductOptionCategory;

/**
 * 상품 옵션 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 저장 기능을 제공합니다.
 */
public interface SaveProductOptionsPort {
    /**
     * @param productOptionCategory 상품 옵션 카테고리 도메인
     * @return 저장된 상품 옵션 카테고리 도메인 {@link ProductOptionCategory}
     * @Date 2026-01-01
     * @Author 성효빈
     * @Description 상품 옵션 카테고리를 저장합니다.
     */
    ProductOptionCategory save(ProductOptionCategory productOptionCategory);
}
