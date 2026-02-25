package com.personal.marketnote.product.port.in.usecase.option;

import com.personal.marketnote.product.port.in.result.option.GetProductOptionsResult;

/**
 * 상품 옵션 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 조회 기능을 제공합니다.
 */
public interface GetProductOptionsUseCase {
    /**
     * @param productId 상품 ID
     * @return 상품의 옵션 카테고리 및 하위 옵션 목록 결과
     * @Date 2026-01-01
     * @Author 성효빈
     * @Description 특정 상품의 옵션 카테고리 및 옵션 목록을 조회합니다.
     */
    GetProductOptionsResult getProductOptions(Long productId);
}
