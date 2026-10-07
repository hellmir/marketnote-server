package com.personal.marketnote.commerce.adapter.out.persistence.ledger;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.AccountJpaRepository;
import com.personal.marketnote.commerce.domain.ledger.Account;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, AccountPersistenceAdapter.class})
@DisplayName("AccountPersistenceAdapter 통합 테스트")
class AccountPersistenceAdapterTest {

    @Autowired
    private AccountPersistenceAdapter adapter;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        accountJpaRepository.deleteAll();
        entityManager.createNativeQuery(
                "INSERT INTO account (id, name, account_type, status, created_at) " +
                        "VALUES (1, 'CASH', 'ASSET', 'ACTIVE', CURRENT_TIMESTAMP)"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO account (id, name, account_type, status, created_at) " +
                        "VALUES (2, 'SALES_REVENUE', 'REVENUE', 'ACTIVE', CURRENT_TIMESTAMP)"
        ).executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("ID로 계정을 조회한다")
        void shouldFindById() {
            // when
            Optional<Account> found = adapter.findById(1L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("CASH");
            assertThat(found.get().getAccountType().isAsset()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<Account> found = adapter.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("이름으로 계정을 조회한다")
        void shouldFindByName() {
            // when
            Optional<Account> found = adapter.findByName("SALES_REVENUE");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(2L);
            assertThat(found.get().getAccountType().isRevenue()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이름으로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNameNotFound() {
            // when
            Optional<Account> found = adapter.findByName("UNKNOWN_ACCOUNT");

            // then
            assertThat(found).isEmpty();
        }
    }
}
