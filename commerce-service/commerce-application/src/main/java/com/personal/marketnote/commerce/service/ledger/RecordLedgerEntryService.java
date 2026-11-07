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
                command.targetType(), command.targetId(), command.idempotencyKey()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordPaymentApproval(Long orderId, long paymentAmount) {
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_PG_RECEIVABLE);
                    return new AccountNotFoundException();
                });
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_SELLER_PAYABLE);
                    return new AccountNotFoundException();
                });

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.forPaymentApproval(
                orderId,
                List.of(
                        RecordLedgerEntryCommand.EntryLine.debit(pgReceivable.getId(), paymentAmount),
                        RecordLedgerEntryCommand.EntryLine.credit(sellerPayable.getId(), paymentAmount)
                )
        );

        record(command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordPaymentCancellation(Long orderId, long cancelAmount, String idempotencyKey) {
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_PG_RECEIVABLE);
                    return new AccountNotFoundException();
                });
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_SELLER_PAYABLE);
                    return new AccountNotFoundException();
                });

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.forPaymentCancellation(
                orderId,
                idempotencyKey,
                List.of(
                        RecordLedgerEntryCommand.EntryLine.debit(sellerPayable.getId(), cancelAmount),
                        RecordLedgerEntryCommand.EntryLine.credit(pgReceivable.getId(), cancelAmount)
                )
        );

        record(command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordPgSettlement(Long settlementId, long totalAmount, long pgFeeAmount) {
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_CASH);
                    return new AccountNotFoundException();
                });
        Account pgFeeAccount = findAccountPort.findByName(ACCOUNT_PG_FEE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_PG_FEE);
                    return new AccountNotFoundException();
                });
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_PG_RECEIVABLE);
                    return new AccountNotFoundException();
                });

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        long cashAmount = totalAmount - pgFeeAmount;
        entries.add(RecordLedgerEntryCommand.EntryLine.debit(cashAccount.getId(), cashAmount));

        if (pgFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.debit(pgFeeAccount.getId(), pgFeeAmount));
        }

        entries.add(RecordLedgerEntryCommand.EntryLine.credit(pgReceivable.getId(), totalAmount));

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.forPgSettlement(settlementId, entries);

        record(command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = READ_COMMITTED)
    public void recordSellerSettlement(
            Long settlementId, long totalAmount, long sellerPayoutAmount, long platformFeeAmount
    ) {
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_SELLER_PAYABLE);
                    return new AccountNotFoundException();
                });
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_CASH);
                    return new AccountNotFoundException();
                });
        Account platformFeeAccount = findAccountPort.findByName(ACCOUNT_PLATFORM_FEE_REVENUE)
                .orElseThrow(() -> {
                    log.warn("계정과목을 찾을 수 없습니다. accountName={}", ACCOUNT_PLATFORM_FEE_REVENUE);
                    return new AccountNotFoundException();
                });

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        entries.add(RecordLedgerEntryCommand.EntryLine.debit(sellerPayable.getId(), totalAmount));

        entries.add(RecordLedgerEntryCommand.EntryLine.credit(cashAccount.getId(), sellerPayoutAmount));

        if (platformFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.credit(platformFeeAccount.getId(), platformFeeAmount));
        }

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.forSellerSettlement(settlementId, entries);

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
                    .orElseThrow(() -> {
                        log.warn("계정과목을 찾을 수 없습니다. accountId={}", entryLine.accountId());
                        return new AccountNotFoundException();
                    });
            if (!account.isActive()) {
                throw new InactiveAccountException(account.getName());
            }
        }
    }

    private List<LedgerEntry> createEntries(List<RecordLedgerEntryCommand.EntryLine> entryLines) {
        List<LedgerEntry> entries = new ArrayList<>();
        for (RecordLedgerEntryCommand.EntryLine entryLine : entryLines) {
            LedgerEntry entry = LedgerEntry.from(LedgerEntryCreateState.of(
                    entryLine.accountId(), entryLine.amount(), entryLine.transactionType()
            ));
            entries.add(entry);
        }
        return entries;
    }

    private LedgerTransaction createTransaction(RecordLedgerEntryCommand command) {
        return LedgerTransaction.from(LedgerTransactionCreateState.forTransaction(
                command.transactionType(),
                command.targetType(),
                command.targetId(),
                command.description(),
                IdempotencyKey.of(command.idempotencyKey())
        ));
    }
}
