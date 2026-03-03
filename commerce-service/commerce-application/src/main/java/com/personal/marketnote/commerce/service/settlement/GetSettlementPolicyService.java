package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.exception.SettlementPolicyNotFoundException;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 정산 정책 조회 서비스
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class GetSettlementPolicyService implements GetSettlementPolicyUseCase {
    private final FindSettlementPolicyPort findSettlementPolicyPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetSettlementPolicyResult getPolicy(Long id) {
        SettlementPolicy policy = findSettlementPolicyPort.findById(id)
                .orElseThrow(() -> new SettlementPolicyNotFoundException(id));

        return GetSettlementPolicyResult.from(policy);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetSettlementPolicyResult getPolicyBySellerId(Long sellerId) {
        SettlementPolicy policy = findSettlementPolicyPort.findActiveBySellerId(sellerId)
                .orElseThrow(() -> new SettlementPolicyNotFoundException(sellerId));

        return GetSettlementPolicyResult.from(policy);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<GetSettlementPolicyResult> getAllPolicies() {
        return findSettlementPolicyPort.findAll().stream()
                .map(GetSettlementPolicyResult::from)
                .toList();
    }
}
