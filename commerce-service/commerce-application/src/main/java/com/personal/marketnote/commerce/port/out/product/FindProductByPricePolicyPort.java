package com.personal.marketnote.commerce.port.out.product;

import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;

import java.util.List;
import java.util.Map;

public interface FindProductByPricePolicyPort {
    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @return 가격 정책 ID별 상품 정보 맵 {@link Map}
     * @Date 2026-01-09
     * @Author 성효빈
     * @Description 가격 정책 ID 목록으로 상품 정보를 조회합니다.
     */
    Map<Long, ProductInfoResult> findByPricePolicyIds(List<Long> pricePolicyIds);
}
