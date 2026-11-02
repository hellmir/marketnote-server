package com.personal.marketnote.product.port.out.pricepolicy;

/**
 * 가격 정책 삭제 포트
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 가격 정책 삭제 기능을 제공합니다.
 */
public interface DeletePricePolicyPort {
    /**
     * @param pricePolicyId 가격 정책 ID
     * @return void
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 가격 정책 ID로 가격 정책을 삭제합니다.
     */
    void deleteById(Long pricePolicyId);
}
