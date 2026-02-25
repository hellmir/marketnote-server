package com.personal.marketnote.product.port.in.usecase.pricepolicy;

import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetPricePoliciesResult;

import java.util.List;

/**
 * 가격 정책 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 가격 정책 목록 조회 관련 기능을 제공합니다.
 */
public interface GetPricePoliciesUseCase {
    /**
     * @param productId 상품 ID
     * @return 가격 정책 및 옵션 조회 결과 {@link GetPricePoliciesResult}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 가격 정책 및 옵션 목록을 조회합니다.
     */
    GetPricePoliciesResult getPricePoliciesAndOptions(Long productId);

    /**
     * @param ids 가격 정책 ID 목록
     * @return 옵션 목록을 포함한 가격 정책 도메인 목록 {@link List<PricePolicy>}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 가격 정책 및 옵션 목록을 조회합니다.
     */
    List<PricePolicy> getPricePoliciesAndOptions(List<Long> ids);
}
