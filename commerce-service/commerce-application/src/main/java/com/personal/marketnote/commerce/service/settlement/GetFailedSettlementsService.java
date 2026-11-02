package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetFailedSettlementsUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 실패한 정산 목록을 조회하는 서비스.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
public class GetFailedSettlementsService implements GetFailedSettlementsUseCase {
    private final FindSettlementPort findSettlementPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetSettlementsResult getFailedSettlements() {
        List<Settlement> failedSettlements = findSettlementPort.findAllByStatus(SettlementStatus.FAILED);
        return GetSettlementsResult.from(failedSettlements);
    }
}
