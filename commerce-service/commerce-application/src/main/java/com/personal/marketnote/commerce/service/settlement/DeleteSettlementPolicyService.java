package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.exception.SettlementPolicyNotFoundException;
import com.personal.marketnote.commerce.port.in.usecase.settlement.DeleteSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPolicyPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 정산 정책 삭제(비활성화) 서비스
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class DeleteSettlementPolicyService implements DeleteSettlementPolicyUseCase {
    private final FindSettlementPolicyPort findSettlementPolicyPort;
    private final UpdateSettlementPolicyPort updateSettlementPolicyPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deletePolicy(Long id) {
        SettlementPolicy policy = findSettlementPolicyPort.findById(id)
                .orElseThrow(() -> new SettlementPolicyNotFoundException(id));

        policy.deactivate();
        updateSettlementPolicyPort.update(policy);

        log.info("정산 정책 비활성화 완료 - id: {}, sellerId: {}", id, policy.getSellerId());
    }
}
