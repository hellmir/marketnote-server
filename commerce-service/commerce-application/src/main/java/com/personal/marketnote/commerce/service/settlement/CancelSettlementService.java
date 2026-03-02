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

        recordPgSettlementCancellation(settlementId, settlement.getTotalAllocatedAmount(), settlement.getPgFeeAmount());
        recordSellerSettlementCancellation(settlementId, settlement.getSellerPayoutAmount(), settlement.getPlatformFeeAmount());

        updateSettlementPort.update(settlement);

        log.info("정산 취소 완료 - settlementId: {}, sellerId: {}, year: {}, month: {}",
                settlementId, settlement.getSellerId(), settlement.getYear(), settlement.getMonth());
    }

    /**
     * PG 정산 역분개를 기록한다.
     * <p>
     * 원래 PG 정산 분개의 DEBIT/CREDIT을 반전한다.
     * <pre>
     * DEBIT  매출채권_PG      = totalAllocatedAmount
     * CREDIT 보통예금         = totalAllocatedAmount - pgFeeAmount
     * CREDIT PG수수료비용     = pgFeeAmount (if > 0)
     * </pre>
     * </p>
     */
    private void recordPgSettlementCancellation(Long settlementId, Long totalAllocatedAmount, Long pgFeeAmount) {
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_RECEIVABLE));
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_CASH));
        Account pgFeeAccount = findAccountPort.findByName(ACCOUNT_PG_FEE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_FEE));

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(pgReceivable.getId())
                .amount(totalAllocatedAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        long cashAmount = totalAllocatedAmount - pgFeeAmount;
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(cashAmount)
                .transactionType(TransactionType.CREDIT)
                .build());

        if (pgFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(pgFeeAccount.getId())
                    .amount(pgFeeAmount)
                    .transactionType(TransactionType.CREDIT)
                    .build());
        }

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SETTLEMENT_CANCELLATION)
                .targetType("SETTLEMENT")
                .targetId(settlementId)
                .description("PG 정산 취소 역분개")
                .idempotencyKey("PG_SETTLEMENT_CANCEL:" + settlementId)
                .entries(entries)
                .build();

        recordLedgerEntryUseCase.record(command);
    }

    /**
     * 판매자 정산 역분개를 기록한다.
     * <p>
     * 원래 판매자 정산 분개의 DEBIT/CREDIT을 반전한다.
     * <pre>
     * DEBIT  보통예금           = sellerPayoutAmount
     * DEBIT  플랫폼수수료수익    = platformFeeAmount (if > 0)
     * CREDIT 미지급금_판매자     = sellerPayoutAmount + platformFeeAmount
     * </pre>
     * </p>
     */
    private void recordSellerSettlementCancellation(Long settlementId, Long sellerPayoutAmount, Long platformFeeAmount) {
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_SELLER_PAYABLE));
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_CASH));
        Account platformFeeAccount = findAccountPort.findByName(ACCOUNT_PLATFORM_FEE_REVENUE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PLATFORM_FEE_REVENUE));

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(sellerPayoutAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        if (platformFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(platformFeeAccount.getId())
                    .amount(platformFeeAmount)
                    .transactionType(TransactionType.DEBIT)
                    .build());
        }

        long totalDebit = sellerPayoutAmount + platformFeeAmount;
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(sellerPayable.getId())
                .amount(totalDebit)
                .transactionType(TransactionType.CREDIT)
                .build());

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SETTLEMENT_CANCELLATION)
                .targetType("SETTLEMENT")
                .targetId(settlementId)
                .description("판매자 정산 취소 역분개")
                .idempotencyKey("SELLER_SETTLEMENT_CANCEL:" + settlementId)
                .entries(entries)
                .build();

        recordLedgerEntryUseCase.record(command);
    }
}
