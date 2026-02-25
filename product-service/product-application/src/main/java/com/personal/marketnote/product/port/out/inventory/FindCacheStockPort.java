package com.personal.marketnote.product.port.out.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingIntegerException;

import java.util.List;
import java.util.Map;

/**
 * 캐시 재고 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 캐시 재고 조회 관련 기능을 제공합니다.
 */
public interface FindCacheStockPort {
    /**
     * @param pricePolicyId 가격 정책 ID
     * @return 캐시 재고 수량 {@link int}
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 가격 정책 ID로 캐시 재고 수량을 조회합니다.
     */
    int findByPricePolicyId(Long pricePolicyId) throws ParsingIntegerException;

    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @return 가격 정책 ID별 캐시 재고 수량 맵 {@link Map}
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 가격 정책 ID 목록으로 캐시 재고 수량을 일괄 조회합니다.
     */
    Map<Long, Integer> findByPricePolicyIds(List<Long> pricePolicyIds);
}
