package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.port.in.result.product.GetProductSortPropertiesResult;

/**
 * 상품 정렬 속성 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 상품 정렬 속성 조회 기능을 제공합니다.
 */
public interface GetProductSortPropertiesUseCase {
    /**
     * @return 상품 정렬 속성 목록 조회 결과 {@link GetProductSortPropertiesResult}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 상품 정렬 속성 목록을 조회합니다.
     */
    GetProductSortPropertiesResult getProductSortProperties();
}
