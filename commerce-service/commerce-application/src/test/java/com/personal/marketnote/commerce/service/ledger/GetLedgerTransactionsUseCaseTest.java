package com.personal.marketnote.commerce.service.ledger;

import com.personal.marketnote.commerce.domain.ledger.*;
import com.personal.marketnote.commerce.port.in.command.ledger.GetLedgerTransactionsQuery;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerTransactionResult;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerTransactionPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLedgerTransactionsUseCase 테스트")
class GetLedgerTransactionsUseCaseTest {

    @InjectMocks
    private GetLedgerTransactionsService getLedgerTransactionsService;

    @Mock
    private FindLedgerTransactionPort findLedgerTransactionPort;

    @Mock
    private FindLedgerEntryPort findLedgerEntryPort;

    private LedgerTransaction createTransaction(Long id, LedgerTransactionType type) {
        return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                .id(id)
                .transactionType(type)
                .targetType("SETTLEMENT")
                .targetId(100L)
                .description("테스트 거래")
                .idempotencyKey("TEST:" + id)
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    private LedgerEntry createEntry(Long id, Long transactionId, Long accountId, TransactionType type, Long amount) {
        return LedgerEntry.from(LedgerEntrySnapshotState.builder()
                .id(id)
                .accountId(accountId)
                .transactionId(transactionId)
                .amount(amount)
                .transactionType(type)
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    @Test
    @DisplayName("기간별 필터로 거래 목록을 조회한다")
    void shouldGetTransactionsByDateRange() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 2, 28, 23, 59, 59);

        LedgerTransaction transaction = createTransaction(1L, LedgerTransactionType.PG_SETTLEMENT);
        when(findLedgerTransactionPort.findByFilters(startDate, endDate, null))
                .thenReturn(List.of(transaction));

        LedgerEntry entry = createEntry(1L, 1L, 1L, TransactionType.DEBIT, 10000L);
        when(findLedgerEntryPort.findByTransactionId(1L))
                .thenReturn(List.of(entry));

        GetLedgerTransactionsQuery query = GetLedgerTransactionsQuery.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // when
        List<GetLedgerTransactionResult> results = getLedgerTransactionsService.getLedgerTransactions(query);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(1L);
        assertThat(results.get(0).transactionType()).isEqualTo(LedgerTransactionType.PG_SETTLEMENT);
        assertThat(results.get(0).entries()).hasSize(1);
        assertThat(results.get(0).entries().get(0).amount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("거래 유형별 필터로 거래 목록을 조회한다")
    void shouldGetTransactionsByType() {
        // given
        LedgerTransaction transaction = createTransaction(1L, LedgerTransactionType.SELLER_SETTLEMENT);
        when(findLedgerTransactionPort.findByFilters(null, null, LedgerTransactionType.SELLER_SETTLEMENT))
                .thenReturn(List.of(transaction));
        when(findLedgerEntryPort.findByTransactionId(1L))
                .thenReturn(List.of());

        GetLedgerTransactionsQuery query = GetLedgerTransactionsQuery.builder()
                .transactionType(LedgerTransactionType.SELLER_SETTLEMENT)
                .build();

        // when
        List<GetLedgerTransactionResult> results = getLedgerTransactionsService.getLedgerTransactions(query);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).transactionType()).isEqualTo(LedgerTransactionType.SELLER_SETTLEMENT);
    }

    @Test
    @DisplayName("필터 없이 전체 거래 목록을 조회한다")
    void shouldGetAllTransactionsWithoutFilter() {
        // given
        LedgerTransaction tx1 = createTransaction(1L, LedgerTransactionType.PG_SETTLEMENT);
        LedgerTransaction tx2 = createTransaction(2L, LedgerTransactionType.SELLER_SETTLEMENT);
        when(findLedgerTransactionPort.findByFilters(null, null, null))
                .thenReturn(List.of(tx1, tx2));
        when(findLedgerEntryPort.findByTransactionId(1L)).thenReturn(List.of());
        when(findLedgerEntryPort.findByTransactionId(2L)).thenReturn(List.of());

        GetLedgerTransactionsQuery query = GetLedgerTransactionsQuery.builder().build();

        // when
        List<GetLedgerTransactionResult> results = getLedgerTransactionsService.getLedgerTransactions(query);

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
    void shouldReturnEmptyListWhenNoTransactions() {
        // given
        when(findLedgerTransactionPort.findByFilters(null, null, null))
                .thenReturn(List.of());

        GetLedgerTransactionsQuery query = GetLedgerTransactionsQuery.builder().build();

        // when
        List<GetLedgerTransactionResult> results = getLedgerTransactionsService.getLedgerTransactions(query);

        // then
        assertThat(results).isEmpty();
        verify(findLedgerEntryPort, never()).findByTransactionId(any());
    }

    @Test
    @DisplayName("거래에 포함된 복수 분개를 함께 조회한다")
    void shouldGetTransactionWithMultipleEntries() {
        // given
        LedgerTransaction transaction = createTransaction(1L, LedgerTransactionType.PG_SETTLEMENT);
        when(findLedgerTransactionPort.findByFilters(null, null, null))
                .thenReturn(List.of(transaction));

        LedgerEntry debitEntry = createEntry(1L, 1L, 1L, TransactionType.DEBIT, 9700L);
        LedgerEntry debitFeeEntry = createEntry(2L, 1L, 2L, TransactionType.DEBIT, 300L);
        LedgerEntry creditEntry = createEntry(3L, 1L, 3L, TransactionType.CREDIT, 10000L);
        when(findLedgerEntryPort.findByTransactionId(1L))
                .thenReturn(List.of(debitEntry, debitFeeEntry, creditEntry));

        GetLedgerTransactionsQuery query = GetLedgerTransactionsQuery.builder().build();

        // when
        List<GetLedgerTransactionResult> results = getLedgerTransactionsService.getLedgerTransactions(query);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).entries()).hasSize(3);
    }
}
