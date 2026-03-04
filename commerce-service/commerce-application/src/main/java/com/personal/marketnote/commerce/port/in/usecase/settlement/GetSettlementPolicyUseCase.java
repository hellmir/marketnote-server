package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;

import java.util.List;

/**
 * 정산 정책 조회 유스케이스
 *
 * @author 성효빈
 * @description 정산 정책을 단건 또는 전체 조회합니다.
 * @since 2026-03-02
 */
public interface GetSettlementPolicyUseCase {
    GetSettlementPolicyResult getPolicy(Long id);

    GetSettlementPolicyResult getPolicyBySellerId(Long sellerId);

    List<GetSettlementPolicyResult> getAllPolicies();
}
