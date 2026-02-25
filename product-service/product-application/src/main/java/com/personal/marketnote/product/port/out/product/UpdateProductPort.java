package com.personal.marketnote.product.port.out.product;

import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.ProductNotFoundException;

/**
 * 상품 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 수정 기능을 제공합니다.
 */
public interface UpdateProductPort {
    /**
     * @param product 상품 도메인
     * @return void
     * @Date 2026-01-01
     * @Author 성효빈
     * @Description 상품 정보를 수정합니다.
     */
    void update(Product product) throws ProductNotFoundException;
}
