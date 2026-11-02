package com.personal.marketnote.product.port.in.usecase.pricepolicy;

import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;

import java.util.List;

/**
 * 가격 정책 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 가격 정책 조회 관련 기능을 제공합니다.
 */
public interface GetPricePolicyUseCase {
    /**
     * @param id 가격 정책 ID
     * @return 가격 정책 도메인 {@link PricePolicy}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 가격 정책을 조회합니다.
     */
    PricePolicy getPricePolicy(Long id);

    /**
     * @param optionIds 옵션 ID 목록
     * @return 가격 정책 도메인 {@link PricePolicy}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 가격 정책을 조회합니다.
     */
    PricePolicy getPricePolicy(List<Long> optionIds);
}


