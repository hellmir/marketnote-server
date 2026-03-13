package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementCreateState;
import com.personal.marketnote.commerce.exception.SettlementAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishSettlementEventPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.SaveSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdatePaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 판매자별 정산 처리를 담당하는 서비스.
 * <p>
 * {@code REQUIRES_NEW} 전파 수준으로 판매자별 독립 트랜잭션을 보장한다.
 * 한 판매자의 정산 실패가 다른 판매자의 정산에 영향을 주지 않는다.
 * UseCase 인터페이스를 구현하지 않는 내부 위임 서비스이므로 {@code @Service}를 사용한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessSellerSettlementService {
    private static final int BASIS_POINT_DENOMINATOR = 10000;

    private final FindSettlementPort findSettlementPort;
    private final SaveSettlementPort saveSettlementPort;
    private final UpdateSettlementPort updateSettlementPort;
    private final UpdatePaymentAllocationPort updatePaymentAllocationPort;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;
    private final PublishSettlementEventPort publishSettlementEventPort;

    /**
     * 개별 판매자의 정산을 독립 트랜잭션으로 처리한다.
     * <p>
     * 정산 생성, 배분 할당, 분개 기록, 상태 완료를 하나의 독립 트랜잭션으로 수행한다.
     * 실패 시 해당 판매자의 정산만 FAILED 상태로 저장된다.
     * </p>
     *
     * @param command           정산 실행 커맨드 (year, month)
     * @param sellerId          판매자 ID
     * @param sellerAllocations 판매자의 결제 배분 목록
     * @param pgFeeRate         PG 수수료율 (basis point)
     * @param platformFeeRate   플랫폼 수수료율 (basis point)
     */
    @Transactional(isolation = READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void process(ExecuteSettlementCommand command, Long sellerId,
                        List<PaymentAllocation> sellerAllocations,
                        Integer pgFeeRate, Integer platformFeeRate) {
        Integer year = command.year();
        Integer month = command.month();

        if (findSettlementPort.existsBySellerIdAndYearAndMonth(sellerId, year, month)) {
            throw new SettlementAlreadyExistsException(sellerId, year, month);
        }

        long totalAllocatedAmount = sellerAllocations.stream()
                .mapToLong(PaymentAllocation::getAllocatedAmount)
                .sum();

        long pgFeeAmount = Math.multiplyExact(totalAllocatedAmount, pgFeeRate) / BASIS_POINT_DENOMINATOR;
        long platformFeeAmount = Math.multiplyExact(totalAllocatedAmount, platformFeeRate) / BASIS_POINT_DENOMINATOR;
        long sellerPayoutAmount = totalAllocatedAmount - pgFeeAmount - platformFeeAmount;

        Settlement settlement = Settlement.from(SettlementCreateState.builder()
                .sellerId(sellerId)
                .year(year)
                .month(month)
                .totalAllocatedAmount(totalAllocatedAmount)
                .pgFeeAmount(pgFeeAmount)
                .platformFeeAmount(platformFeeAmount)
                .sellerPayoutAmount(sellerPayoutAmount)
                .build());

        Settlement savedSettlement = saveSettlementPort.save(settlement);
        Long settlementId = savedSettlement.getId();

        List<Long> allocationIds = sellerAllocations.stream()
                .map(PaymentAllocation::getId)
                .toList();
        updatePaymentAllocationPort.assignSettlement(allocationIds, settlementId);

        recordLedgerEntryUseCase.recordPgSettlement(settlementId, totalAllocatedAmount, pgFeeAmount);

        long sellerSettlementDebit = sellerPayoutAmount + platformFeeAmount;
        recordLedgerEntryUseCase.recordSellerSettlement(settlementId, sellerSettlementDebit, sellerPayoutAmount, platformFeeAmount);

        savedSettlement.complete();
        updateSettlementPort.update(savedSettlement);

        publishSettlementExecutedEvent(settlementId, sellerId, totalAllocatedAmount,
                pgFeeAmount, platformFeeAmount, sellerPayoutAmount);

        log.info("정산 완료 - settlementId: {}, sellerId: {}, year: {}, month: {}, total: {}, pgFee: {}, platformFee: {}, sellerPayout: {}",
                settlementId, sellerId, year, month, totalAllocatedAmount, pgFeeAmount, platformFeeAmount, sellerPayoutAmount);
    }

    private void publishSettlementExecutedEvent(Long settlementId, Long sellerId,
                                                 Long totalAllocatedAmount, Long pgFeeAmount,
                                                 Long platformFeeAmount, Long sellerPayoutAmount) {
        try {
            publishSettlementEventPort.publishSettlementExecutedEvent(
                    settlementId, sellerId, totalAllocatedAmount,
                    pgFeeAmount, platformFeeAmount, sellerPayoutAmount);
        } catch (Exception e) {
            log.error("정산 실행 이벤트 발행 실패 - settlementId: {}, sellerId: {}, error: {}",
                    settlementId, sellerId, e.getMessage(), e);
        }
    }
}
