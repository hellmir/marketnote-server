package com.personal.marketnote.product.port.out.pricepolicy;

import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;

/**
 * 가격 정책 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 가격 정책 저장 기능을 제공합니다.
 */
public interface SavePricePolicyPort {
    /**
     * @param pricePolicy 가격 정책 도메인
     * @return 저장된 가격 정책 ID {@link Long}
     * @Date 2026-01-01
     * @Author 성효빈
     * @Description 가격 정책을 저장합니다.
     */
    Long save(PricePolicy pricePolicy);
}
