package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicyCreateState;
import com.personal.marketnote.commerce.exception.SettlementPolicyAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.settlement.RegisterSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.RegisterSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.SaveSettlementPolicyPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 정산 정책 등록 서비스
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class RegisterSettlementPolicyService implements RegisterSettlementPolicyUseCase {
    private final FindSettlementPolicyPort findSettlementPolicyPort;
    private final SaveSettlementPolicyPort saveSettlementPolicyPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public GetSettlementPolicyResult registerPolicy(RegisterSettlementPolicyCommand command) {
        findSettlementPolicyPort.findActiveBySellerId(command.sellerId())
                .ifPresent(existing -> {
                    throw new SettlementPolicyAlreadyExistsException(command.sellerId());
                });

        SettlementPolicy policy = SettlementPolicy.from(SettlementPolicyCreateState.builder()
                .sellerId(command.sellerId())
                .pgFeeRate(command.pgFeeRate())
                .platformFeeRate(command.platformFeeRate())
                .settlementCycle(SettlementCycle.valueOf(command.settlementCycle()))
                .minPayoutAmount(command.minPayoutAmount())
                .build());

        SettlementPolicy saved = saveSettlementPolicyPort.save(policy);

        log.info("정산 정책 등록 완료 - sellerId: {}, pgFeeRate: {}, platformFeeRate: {}, cycle: {}",
                command.sellerId(), command.pgFeeRate(), command.platformFeeRate(), command.settlementCycle());

        return GetSettlementPolicyResult.from(saved);
    }
}
