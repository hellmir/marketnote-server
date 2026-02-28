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

    @Nested
    @DisplayName("결제 취소/환불 역분개 편의 메서드")
    class RecordPaymentCancellationTest {

        @Test
        @DisplayName("결제 취소 역분개 시 미지급금_판매자 차변, 매출채권_PG 대변으로 기록된다")
        void shouldRecordReverseLedgerEntriesForCancellation() {
            // given
            Account pgReceivable = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);
            Account sellerPayable = createActiveAccount(3L, "미지급금_판매자", AccountType.LIABILITY);

            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(pgReceivable));
            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.of(sellerPayable));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(3L)).thenReturn(Optional.of(sellerPayable));
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(pgReceivable));
            when(saveLedgerTransactionPort.save(any())).thenAnswer(invocation -> {
                LedgerTransaction tx = invocation.getArgument(0);
                return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                        .id(300L)
                        .transactionType(tx.getTransactionType())
                        .targetType(tx.getTargetType())
                        .targetId(tx.getTargetId())
                        .description(tx.getDescription())
                        .idempotencyKey(tx.getIdempotencyKey())
                        .build());
            });

            // when
            recordLedgerEntryService.recordPaymentCancellation(1L, 50000L, "PAYMENT_CANCELLATION:1");

            // then
            verify(saveLedgerTransactionPort).save(argThat(tx ->
                    tx.getTransactionType() == LedgerTransactionType.PAYMENT_CANCELLATION
                            && "PAYMENT_CANCELLATION:1".equals(tx.getIdempotencyKey())
                            && "PAYMENT".equals(tx.getTargetType())
            ));

            ArgumentCaptor<List<LedgerEntry>> entriesCaptor = ArgumentCaptor.forClass(List.class);
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());

            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(2);

            LedgerEntry debitEntry = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit())
                    .findFirst().orElseThrow();
            assertThat(debitEntry.getAccountId()).isEqualTo(3L);
            assertThat(debitEntry.getAmount()).isEqualTo(50000L);

            LedgerEntry creditEntry = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isCredit())
                    .findFirst().orElseThrow();
            assertThat(creditEntry.getAccountId()).isEqualTo(1L);
            assertThat(creditEntry.getAmount()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("부분 환불 역분개 시 환불 금액만큼만 기록된다")
        void shouldRecordPartialRefundWithCorrectAmount() {
            // given
            Account pgReceivable = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);
            Account sellerPayable = createActiveAccount(3L, "미지급금_판매자", AccountType.LIABILITY);

            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(pgReceivable));
            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.of(sellerPayable));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(3L)).thenReturn(Optional.of(sellerPayable));
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(pgReceivable));
            when(saveLedgerTransactionPort.save(any())).thenAnswer(invocation -> {
                LedgerTransaction tx = invocation.getArgument(0);
                return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                        .id(301L)
                        .transactionType(tx.getTransactionType())
                        .targetType(tx.getTargetType())
                        .targetId(tx.getTargetId())
                        .description(tx.getDescription())
                        .idempotencyKey(tx.getIdempotencyKey())
                        .build());
            });

            // when
            recordLedgerEntryService.recordPaymentCancellation(1L, 20000L, "PAYMENT_PARTIAL_REFUND:1:20000:0");

            // then
            verify(saveLedgerTransactionPort).save(argThat(tx ->
                    tx.getTransactionType() == LedgerTransactionType.PAYMENT_CANCELLATION
                            && "PAYMENT_PARTIAL_REFUND:1:20000:0".equals(tx.getIdempotencyKey())
            ));

            ArgumentCaptor<List<LedgerEntry>> entriesCaptor = ArgumentCaptor.forClass(List.class);
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());

            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(2);
            savedEntries.forEach(entry -> assertThat(entry.getAmount()).isEqualTo(20000L));
        }

        @Test
        @DisplayName("매출채권_PG 계정이 없으면 AccountNotFoundException이 발생한다")
        void shouldThrowWhenPgReceivableNotFoundForCancellation() {
            // given
            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.recordPaymentCancellation(1L, 50000L, "PAYMENT_CANCELLATION:1"))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(saveLedgerTransactionPort, never()).save(any());
        }

        @Test
        @DisplayName("미지급금_판매자 계정이 없으면 AccountNotFoundException이 발생한다")
        void shouldThrowWhenSellerPayableNotFoundForCancellation() {
            // given
            Account pgReceivable = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);
            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(pgReceivable));
            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.recordPaymentCancellation(1L, 50000L, "PAYMENT_CANCELLATION:1"))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(saveLedgerTransactionPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("PG 정산 입금 분개 편의 메서드")
    class RecordPgSettlementTest {

        @Test
        @DisplayName("PG 정산 입금 분개 시 보통예금 차변, PG수수료비용 차변, 매출채권_PG 대변으로 기록된다")
        void shouldRecordPgSettlementWithCorrectAccounts() {
            // given
            Account cashAccount = createActiveAccount(2L, "보통예금", AccountType.ASSET);
            Account pgFeeAccount = createActiveAccount(5L, "PG수수료비용", AccountType.EXPENSE);
            Account pgReceivable = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);

            when(findAccountPort.findByName("보통예금")).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findByName("PG수수료비용")).thenReturn(Optional.of(pgFeeAccount));
            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(pgReceivable));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(2L)).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findById(5L)).thenReturn(Optional.of(pgFeeAccount));
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(pgReceivable));
            when(saveLedgerTransactionPort.save(any())).thenAnswer(invocation -> {
                LedgerTransaction tx = invocation.getArgument(0);
                return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                        .id(400L)
                        .transactionType(tx.getTransactionType())
                        .targetType(tx.getTargetType())
                        .targetId(tx.getTargetId())
                        .description(tx.getDescription())
                        .idempotencyKey(tx.getIdempotencyKey())
                        .build());
            });

            // when
            recordLedgerEntryService.recordPgSettlement(1L, 10000L, 300L);

            // then
            verify(saveLedgerTransactionPort).save(argThat(tx ->
                    tx.getTransactionType() == LedgerTransactionType.PG_SETTLEMENT
                            && "PG_SETTLEMENT:1".equals(tx.getIdempotencyKey())
                            && "SETTLEMENT".equals(tx.getTargetType())
            ));

            ArgumentCaptor<List<LedgerEntry>> entriesCaptor = ArgumentCaptor.forClass(List.class);
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());

            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(3);

            // DEBIT 보통예금 = 10000 - 300 = 9700
            LedgerEntry cashDebit = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit() && e.getAccountId().equals(2L))
                    .findFirst().orElseThrow();
            assertThat(cashDebit.getAmount()).isEqualTo(9700L);

            // DEBIT PG수수료비용 = 300
            LedgerEntry pgFeeDebit = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit() && e.getAccountId().equals(5L))
                    .findFirst().orElseThrow();
            assertThat(pgFeeDebit.getAmount()).isEqualTo(300L);

            // CREDIT 매출채권_PG = 10000
            LedgerEntry pgReceivableCredit = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isCredit())
                    .findFirst().orElseThrow();
            assertThat(pgReceivableCredit.getAccountId()).isEqualTo(1L);
            assertThat(pgReceivableCredit.getAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("PG 수수료 0일 때 보통예금 차변 = totalAmount, PG수수료비용 Entry 생략")
        void shouldSkipPgFeeEntryWhenZero() {
            // given
            Account cashAccount = createActiveAccount(2L, "보통예금", AccountType.ASSET);
            Account pgFeeAccount = createActiveAccount(5L, "PG수수료비용", AccountType.EXPENSE);
            Account pgReceivable = createActiveAccount(1L, "매출채권_PG", AccountType.ASSET);

            when(findAccountPort.findByName("보통예금")).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findByName("PG수수료비용")).thenReturn(Optional.of(pgFeeAccount));
            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(pgReceivable));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(2L)).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findById(1L)).thenReturn(Optional.of(pgReceivable));
            when(saveLedgerTransactionPort.save(any())).thenAnswer(invocation -> {
                LedgerTransaction tx = invocation.getArgument(0);
                return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                        .id(401L)
                        .transactionType(tx.getTransactionType())
                        .targetType(tx.getTargetType())
                        .targetId(tx.getTargetId())
                        .description(tx.getDescription())
                        .idempotencyKey(tx.getIdempotencyKey())
                        .build());
            });

            // when
            recordLedgerEntryService.recordPgSettlement(1L, 10000L, 0L);

            // then
            ArgumentCaptor<List<LedgerEntry>> entriesCaptor = ArgumentCaptor.forClass(List.class);
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());

            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(2); // PG수수료비용 Entry 생략

            LedgerEntry cashDebit = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit())
                    .findFirst().orElseThrow();
            assertThat(cashDebit.getAccountId()).isEqualTo(2L);
            assertThat(cashDebit.getAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("보통예금 계정 미존재 시 AccountNotFoundException이 발생한다")
        void shouldThrowWhenCashAccountNotFound() {
            // given
            when(findAccountPort.findByName("보통예금")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.recordPgSettlement(1L, 10000L, 300L))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(saveLedgerTransactionPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("판매자 정산 분개 편의 메서드")
    class RecordSellerSettlementTest {

        @Test
        @DisplayName("판매자 정산 분개 시 미지급금_판매자 차변, 보통예금 대변, 플랫폼수수료수익 대변으로 기록된다")
        void shouldRecordSellerSettlementWithCorrectAccounts() {
            // given
            Account sellerPayable = createActiveAccount(3L, "미지급금_판매자", AccountType.LIABILITY);
            Account cashAccount = createActiveAccount(2L, "보통예금", AccountType.ASSET);
            Account platformFeeAccount = createActiveAccount(4L, "플랫폼수수료수익", AccountType.REVENUE);

            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.of(sellerPayable));
            when(findAccountPort.findByName("보통예금")).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findByName("플랫폼수수료수익")).thenReturn(Optional.of(platformFeeAccount));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(3L)).thenReturn(Optional.of(sellerPayable));
            when(findAccountPort.findById(2L)).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findById(4L)).thenReturn(Optional.of(platformFeeAccount));
            when(saveLedgerTransactionPort.save(any())).thenAnswer(invocation -> {
                LedgerTransaction tx = invocation.getArgument(0);
                return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                        .id(500L)
                        .transactionType(tx.getTransactionType())
                        .targetType(tx.getTargetType())
                        .targetId(tx.getTargetId())
                        .description(tx.getDescription())
                        .idempotencyKey(tx.getIdempotencyKey())
                        .build());
            });

            // when
            recordLedgerEntryService.recordSellerSettlement(1L, 9700L, 9200L, 500L); // 9700 = 9200 + 500

            // then
            verify(saveLedgerTransactionPort).save(argThat(tx ->
                    tx.getTransactionType() == LedgerTransactionType.SELLER_SETTLEMENT
                            && "SELLER_SETTLEMENT:1".equals(tx.getIdempotencyKey())
                            && "SETTLEMENT".equals(tx.getTargetType())
            ));

            ArgumentCaptor<List<LedgerEntry>> entriesCaptor = ArgumentCaptor.forClass(List.class);
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());

            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(3);

            // 판매자 정산 분개: DEBIT 미지급금(sellerPayout+platformFee) = CREDIT 보통예금(sellerPayout) + CREDIT 플랫폼수수료수익(platformFee)
            LedgerEntry sellerDebit = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit())
                    .findFirst().orElseThrow();
            assertThat(sellerDebit.getAccountId()).isEqualTo(3L);
            assertThat(sellerDebit.getAmount()).isEqualTo(9700L); // sellerPayout + platformFee

            List<LedgerEntry> credits = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isCredit())
                    .toList();
            assertThat(credits).hasSize(2);

            LedgerEntry cashCredit = credits.stream()
                    .filter(e -> e.getAccountId().equals(2L))
                    .findFirst().orElseThrow();
            assertThat(cashCredit.getAmount()).isEqualTo(9200L);

            LedgerEntry platformFeeCredit = credits.stream()
                    .filter(e -> e.getAccountId().equals(4L))
                    .findFirst().orElseThrow();
            assertThat(platformFeeCredit.getAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("플랫폼 수수료 0일 때 플랫폼수수료수익 Entry 생략")
        void shouldSkipPlatformFeeEntryWhenZero() {
            // given
            Account sellerPayable = createActiveAccount(3L, "미지급금_판매자", AccountType.LIABILITY);
            Account cashAccount = createActiveAccount(2L, "보통예금", AccountType.ASSET);
            Account platformFeeAccount = createActiveAccount(4L, "플랫폼수수료수익", AccountType.REVENUE);

            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.of(sellerPayable));
            when(findAccountPort.findByName("보통예금")).thenReturn(Optional.of(cashAccount));
            when(findAccountPort.findByName("플랫폼수수료수익")).thenReturn(Optional.of(platformFeeAccount));
            when(saveLedgerTransactionPort.existsByIdempotencyKey(anyString())).thenReturn(false);
            when(findAccountPort.findById(3L)).thenReturn(Optional.of(sellerPayable));
            when(findAccountPort.findById(2L)).thenReturn(Optional.of(cashAccount));
            when(saveLedgerTransactionPort.save(any())).thenAnswer(invocation -> {
                LedgerTransaction tx = invocation.getArgument(0);
                return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                        .id(501L)
                        .transactionType(tx.getTransactionType())
                        .targetType(tx.getTargetType())
                        .targetId(tx.getTargetId())
                        .description(tx.getDescription())
                        .idempotencyKey(tx.getIdempotencyKey())
                        .build());
            });

            // when — totalAmount = sellerPayout + platformFee = 9500 + 0 = 9500
            recordLedgerEntryService.recordSellerSettlement(1L, 9500L, 9500L, 0L);

            // then
            ArgumentCaptor<List<LedgerEntry>> entriesCaptor = ArgumentCaptor.forClass(List.class);
            verify(saveLedgerEntryPort).saveAll(entriesCaptor.capture());

            List<LedgerEntry> savedEntries = entriesCaptor.getValue();
            assertThat(savedEntries).hasSize(2); // 플랫폼수수료수익 Entry 생략

            LedgerEntry sellerDebit = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isDebit())
                    .findFirst().orElseThrow();
            assertThat(sellerDebit.getAccountId()).isEqualTo(3L);
            assertThat(sellerDebit.getAmount()).isEqualTo(9500L);

            LedgerEntry cashCredit = savedEntries.stream()
                    .filter(e -> e.getTransactionType().isCredit())
                    .findFirst().orElseThrow();
            assertThat(cashCredit.getAccountId()).isEqualTo(2L);
            assertThat(cashCredit.getAmount()).isEqualTo(9500L);
        }

        @Test
        @DisplayName("미지급금_판매자 계정 미존재 시 AccountNotFoundException이 발생한다")
        void shouldThrowWhenSellerPayableNotFound() {
            // given
            when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recordLedgerEntryService.recordSellerSettlement(1L, 9700L, 9200L, 500L))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(saveLedgerTransactionPort, never()).save(any());
        }
    }
}
