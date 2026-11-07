package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.command.settlement.GetSettlementsQuery;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementUseCase;
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
public class GetSettlementService implements GetSettlementUseCase {
    private final FindSettlementPort findSettlementPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetSettlementResult getSettlement(Long id) {
        Settlement settlement = findSettlementPort.findById(id)
                .orElseThrow(() -> {
                    log.warn("정산을 찾을 수 없습니다. settlementId={}", id);
                    return new SettlementNotFoundException();
                });
        return GetSettlementResult.from(settlement);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetSettlementsResult getSettlements(GetSettlementsQuery query) {
        List<Settlement> settlements = findSettlementPort.findAllByYearAndMonth(query.year(), query.month());
        return GetSettlementsResult.from(settlements);
    }
}
