package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.RetryFailedSettlementUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 실패한 정산을 재시도하는 서비스.
 * <p>
 * FAILED 상태의 정산을 PENDING으로 리셋한 후 분개 기록과 상태 완료 처리를 재실행한다.
 * PaymentAllocation은 최초 정산 실행 시 이미 할당되어 있으므로 재할당하지 않는다.
 * 분개 기록 중 실패하면 정산을 다시 FAILED 상태로 되돌린다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class RetryFailedSettlementService implements RetryFailedSettlementUseCase {
    private final FindSettlementPort findSettlementPort;
    private final UpdateSettlementPort updateSettlementPort;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void retrySettlement(Long settlementId) {
        Settlement settlement = findSettlementPort.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException(settlementId));

        settlement.resetToPending();

        try {
            Long totalAllocatedAmount = settlement.getTotalAllocatedAmount();
            Long pgFeeAmount = settlement.getPgFeeAmount();
            Long platformFeeAmount = settlement.getPlatformFeeAmount();
            Long sellerPayoutAmount = settlement.getSellerPayoutAmount();

            recordLedgerEntryUseCase.recordPgSettlement(settlementId, totalAllocatedAmount, pgFeeAmount);

            long sellerSettlementDebit = sellerPayoutAmount + platformFeeAmount;
            recordLedgerEntryUseCase.recordSellerSettlement(settlementId, sellerSettlementDebit, sellerPayoutAmount, platformFeeAmount);

            settlement.complete();
        } catch (Exception e) {
            settlement.fail();
            updateSettlementPort.update(settlement);
            log.error("정산 재시도 실패 - settlementId: {}, error: {}", settlementId, e.getMessage(), e);
            throw e;
        }

        updateSettlementPort.update(settlement);

        log.info("정산 재시도 완료 - settlementId: {}, sellerId: {}, year: {}, month: {}",
                settlementId, settlement.getSellerId(), settlement.getYear(), settlement.getMonth());
    }
}
