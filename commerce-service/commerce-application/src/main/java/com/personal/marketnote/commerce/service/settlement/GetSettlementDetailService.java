package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementDetailResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementDetailUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetSettlementDetailService implements GetSettlementDetailUseCase {
    private final FindSettlementPort findSettlementPort;
    private final FindPaymentAllocationPort findPaymentAllocationPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<GetSettlementDetailResult> getSettlementAllocations(Long settlementId) {
        findSettlementPort.findById(settlementId)
                .orElseThrow(() -> {
                    log.warn("정산을 찾을 수 없습니다. settlementId={}", settlementId);
                    return new SettlementNotFoundException();
                });

        List<PaymentAllocation> allocations = findPaymentAllocationPort.findBySettlementId(settlementId);
        return GetSettlementDetailResult.fromList(allocations);
    }
}
