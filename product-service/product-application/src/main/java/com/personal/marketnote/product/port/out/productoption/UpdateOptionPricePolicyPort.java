package com.personal.marketnote.product.port.out.productoption;

import java.util.List;

/**
 * 옵션 가격 정책 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 옵션 가격 정책 수정 기능을 제공합니다.
 */
public interface UpdateOptionPricePolicyPort {
    /**
     * @param productId     상품 ID
     * @param pricePolicyId 가격 정책 ID
     * @param optionIds     옵션 ID 목록
     * @return void
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 상품의 옵션에 가격 정책을 할당합니다.
     */
    void assignPricePolicyToOptions(Long productId, Long pricePolicyId, List<Long> optionIds);
}

