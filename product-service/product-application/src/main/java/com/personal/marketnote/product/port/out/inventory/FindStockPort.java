package com.personal.marketnote.product.port.out.inventory;

import com.personal.marketnote.product.port.out.result.GetInventoryResult;

import java.util.List;
import java.util.Set;

/**
 * 재고 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 재고 조회 관련 기능을 제공합니다.
 */
public interface FindStockPort {
    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @return 재고 조회 결과 목록 {@link Set<GetInventoryResult>}
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 가격 정책 ID 목록으로 재고를 조회합니다.
     */
    Set<GetInventoryResult> findByPricePolicyIds(List<Long> pricePolicyIds);
}
