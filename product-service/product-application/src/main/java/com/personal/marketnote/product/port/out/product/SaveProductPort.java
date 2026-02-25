package com.personal.marketnote.product.port.out.product;

import com.personal.marketnote.product.domain.product.Product;

/**
 * 상품 저장 포트
 *
 * @Author 성효빈
 * @Date 2025-12-30
 * @Description 상품 저장 기능을 제공합니다.
 */
public interface SaveProductPort {
    /**
     * @param product 상품 도메인
     * @return 저장된 상품 도메인 {@link Product}
     * @Date 2025-12-30
     * @Author 성효빈
     * @Description 상품을 저장합니다.
     */
    Product save(Product product);
}
