package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.exception.NoUnsettledAllocationException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ExecuteSettlementUseCase;
import com.personal.marketnote.commerce.port.out.settlement.DefaultSettlementPolicyProvider;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 정산 실행 서비스.
 * <p>
 * 판매자별 독립 트랜잭션으로 정산을 처리한다.
 * 한 판매자의 정산 실패가 다른 판매자에게 영향을 주지 않으며,
 * 실패한 정산은 FAILED 상태로 저장되어 재시도가 가능하다.
 * </p>
 * <p>
 * 판매자별 정산 정책이 등록되어 있으면 해당 수수료율을 적용하고,
 * 미등록 판매자에 대해서는 시스템 기본 수수료율(DefaultSettlementPolicyProvider)을 사용한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-02-16
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class ExecuteSettlementService implements ExecuteSettlementUseCase {
    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final FindSettlementPolicyPort findSettlementPolicyPort;
    private final DefaultSettlementPolicyProvider defaultSettlementPolicyProvider;
    private final ProcessSellerSettlementService processSellerSettlementService;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void executeSettlement(ExecuteSettlementCommand command) {
        List<PaymentAllocation> unsettledAllocations = findPaymentAllocationPort.findUnsettledAllocations(
                command.year(), command.month());

        if (unsettledAllocations.isEmpty()) {
            throw new NoUnsettledAllocationException();
        }

        Map<Long, List<PaymentAllocation>> allocationsBySeller = unsettledAllocations.stream()
                .collect(Collectors.groupingBy(PaymentAllocation::getSellerId));

        List<Long> sellerIds = allocationsBySeller.keySet().stream().toList();
        Map<Long, SettlementPolicy> policyMap = findSettlementPolicyPort.findActiveBySellerIdIn(sellerIds);

        Integer defaultPgFeeRate = defaultSettlementPolicyProvider.getDefaultPgFeeRate();
        Integer defaultPlatformFeeRate = defaultSettlementPolicyProvider.getDefaultPlatformFeeRate();

        int failedCount = 0;
        for (Map.Entry<Long, List<PaymentAllocation>> entry : allocationsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<PaymentAllocation> sellerAllocations = entry.getValue();

            SettlementPolicy policy = policyMap.get(sellerId);
            Integer pgFeeRate = (FormatValidator.hasValue(policy)) ? policy.getPgFeeRate() : defaultPgFeeRate;
            Integer platformFeeRate = (FormatValidator.hasValue(policy)) ? policy.getPlatformFeeRate() : defaultPlatformFeeRate;

            try {
                processSellerSettlementService.process(
                        command, sellerId, sellerAllocations, pgFeeRate, platformFeeRate);
            } catch (Exception e) {
                failedCount++;
                log.error("판매자 정산 실패 - sellerId: {}, year: {}, month: {}, pgFeeRate: {}, platformFeeRate: {}, error: {}",
                        sellerId, command.year(), command.month(), pgFeeRate, platformFeeRate, e.getMessage(), e);
            }
        }

        if (failedCount > 0) {
            log.warn("정산 실행 완료 - 총 {}건 중 {}건 실패", allocationsBySeller.size(), failedCount);
        }
    }
}
