package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.exception.SettlementPolicyNotFoundException;
import com.personal.marketnote.commerce.port.in.command.settlement.UpdateSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.UpdateSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPolicyPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 정산 정책 수정 서비스
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class UpdateSettlementPolicyService implements UpdateSettlementPolicyUseCase {
    private final FindSettlementPolicyPort findSettlementPolicyPort;
    private final UpdateSettlementPolicyPort updateSettlementPolicyPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public GetSettlementPolicyResult updatePolicy(UpdateSettlementPolicyCommand command) {
        SettlementPolicy policy = findSettlementPolicyPort.findById(command.id())
                .orElseThrow(() -> new SettlementPolicyNotFoundException(command.id()));

        policy.update(
                command.pgFeeRate(),
                command.platformFeeRate(),
                SettlementCycle.valueOf(command.settlementCycle()),
                command.minPayoutAmount()
        );

        SettlementPolicy updated = updateSettlementPolicyPort.update(policy);

        log.info("정산 정책 수정 완료 - id: {}, sellerId: {}, pgFeeRate: {}, platformFeeRate: {}",
                command.id(), policy.getSellerId(), command.pgFeeRate(), command.platformFeeRate());

        return GetSettlementPolicyResult.from(updated);
    }
}
