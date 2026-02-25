package com.personal.marketnote.community.port.out.product;

import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;

import java.util.List;
import java.util.Map;

/**
 * 가격 정책 기반 상품 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-09
 * @Description 가격 정책 ID를 기반으로 상품 정보를 조회합니다.
 */
public interface FindProductByPricePolicyPort {
    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @return 가격 정책 ID별 상품 정보 {@link Map}
     * @Date 2026-01-09
     * @Author 성효빈
     * @Description 가격 정책 ID 목록으로 상품 정보를 조회합니다.
     */
    Map<Long, ProductInfoResult> findByPricePolicyIds(List<Long> pricePolicyIds);
}
