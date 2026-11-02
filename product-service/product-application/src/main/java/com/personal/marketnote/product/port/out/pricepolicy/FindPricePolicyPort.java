package com.personal.marketnote.product.port.out.pricepolicy;

import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;

import java.util.List;
import java.util.Optional;

/**
 * 가격 정책 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 가격 정책 조회 관련 기능을 제공합니다.
 */
public interface FindPricePolicyPort {
    /**
     * @param id 가격 정책 ID
     * @return 가격 정책 도메인 {@link Optional<PricePolicy>}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 가격 정책 ID로 가격 정책을 조회합니다.
     */
    Optional<PricePolicy> findById(Long id);

    /**
     * @param productId 상품 ID
     * @param optionIds 옵션 ID 목록
     * @return 가격 정책 도메인 {@link Optional<PricePolicy>}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 상품 ID와 옵션 ID 목록으로 가격 정책을 조회합니다.
     */
    Optional<PricePolicy> findByProductAndOptionIds(Long productId, List<Long> optionIds);

    /**
     * @param optionIds 옵션 ID 목록
     * @return 가격 정책 도메인 {@link Optional<PricePolicy>}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 옵션 ID 목록으로 가격 정책을 조회합니다.
     */
    Optional<PricePolicy> findByOptionIds(List<Long> optionIds);
}
