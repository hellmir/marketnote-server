package com.personal.marketnote.commerce.service.ledger;

import com.personal.marketnote.commerce.domain.ledger.*;
import com.personal.marketnote.commerce.exception.AccountNotFoundException;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.exception.InactiveAccountException;
import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.ledger.SaveLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.SaveLedgerTransactionPort;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordLedgerEntryUseCase 테스트")
class RecordLedgerEntryUseCaseTest {

    @InjectMocks
    private RecordLedgerEntryService recordLedgerEntryService;

    @Mock
    private FindAccountPort findAccountPort;

    @Mock
    private SaveLedgerTransactionPort saveLedgerTransactionPort;

    @Mock
    private SaveLedgerEntryPort saveLedgerEntryPort;

    @Captor
    private ArgumentCaptor<LedgerTransaction> transactionCaptor;

    @Captor
    private ArgumentCaptor<List<LedgerEntry>> entriesCaptor;

    private Account createActiveAccount(Long id, String name, AccountType type) {
        return Account.from(AccountSnapshotState.builder()
                .id(id)
                .name(name)
                .accountType(type)
                .status(EntityStatus.ACTIVE)
                .build());
    }

    @Nested
    @DisplayName("정상 분개 처리")
    class SuccessfulRecording {

        @Test
        @DisplayName("결제 승인 시 차변/대변 분개를 정상적으로 기록한다")
        void shouldRecordLedgerEntriesForPaymentApproval() {
            // given
            Long pgReceivableAccountId = 1L;
            Long sellerPayableAccountId = 2L;

            Account pgReceivable = createActiveAccount(pgReceivableAccountId, "매출채권_PG", AccountType.ASSET);
            Account sellerPayable = createActiveAccount(sellerPayableAccountId, "미지급금_판매자", AccountType.LIABILITY);

            when(findAccountPort.findById(pgReceivableAccountId)).thenReturn(Optional.of(pgReceivable));
            when(findAccountPort.findById(sellerPayableAccountId)).thenReturn(Optional.of(sellerPayable));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(saveLedgerTransactionPort.save(any(LedgerTransaction.class)))
                    .thenAnswer(invocation -> {
                        LedgerTransaction tx = invocation.getArgument(0);
                        return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                                .id(100L)
                                .transactionType(tx.getTransactionType())
                                .targetType(tx.getTargetType())
                                .targetId(tx.getTargetId())
                                .description(tx.getDescription())
                                .idempotencyKey(tx.getIdempotencyKey())
                                .build());
                    });

            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("주문 1 결제 승인 분개")
                    .idempotencyKey("PAYMENT_APPROVAL:1")
                    .entries(List.of(
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(pgReceivableAccountId)
                                    .amount(10000L)
                                    .transactionType(TransactionType.DEBIT)
                                    .build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(sellerPayableAccountId)
                                    .amount(10000L)
                                    .transactionType(TransactionType.CREDIT)
                                    .build()
                    ))
                    .build();

            // when
            recordLedgerEntryService.record(command);

            // then
            verify(saveLedgerTransactionPort).save(transactionCaptor.capture());
            LedgerTransaction savedTransaction = transactionCaptor.getValue();
            assertThat(savedTransaction.getTransactionType()).isEqualTo(LedgerTransactionType.PAYMENT_APPROVAL);
            assertThat(savedTransaction.getTargetType()).isEqualTo("ORDER");
            assertThat(savedTransaction.getTargetId()).isEqualTo(1L);
            assertThat(savedTransaction.getIdempotencyKey()).isEqualTo("PAYMENT_APPROVAL:1");

            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());
            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(2);

            LedgerEntry debitEntry = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit())
                    .findFirst().orElseThrow();
            assertThat(debitEntry.getAccountId()).isEqualTo(pgReceivableAccountId);
            assertThat(debitEntry.getAmount()).isEqualTo(10000L);
            assertThat(debitEntry.getTransactionId()).isEqualTo(100L);

            LedgerEntry creditEntry = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isCredit())
                    .findFirst().orElseThrow();
            assertThat(creditEntry.getAccountId()).isEqualTo(sellerPayableAccountId);
            assertThat(creditEntry.getAmount()).isEqualTo(10000L);
            assertThat(creditEntry.getTransactionId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("여러 차변/대변 항목이 있는 분개를 정상적으로 기록한다")
        void shouldRecordMultipleEntries() {
            // given
            Account cashAccount = createActiveAccount(1L, "보통예금", AccountType.ASSET);
            Account pgFeeAccount = createActiveAccount(3L, "PG수수료비용", AccountType.EXPENSE);
            Account pgReceivable = createActiveAccount(2L, "매출채권_PG", AccountType.ASSET);

            when(findAccountPort.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findById(3L)).thenReturn(Optional.of(pgFeeAccount));
            when(findAccountPort.findById(2L)).thenReturn(Optional.of(pgReceivable));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(saveLedgerTransactionPort.save(any(LedgerTransaction.class)))
                    .thenAnswer(invocation -> {
                        LedgerTransaction tx = invocation.getArgument(0);
                        return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                                .id(200L)
                                .transactionType(tx.getTransactionType())
                                .targetType(tx.getTargetType())
                                .targetId(tx.getTargetId())
                                .description(tx.getDescription())
                                .idempotencyKey(tx.getIdempotencyKey())
                                .build());
                    });

            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PG_SETTLEMENT)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("PG 정산 입금")
                    .idempotencyKey("PG_SETTLEMENT:1")
                    .entries(List.of(
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(1L).amount(9700L).transactionType(TransactionType.DEBIT).build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(3L).amount(300L).transactionType(TransactionType.DEBIT).build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(2L).amount(10000L).transactionType(TransactionType.CREDIT).build()
                    ))
                    .build();

            // when
            recordLedgerEntryService.record(command);

            // then
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());
            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(3);

            Long debitTotal = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit())
                    .mapToLong(LedgerEntry::getAmount)
                    .sum();
            Long creditTotal = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isCredit())
                    .mapToLong(LedgerEntry::getAmount)
                    .sum();
            assertThat(debitTotal).isEqualTo(creditTotal);
        }
    }

    @Nested
    @DisplayName("멱등성 검증")
    class IdempotencyValidation {

        @Test
        @DisplayName("동일한 멱등성 키로 중복 요청 시 DuplicateLedgerTransactionException을 던진다")
        void shouldThrowWhenDuplicateIdempotencyKey() {
            // given
            when(saveLedgerTransactionPort.existsByIdempotencyKey("PAYMENT_APPROVAL:1")).thenReturn(true);

            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:1")
                    .entries(List.of(
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(1L).amount(10000L).transactionType(TransactionType.DEBIT).build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(2L).amount(10000L).transactionType(TransactionType.CREDIT).build()
                    ))
                    .build();

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.record(command))
                    .isInstanceOf(DuplicateLedgerTransactionException.class)
                    .hasMessageContaining("PAYMENT_APPROVAL:1");

            verify(saveLedgerTransactionPort, never()).save(any());
            verify(saveLedgerEntryPort, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("계정과목 검증")
    class AccountValidation {

        @Test
        @DisplayName("존재하지 않는 계정과목 ID로 분개 시 AccountNotFoundException을 던진다")
        void shouldThrowWhenAccountNotFound() {
            // given
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(1L)).thenReturn(Optional.empty());

            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:1")
                    .entries(List.of(
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(1L).amount(10000L).transactionType(TransactionType.DEBIT).build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(2L).amount(10000L).transactionType(TransactionType.CREDIT).build()
                    ))
                    .build();

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.record(command))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(saveLedgerTransactionPort, never()).save(any());
        }

        @Test
        @DisplayName("비활성 계정과목으로 분개 시 IllegalStateException을 던진다")
        void shouldThrowWhenAccountIsInactive() {
            // given
            Account inactiveAccount = Account.from(AccountSnapshotState.builder()
                    .id(1L)
                    .name("비활성 계정")
                    .accountType(AccountType.ASSET)
                    .status(EntityStatus.INACTIVE)
                    .build());

            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(inactiveAccount));

            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:1")
                    .entries(List.of(
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(1L).amount(10000L).transactionType(TransactionType.DEBIT).build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(2L).amount(10000L).transactionType(TransactionType.CREDIT).build()
                    ))
                    .build();

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.record(command))
                    .isInstanceOf(InactiveAccountException.class)
                    .hasMessageContaining("비활성");

            verify(saveLedgerTransactionPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("차대변 검증")
    class BalanceValidation {

        @Test
        @DisplayName("차변 합계와 대변 합계가 다르면 IllegalStateException을 던진다")
        void shouldThrowWhenDebitNotEqualsCredit() {
            // given
            Account account1 = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);
            Account account2 = createActiveAccount(2L, "미지급금_판매자", AccountType.LIABILITY);

            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(account1));
            when(findAccountPort.findById(2L)).thenReturn(Optional.of(account2));

            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:1")
                    .entries(List.of(
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(1L).amount(10000L).transactionType(TransactionType.DEBIT).build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(2L).amount(5000L).transactionType(TransactionType.CREDIT).build()
                    ))
                    .build();

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.record(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("차변 합계");

            verify(saveLedgerTransactionPort, never()).save(any());
            verify(saveLedgerEntryPort, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("입력값 검증")
    class InputValidation {

        @Test
        @DisplayName("빈 분개 항목 리스트로 요청 시 IllegalArgumentException을 던진다")
        void shouldThrowWhenEntriesEmpty() {
            // given
            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:1")
                    .entries(List.of())
                    .build();

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.record(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어있습니다");

            verify(saveLedgerTransactionPort, never()).save(any());
            verify(saveLedgerEntryPort, never()).saveAll(any());
        }

        @Test
        @DisplayName("음수 금액으로 분개 시 IllegalStateException을 던진다")
        void shouldThrowWhenNegativeAmount() {
            // given
            Account account1 = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);
            Account account2 = createActiveAccount(2L, "미지급금_판매자", AccountType.LIABILITY);

            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(account1));
            when(findAccountPort.findById(2L)).thenReturn(Optional.of(account2));

            RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(1L)
                    .description("결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:1")
                    .entries(List.of(
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(1L).amount(-10000L).transactionType(TransactionType.DEBIT).build(),
                            RecordLedgerEntryCommand.EntryLine.builder()
                                    .accountId(2L).amount(-10000L).transactionType(TransactionType.CREDIT).build()
                    ))
                    .build();

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.record(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("0보다 커야 합니다");

            verify(saveLedgerTransactionPort, never()).save(any());
            verify(saveLedgerEntryPort, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("결제 승인 분개 편의 메서드")
    class RecordPaymentApprovalTest {

        @Test
        @DisplayName("결제 승인 분개 시 매출채권_PG 차변, 미지급금_판매자 대변으로 기록된다")
        void shouldRecordPaymentApprovalWithCorrectAccounts() {
            // given
            Account pgReceivable = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);
            Account sellerPayable = createActiveAccount(3L, "미지급금_판매자", AccountType.LIABILITY);

            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(pgReceivable));
            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.of(sellerPayable));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(pgReceivable));
            when(findAccountPort.findById(3L)).thenReturn(Optional.of(sellerPayable));
            when(saveLedgerTransactionPort.save(any())).thenAnswer(invocation -> {
                LedgerTransaction tx = invocation.getArgument(0);
                return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                        .id(100L)
                        .transactionType(tx.getTransactionType())
                        .targetType(tx.getTargetType())
                        .targetId(tx.getTargetId())
                        .description(tx.getDescription())
                        .idempotencyKey(tx.getIdempotencyKey())
                        .build());
            });

            // when
            recordLedgerEntryService.recordPaymentApproval(1L, 50000L);

            // then
            verify(findAccountPort).findByName("매출채권_PG");
            verify(findAccountPort).findByName("미지급금_판매자");
            verify(saveLedgerTransactionPort).save(argThat(tx ->
                    tx.getTransactionType() == LedgerTransactionType.PAYMENT_APPROVAL
                            && "PAYMENT_APPROVAL:1".equals(tx.getIdempotencyKey())
                            && "PAYMENT".equals(tx.getTargetType())
            ));

            ArgumentCaptor<List<LedgerEntry>> entriesCaptor = ArgumentCaptor.forClass(List.class);
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());

            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(2);

            LedgerEntry debitEntry = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit())
                    .findFirst().orElseThrow();
            assertThat(debitEntry.getAccountId()).isEqualTo(1L);
            assertThat(debitEntry.getAmount()).isEqualTo(50000L);

            LedgerEntry creditEntry = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isCredit())
                    .findFirst().orElseThrow();
            assertThat(creditEntry.getAccountId()).isEqualTo(3L);
            assertThat(creditEntry.getAmount()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("매출채권_PG 계정이 없으면 AccountNotFoundException이 발생한다")
        void shouldThrowWhenPgReceivableAccountNotFound() {
            // given
            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.recordPaymentApproval(1L, 50000L))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(saveLedgerTransactionPort, never()).save(any());
        }

        @Test
        @DisplayName("미지급금_판매자 계정이 없으면 AccountNotFoundException이 발생한다")
        void shouldThrowWhenSellerPayableAccountNotFound() {
            // given
            Account pgReceivable = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);
            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(pgReceivable));
            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.recordPaymentApproval(1L, 50000L))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(saveLedgerTransactionPort, never()).save(any());
        }
    }
}
