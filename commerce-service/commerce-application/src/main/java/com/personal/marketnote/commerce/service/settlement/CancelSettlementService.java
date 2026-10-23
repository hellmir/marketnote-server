package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.exception.AccountNotFoundException;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.CancelSettlementUseCase;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 정산 취소 서비스.
 * <p>
 * COMPLETED 상태의 정산을 CANCELLED로 전이하고,
 * 원래 분개의 역분개(reverse journal entry)를 기록한다.
 * PG 역분개와 Seller 역분개를 단일 LedgerTransaction에 6개 entries로 묶어
 * 원자성을 보장한다 (부분 커밋으로 인한 복식부기 불균형 방지).
 * PaymentAllocation의 settlementId는 유지하여 재실행 시 추적 가능하도록 한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class CancelSettlementService implements CancelSettlementUseCase {
    private static final String ACCOUNT_PG_RECEIVABLE = "매출채권_PG";
    private static final String ACCOUNT_SELLER_PAYABLE = "미지급금_판매자";
    private static final String ACCOUNT_CASH = "보통예금";
    private static final String ACCOUNT_PG_FEE = "PG수수료비용";
    private static final String ACCOUNT_PLATFORM_FEE_REVENUE = "플랫폼수수료수익";

    private final FindSettlementPort findSettlementPort;
    private final UpdateSettlementPort updateSettlementPort;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;
    private final FindAccountPort findAccountPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void cancelSettlement(Long settlementId) {
        Settlement settlement = findSettlementPort.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException(settlementId));

        settlement.cancel();

        recordSettlementCancellation(
                settlementId,
                settlement.getTotalAllocatedAmount().getValue(),
                settlement.getPgFeeAmount().getValue(),
                settlement.getSellerPayoutAmount().getValue(),
                settlement.getPlatformFeeAmount().getValue()
        );

        updateSettlementPort.update(settlement);

        log.info("정산 취소 완료 - settlementId: {}, sellerId: {}, year: {}, month: {}",
                settlementId, settlement.getSellerId(), settlement.getYear(), settlement.getMonth()
        );
    }

    /**
     * 정산 취소 역분개(PG + Seller)를 단일 LedgerTransaction으로 기록한다.
     * <p>
     * PG 역분개와 Seller 역분개를 1개의 트랜잭션에 6개 entries로 묶어 원자성을 보장한다.
     * <pre>
     * DEBIT  매출채권_PG        = totalAllocatedAmount
     * DEBIT  보통예금           = sellerPayoutAmount
     * DEBIT  플랫폼수수료수익   = platformFeeAmount (if > 0)
     * CREDIT 보통예금           = totalAllocatedAmount - pgFeeAmount
     * CREDIT PG수수료비용       = pgFeeAmount (if > 0)
     * CREDIT 미지급금_판매자    = sellerPayoutAmount + platformFeeAmount
     * </pre>
     * DEBIT 합계 == CREDIT 합계 == totalAllocated + sellerPayout + platformFee
     * </p>
     */
    private void recordSettlementCancellation(
            Long settlementId,
            long totalAllocatedAmount,
            long pgFeeAmount,
            long sellerPayoutAmount,
            long platformFeeAmount
    ) {
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_RECEIVABLE));
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_CASH));
        Account pgFeeAccount = findAccountPort.findByName(ACCOUNT_PG_FEE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_FEE));
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_SELLER_PAYABLE));
        Account platformFeeAccount = findAccountPort.findByName(ACCOUNT_PLATFORM_FEE_REVENUE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PLATFORM_FEE_REVENUE));

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        // PG 역분개 DEBIT: 매출채권_PG
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(pgReceivable.getId())
                .amount(totalAllocatedAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        // Seller 역분개 DEBIT: 보통예금
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(sellerPayoutAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        // Seller 역분개 DEBIT: 플랫폼수수료수익 (platformFeeAmount > 0일 때만)
        if (platformFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(platformFeeAccount.getId())
                    .amount(platformFeeAmount)
                    .transactionType(TransactionType.DEBIT)
                    .build());
        }

        // PG 역분개 CREDIT: 보통예금 (totalAllocated - pgFee)
        long pgCashCredit = Math.subtractExact(totalAllocatedAmount, pgFeeAmount);
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(pgCashCredit)
                .transactionType(TransactionType.CREDIT)
                .build());

        // PG 역분개 CREDIT: PG수수료비용 (pgFeeAmount > 0일 때만)
        if (pgFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(pgFeeAccount.getId())
                    .amount(pgFeeAmount)
                    .transactionType(TransactionType.CREDIT)
                    .build());
        }

        // Seller 역분개 CREDIT: 미지급금_판매자 (sellerPayout + platformFee)
        long sellerPayableCredit = Math.addExact(sellerPayoutAmount, platformFeeAmount);
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(sellerPayable.getId())
                .amount(sellerPayableCredit)
                .transactionType(TransactionType.CREDIT)
                .build());

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SETTLEMENT_CANCELLATION)
                .targetType("SETTLEMENT")
                .targetId(settlementId)
                .description("정산 취소 역분개")
                .idempotencyKey("SETTLEMENT_CANCEL:" + settlementId)
                .entries(entries)
                .build();

        recordLedgerEntryUseCase.record(command);
    }
}
