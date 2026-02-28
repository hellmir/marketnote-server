package com.personal.marketnote.commerce.domain.ledger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LedgerTransaction 도메인 테스트")
class LedgerTransactionTest {

    @Nested
    @DisplayName("validateEntries")
    class ValidateEntries {

        @Test
        @DisplayName("차변 합계와 대변 합계가 같으면 검증을 통과한다")
        void shouldPassWhenDebitEqualsCreditTotal() {
            // given
            LedgerTransaction transaction = LedgerTransaction.from(
                    LedgerTransactionCreateState.builder()
                            .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                            .targetType("ORDER")
                            .targetId(1L)
                            .description("결제 승인")
                            .idempotencyKey("PAYMENT_APPROVAL:1")
                            .build()
            );

            List<LedgerEntry> entries = List.of(
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(1L)
                            .amount(10000L)
                            .transactionType(TransactionType.DEBIT)
                            .build()),
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(2L)
                            .amount(10000L)
                            .transactionType(TransactionType.CREDIT)
                            .build())
            );

            // when & then
            transaction.validateEntries(entries);
        }

        @Test
        @DisplayName("차변 합계와 대변 합계가 다르면 예외를 던진다")
        void shouldThrowWhenDebitNotEqualsCredit() {
            // given
            LedgerTransaction transaction = LedgerTransaction.from(
                    LedgerTransactionCreateState.builder()
                            .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                            .targetType("ORDER")
                            .targetId(1L)
                            .description("결제 승인")
                            .idempotencyKey("PAYMENT_APPROVAL:1")
                            .build()
            );

            List<LedgerEntry> entries = List.of(
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(1L)
                            .amount(10000L)
                            .transactionType(TransactionType.DEBIT)
                            .build()),
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(2L)
                            .amount(5000L)
                            .transactionType(TransactionType.CREDIT)
                            .build())
            );

            // when & then
            assertThatThrownBy(() -> transaction.validateEntries(entries))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("차변 합계")
                    .hasMessageContaining("대변 합계");
        }

        @Test
        @DisplayName("분개 금액이 0원이면 예외를 던진다")
        void shouldThrowWhenAmountIsZero() {
            // given
            LedgerTransaction transaction = LedgerTransaction.from(
                    LedgerTransactionCreateState.builder()
                            .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                            .targetType("ORDER")
                            .targetId(1L)
                            .description("결제 승인")
                            .idempotencyKey("PAYMENT_APPROVAL:1")
                            .build()
            );

            List<LedgerEntry> entries = List.of(
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(1L)
                            .amount(0L)
                            .transactionType(TransactionType.DEBIT)
                            .build()),
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(2L)
                            .amount(0L)
                            .transactionType(TransactionType.CREDIT)
                            .build())
            );

            // when & then
            assertThatThrownBy(() -> transaction.validateEntries(entries))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("음수 금액이 포함되면 예외를 던진다")
        void shouldThrowWhenNegativeAmount() {
            // given
            LedgerTransaction transaction = LedgerTransaction.from(
                    LedgerTransactionCreateState.builder()
                            .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                            .targetType("ORDER")
                            .targetId(1L)
                            .description("결제 승인")
                            .idempotencyKey("PAYMENT_APPROVAL:1")
                            .build()
            );

            List<LedgerEntry> entries = List.of(
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(1L)
                            .amount(-5000L)
                            .transactionType(TransactionType.DEBIT)
                            .build()),
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(2L)
                            .amount(-5000L)
                            .transactionType(TransactionType.CREDIT)
                            .build())
            );

            // when & then
            assertThatThrownBy(() -> transaction.validateEntries(entries))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("여러 차변/대변 항목의 합계가 같으면 검증을 통과한다")
        void shouldPassWithMultipleEntriesWhenTotalsMatch() {
            // given
            LedgerTransaction transaction = LedgerTransaction.from(
                    LedgerTransactionCreateState.builder()
                            .transactionType(LedgerTransactionType.PG_SETTLEMENT)
                            .targetType("ORDER")
                            .targetId(1L)
                            .description("PG 정산")
                            .idempotencyKey("PG_SETTLEMENT:1")
                            .build()
            );

            List<LedgerEntry> entries = List.of(
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(1L)
                            .amount(9700L)
                            .transactionType(TransactionType.DEBIT)
                            .build()),
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(3L)
                            .amount(300L)
                            .transactionType(TransactionType.DEBIT)
                            .build()),
                    LedgerEntry.from(LedgerEntryCreateState.builder()
                            .accountId(2L)
                            .amount(10000L)
                            .transactionType(TransactionType.CREDIT)
                            .build())
            );

            // when & then
            transaction.validateEntries(entries);
        }
    }

    @Nested
    @DisplayName("from CreateState")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로부터 LedgerTransaction을 생성한다")
        void shouldCreateFromCreateState() {
            // given
            LedgerTransactionCreateState state = LedgerTransactionCreateState.builder()
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("ORDER")
                    .targetId(100L)
                    .description("주문 100 결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:100")
                    .build();

            // when
            LedgerTransaction transaction = LedgerTransaction.from(state);

            // then
            assertThat(transaction.getTransactionType()).isEqualTo(LedgerTransactionType.PAYMENT_APPROVAL);
            assertThat(transaction.getTargetType()).isEqualTo("ORDER");
            assertThat(transaction.getTargetId()).isEqualTo(100L);
            assertThat(transaction.getDescription()).isEqualTo("주문 100 결제 승인");
            assertThat(transaction.getIdempotencyKey()).isEqualTo("PAYMENT_APPROVAL:100");
        }
    }
}
