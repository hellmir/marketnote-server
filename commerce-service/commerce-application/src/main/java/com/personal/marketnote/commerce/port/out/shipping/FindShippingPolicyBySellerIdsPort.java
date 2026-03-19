package com.personal.marketnote.commerce.port.out.shipping;

import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;

import java.util.List;
import java.util.Map;

/**
 * 판매자 ID 목록 기반 배송비 정책 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-03-19
 * @Description 판매자 ID 목록으로 배송비 정책을 조회합니다.
 */
public interface FindShippingPolicyBySellerIdsPort {

    /**
     * @param sellerIds 판매자 ID 목록
     * @return 판매자 ID별 배송비 정책 정보 맵 {@link Map}
     * @Date 2026-03-19
     * @Author 성효빈
     * @Description 판매자 ID 목록으로 배송비 정책을 조회합니다.
     */
    Map<Long, ShippingPolicyInfoResult> findBySellerIds(List<Long> sellerIds);
}
