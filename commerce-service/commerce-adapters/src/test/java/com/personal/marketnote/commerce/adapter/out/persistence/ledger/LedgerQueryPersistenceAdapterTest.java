package com.personal.marketnote.commerce.adapter.out.persistence.ledger;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.LedgerEntryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.LedgerTransactionJpaRepository;
import com.personal.marketnote.commerce.domain.ledger.IdempotencyKey;
import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;
import com.personal.marketnote.commerce.domain.ledger.LedgerEntryCreateState;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionCreateState;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import com.personal.marketnote.commerce.port.out.ledger.dto.AccountBalanceDto;
import com.personal.marketnote.common.configuration.AuditConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, LedgerQueryPersistenceAdapter.class, LedgerPersistenceAdapter.class})
@DisplayName("LedgerQueryPersistenceAdapter 통합 테스트")
class LedgerQueryPersistenceAdapterTest {

    @Autowired
    private LedgerQueryPersistenceAdapter queryAdapter;

    @Autowired
    private LedgerPersistenceAdapter adapter;

    @Autowired
    private LedgerTransactionJpaRepository ledgerTransactionJpaRepository;

    @Autowired
    private LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        ledgerEntryJpaRepository.deleteAll();
        ledgerTransactionJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private LedgerTransaction saveTransaction(LedgerTransactionType type, String idempotencyKey) {
        LedgerTransaction transaction = LedgerTransaction.from(
                LedgerTransactionCreateState.builder()
                        .transactionType(type)
                        .targetType("ORDER")
                        .targetId(1L)
                        .description("테스트")
                        .idempotencyKey(IdempotencyKey.of(idempotencyKey))
                        .build()
        );
        return adapter.save(transaction);
    }

    private void saveEntries(Long transactionId, Long accountId, Long amount, TransactionType type) {
        LedgerEntry entry = LedgerEntry.from(
                LedgerEntryCreateState.builder()
                        .accountId(accountId)
                        .amount(amount)
                        .transactionType(type)
                        .build()
        );
        entry.assignTransaction(transactionId);
        adapter.saveAll(List.of(entry));
    }

    @Nested
    @DisplayName("findByFilters")
    class FindByFilters {

        @Test
        @DisplayName("거래 유형으로 거래 목록을 조회한다")
        void shouldFilterByType() {
            // given
            saveTransaction(LedgerTransactionType.PAYMENT_APPROVAL, "PAYMENT_APPROVAL:1");
            saveTransaction(LedgerTransactionType.SELLER_SETTLEMENT, "SELLER_SETTLEMENT:1");
            entityManager.flush();
            entityManager.clear();

            // when
            List<LedgerTransaction> result = queryAdapter.findByFilters(null, null, LedgerTransactionType.PAYMENT_APPROVAL);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTransactionType()).isEqualTo(LedgerTransactionType.PAYMENT_APPROVAL);
        }

        @Test
        @DisplayName("필터가 없으면 전체를 createdAt 내림차순으로 반환한다")
        void shouldReturnAllWhenNoFilter() {
            // given
            saveTransaction(LedgerTransactionType.PAYMENT_APPROVAL, "PAYMENT_APPROVAL:1");
            saveTransaction(LedgerTransactionType.PG_SETTLEMENT, "PG_SETTLEMENT:1");
            entityManager.flush();
            entityManager.clear();

            // when
            List<LedgerTransaction> result = queryAdapter.findByFilters(null, null, null);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByTransactionId")
    class FindByTransactionId {

        @Test
        @DisplayName("거래 ID로 분개 항목 목록을 ID 오름차순으로 조회한다")
        void shouldFindEntriesByTransactionId() {
            // given
            LedgerTransaction transaction = saveTransaction(LedgerTransactionType.PAYMENT_APPROVAL, "PAYMENT_APPROVAL:42");
            saveEntries(transaction.getId(), 1L, 10000L, TransactionType.DEBIT);
            saveEntries(transaction.getId(), 2L, 10000L, TransactionType.CREDIT);
            entityManager.flush();
            entityManager.clear();

            // when
            List<LedgerEntry> result = queryAdapter.findByTransactionId(transaction.getId());

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("거래 ID에 분개가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyWhenNoEntries() {
            // when
            List<LedgerEntry> result = queryAdapter.findByTransactionId(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAccountBalanceSummary")
    class FindAccountBalanceSummary {

        @Test
        @DisplayName("asOf 시점까지 계정별 차변/대변 합계를 조회한다")
        void shouldReturnBalanceSummary() {
            // given
            LedgerTransaction tx = saveTransaction(LedgerTransactionType.PAYMENT_APPROVAL, "PAYMENT_APPROVAL:1");
            saveEntries(tx.getId(), 1L, 10000L, TransactionType.DEBIT);
            saveEntries(tx.getId(), 2L, 10000L, TransactionType.CREDIT);
            entityManager.flush();
            entityManager.clear();

            // when
            List<AccountBalanceDto> result = queryAdapter.findAccountBalanceSummary(LocalDateTime.now().plusDays(1));

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(AccountBalanceDto::accountId).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("asOf 시점 이후 분개는 집계에서 제외된다")
        void shouldExcludeEntriesAfterCutoff() {
            // given
            LedgerTransaction tx = saveTransaction(LedgerTransactionType.PAYMENT_APPROVAL, "PAYMENT_APPROVAL:1");
            saveEntries(tx.getId(), 1L, 10000L, TransactionType.DEBIT);
            entityManager.flush();
            entityManager.clear();

            // when
            List<AccountBalanceDto> result = queryAdapter.findAccountBalanceSummary(LocalDateTime.now().minusDays(1));

            // then
            assertThat(result).isEmpty();
        }
    }
}
