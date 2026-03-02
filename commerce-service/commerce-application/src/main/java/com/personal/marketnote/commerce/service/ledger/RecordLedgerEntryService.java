package com.personal.marketnote.commerce.service.ledger;

import com.personal.marketnote.commerce.domain.ledger.*;
import com.personal.marketnote.commerce.exception.AccountNotFoundException;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.exception.EmptyLedgerEntriesException;
import com.personal.marketnote.commerce.exception.InactiveAccountException;
import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.ledger.SaveLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.SaveLedgerTransactionPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class RecordLedgerEntryService implements RecordLedgerEntryUseCase {
    private static final String ACCOUNT_PG_RECEIVABLE = "매출채권_PG";
    private static final String ACCOUNT_SELLER_PAYABLE = "미지급금_판매자";
    private static final String ACCOUNT_CASH = "보통예금";
    private static final String ACCOUNT_PG_FEE = "PG수수료비용";
    private static final String ACCOUNT_PLATFORM_FEE_REVENUE = "플랫폼수수료수익";

    private final FindAccountPort findAccountPort;
    private final SaveLedgerTransactionPort saveLedgerTransactionPort;
    private final SaveLedgerEntryPort saveLedgerEntryPort;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void record(RecordLedgerEntryCommand command) {
        validateEntryLines(command.entries());
        validateIdempotency(command.idempotencyKey());
        validateAccounts(command.entries());

        List<LedgerEntry> entries = createEntries(command.entries());

        LedgerTransaction transaction = createTransaction(command);
        transaction.validateEntries(entries);

        LedgerTransaction savedTransaction = saveLedgerTransactionPort.save(transaction);

        entries.forEach(entry -> entry.assignTransaction(savedTransaction.getId()));
        saveLedgerEntryPort.saveAll(entries);

        log.info("장부 거래 기록 완료 - transactionId: {}, type: {}, targetType: {}, targetId: {}, idempotencyKey: {}",
                savedTransaction.getId(), command.transactionType(),
                command.targetType(), command.targetId(), command.idempotencyKey());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordPaymentApproval(Long orderId, long paymentAmount) {
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_RECEIVABLE));
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_SELLER_PAYABLE));

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                .targetType("PAYMENT")
                .targetId(orderId)
                .description("결제 승인 분개")
                .idempotencyKey("PAYMENT_APPROVAL:" + orderId)
                .entries(List.of(
                        RecordLedgerEntryCommand.EntryLine.builder()
                                .accountId(pgReceivable.getId())
                                .amount(paymentAmount)
                                .transactionType(TransactionType.DEBIT)
                                .build(),
                        RecordLedgerEntryCommand.EntryLine.builder()
                                .accountId(sellerPayable.getId())
                                .amount(paymentAmount)
                                .transactionType(TransactionType.CREDIT)
                                .build()
                ))
                .build();

        record(command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordPaymentCancellation(Long orderId, long cancelAmount, String idempotencyKey) {
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_RECEIVABLE));
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_SELLER_PAYABLE));

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PAYMENT_CANCELLATION)
                .targetType("PAYMENT")
                .targetId(orderId)
                .description("결제 취소/환불 역분개")
                .idempotencyKey(idempotencyKey)
                .entries(List.of(
                        RecordLedgerEntryCommand.EntryLine.builder()
                                .accountId(sellerPayable.getId())
                                .amount(cancelAmount)
                                .transactionType(TransactionType.DEBIT)
                                .build(),
                        RecordLedgerEntryCommand.EntryLine.builder()
                                .accountId(pgReceivable.getId())
                                .amount(cancelAmount)
                                .transactionType(TransactionType.CREDIT)
                                .build()
                ))
                .build();

        record(command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordPgSettlement(Long settlementId, long totalAmount, long pgFeeAmount) {
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_CASH));
        Account pgFeeAccount = findAccountPort.findByName(ACCOUNT_PG_FEE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_FEE));
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_RECEIVABLE));

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        long cashAmount = totalAmount - pgFeeAmount;
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(cashAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        if (pgFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(pgFeeAccount.getId())
                    .amount(pgFeeAmount)
                    .transactionType(TransactionType.DEBIT)
                    .build());
        }

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(pgReceivable.getId())
                .amount(totalAmount)
                .transactionType(TransactionType.CREDIT)
                .build());

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PG_SETTLEMENT)
                .targetType("SETTLEMENT")
                .targetId(settlementId)
                .description("PG 정산 입금 분개")
                .idempotencyKey("PG_SETTLEMENT:" + settlementId)
                .entries(entries)
                .build();

        record(command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordSellerSettlement(Long settlementId, long totalAmount, long sellerPayoutAmount, long platformFeeAmount) {
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_SELLER_PAYABLE));
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_CASH));
        Account platformFeeAccount = findAccountPort.findByName(ACCOUNT_PLATFORM_FEE_REVENUE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PLATFORM_FEE_REVENUE));

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(sellerPayable.getId())
                .amount(totalAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(sellerPayoutAmount)
                .transactionType(TransactionType.CREDIT)
                .build());

        if (platformFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(platformFeeAccount.getId())
                    .amount(platformFeeAmount)
                    .transactionType(TransactionType.CREDIT)
                    .build());
        }

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SELLER_SETTLEMENT)
                .targetType("SETTLEMENT")
                .targetId(settlementId)
                .description("판매자 정산 분개")
                .idempotencyKey("SELLER_SETTLEMENT:" + settlementId)
                .entries(entries)
                .build();

        record(command);
    }

    private void validateEntryLines(List<RecordLedgerEntryCommand.EntryLine> entryLines) {
        if (FormatValidator.hasNoValue(entryLines) || entryLines.isEmpty()) {
            throw new EmptyLedgerEntriesException();
        }
    }

    private void validateIdempotency(String idempotencyKey) {
        if (saveLedgerTransactionPort.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateLedgerTransactionException(idempotencyKey);
        }
    }

    private void validateAccounts(List<RecordLedgerEntryCommand.EntryLine> entryLines) {
        for (RecordLedgerEntryCommand.EntryLine entryLine : entryLines) {
            Account account = findAccountPort.findById(entryLine.accountId())
                    .orElseThrow(() -> new AccountNotFoundException(entryLine.accountId()));
            if (!account.isActive()) {
                throw new InactiveAccountException(account.getName());
            }
        }
    }

    private List<LedgerEntry> createEntries(List<RecordLedgerEntryCommand.EntryLine> entryLines) {
        List<LedgerEntry> entries = new ArrayList<>();
        for (RecordLedgerEntryCommand.EntryLine entryLine : entryLines) {
            LedgerEntry entry = LedgerEntry.from(LedgerEntryCreateState.builder()
                    .accountId(entryLine.accountId())
                    .amount(entryLine.amount())
                    .transactionType(entryLine.transactionType())
                    .build());
            entries.add(entry);
        }
        return entries;
    }

    private LedgerTransaction createTransaction(RecordLedgerEntryCommand command) {
        return LedgerTransaction.from(LedgerTransactionCreateState.builder()
                .transactionType(command.transactionType())
                .targetType(command.targetType())
                .targetId(command.targetId())
                .description(command.description())
                .idempotencyKey(command.idempotencyKey())
                .build());
    }
}
