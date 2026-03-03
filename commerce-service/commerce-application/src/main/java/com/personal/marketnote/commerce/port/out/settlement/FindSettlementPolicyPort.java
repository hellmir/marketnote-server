package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FindSettlementPolicyPort {
    Optional<SettlementPolicy> findById(Long id);

    Optional<SettlementPolicy> findActiveBySellerId(Long sellerId);

    List<SettlementPolicy> findAll();

    /**
     * 판매자 ID 목록에 해당하는 활성 정산 정책을 조회하여 sellerId → SettlementPolicy 맵으로 반환한다.
     */
    Map<Long, SettlementPolicy> findActiveBySellerIdIn(List<Long> sellerIds);
}
