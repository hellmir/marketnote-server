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
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, LedgerPersistenceAdapter.class})
@DisplayName("LedgerPersistenceAdapter 통합 테스트")
class LedgerPersistenceAdapterTest {

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

    private LedgerTransaction buildTransaction(String idempotencyKey) {
        return LedgerTransaction.from(
                LedgerTransactionCreateState.builder()
                        .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                        .targetType("ORDER")
                        .targetId(1L)
                        .description("결제 승인")
                        .idempotencyKey(IdempotencyKey.of(idempotencyKey))
                        .build()
        );
    }

    private LedgerEntry buildEntry(Long accountId, Long amount, TransactionType type) {
        return LedgerEntry.from(
                LedgerEntryCreateState.builder()
                        .accountId(accountId)
                        .amount(amount)
                        .transactionType(type)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("거래를 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSaveTransaction() {
            // given
            LedgerTransaction transaction = buildTransaction("PAYMENT_APPROVAL:1");

            // when
            LedgerTransaction saved = adapter.save(transaction);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getIdempotencyKey().getValue()).isEqualTo("PAYMENT_APPROVAL:1");
            assertThat(saved.getTransactionType()).isEqualTo(LedgerTransactionType.PAYMENT_APPROVAL);
            assertThat(saved.getTargetType()).isEqualTo("ORDER");
            assertThat(saved.getTargetId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("동일한 idempotencyKey 거래 중복 시 DuplicateLedgerTransactionException을 던진다")
        void shouldThrowOnDuplicate() {
            // given
            adapter.save(buildTransaction("PAYMENT_APPROVAL:1"));
            entityManager.clear();

            // when & then
            assertThatThrownBy(() -> adapter.save(buildTransaction("PAYMENT_APPROVAL:1")))
                    .isInstanceOf(DuplicateLedgerTransactionException.class);
        }
    }

    @Nested
    @DisplayName("existsByIdempotencyKey")
    class ExistsByIdempotencyKey {

        @Test
        @DisplayName("동일 키가 존재하면 true를 반환한다")
        void shouldReturnTrueWhenExists() {
            // given
            adapter.save(buildTransaction("PAYMENT_APPROVAL:42"));
            entityManager.flush();
            entityManager.clear();

            // when
            boolean exists = adapter.existsByIdempotencyKey("PAYMENT_APPROVAL:42");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("동일 키가 없으면 false를 반환한다")
        void shouldReturnFalseWhenNotExists() {
            // when
            boolean exists = adapter.existsByIdempotencyKey("PAYMENT_APPROVAL:999");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("saveAll")
    class SaveAll {

        @Test
        @DisplayName("분개 항목 목록을 저장한다")
        void shouldSaveAllEntries() {
            // given
            LedgerTransaction saved = adapter.save(buildTransaction("PAYMENT_APPROVAL:5"));
            List<LedgerEntry> entries = List.of(
                    buildEntry(1L, 10000L, TransactionType.DEBIT),
                    buildEntry(2L, 10000L, TransactionType.CREDIT)
            );
            entries.forEach(entry -> entry.assignTransaction(saved.getId()));

            // when
            adapter.saveAll(entries);
            entityManager.flush();
            entityManager.clear();

            // then
            assertThat(ledgerEntryJpaRepository.findAll()).hasSize(2);
        }
    }
}
