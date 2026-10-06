package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository.SettlementPolicyJpaRepository;
import com.personal.marketnote.commerce.domain.settlement.FeeRate;
import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicyCreateState;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, SettlementPolicyPersistenceAdapter.class})
@DisplayName("SettlementPolicyPersistenceAdapter 통합 테스트")
class SettlementPolicyPersistenceAdapterTest {

    @Autowired
    private SettlementPolicyPersistenceAdapter adapter;

    @Autowired
    private SettlementPolicyJpaRepository settlementPolicyJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        settlementPolicyJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private SettlementPolicy createPolicy(Long sellerId) {
        return SettlementPolicy.from(
                SettlementPolicyCreateState.builder()
                        .sellerId(sellerId)
                        .pgFeeRate(FeeRate.of(300))
                        .platformFeeRate(FeeRate.of(500))
                        .settlementCycle(SettlementCycle.MONTHLY)
                        .minPayoutAmount(10000L)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("정산 정책을 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSavePolicy() {
            // when
            SettlementPolicy saved = adapter.save(createPolicy(1L));

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getSellerId()).isEqualTo(1L);
            assertThat(saved.getPgFeeRate().getValue()).isEqualTo(300);
            assertThat(saved.getPlatformFeeRate().getValue()).isEqualTo(500);
            assertThat(saved.getSettlementCycle()).isEqualTo(SettlementCycle.MONTHLY);
            assertThat(saved.getMinPayoutAmount().getValue()).isEqualTo(10000L);
            assertThat(saved.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("ID로 정산 정책을 조회한다")
        void shouldFindById() {
            // given
            SettlementPolicy saved = adapter.save(createPolicy(1L));
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<SettlementPolicy> found = adapter.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getSellerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<SettlementPolicy> found = adapter.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveBySellerId")
    class FindActiveBySellerId {

        @Test
        @DisplayName("ACTIVE 상태 정책을 판매자 ID로 조회한다")
        void shouldFindActiveBySellerId() {
            // given
            adapter.save(createPolicy(1L));
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<SettlementPolicy> found = adapter.findActiveBySellerId(1L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getSellerId()).isEqualTo(1L);
            assertThat(found.get().isActive()).isTrue();
        }

        @Test
        @DisplayName("INACTIVE 상태 정책은 조회되지 않는다")
        void shouldNotReturnInactivePolicy() {
            // given
            SettlementPolicy saved = adapter.save(createPolicy(1L));
            saved.deactivate();
            adapter.update(saved);
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<SettlementPolicy> found = adapter.findActiveBySellerId(1L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("모든 정산 정책을 조회한다")
        void shouldFindAll() {
            // given
            adapter.save(createPolicy(1L));
            adapter.save(createPolicy(2L));
            entityManager.flush();
            entityManager.clear();

            // when
            List<SettlementPolicy> result = adapter.findAll();

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findActiveBySellerIdIn")
    class FindActiveBySellerIdIn {

        @Test
        @DisplayName("판매자 ID 집합으로 ACTIVE 정책 맵을 조회한다")
        void shouldFindMapBySellerIds() {
            // given
            adapter.save(createPolicy(1L));
            adapter.save(createPolicy(2L));
            adapter.save(createPolicy(3L));
            entityManager.flush();
            entityManager.clear();

            // when
            Map<Long, SettlementPolicy> result = adapter.findActiveBySellerIdIn(List.of(1L, 2L));

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsOnlyKeys(1L, 2L);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("정산 정책을 업데이트한다")
        void shouldUpdatePolicy() {
            // given
            SettlementPolicy saved = adapter.save(createPolicy(1L));
            entityManager.flush();
            entityManager.clear();

            saved.update(FeeRate.of(400), FeeRate.of(600), SettlementCycle.WEEKLY, 20000L);

            // when
            adapter.update(saved);
            entityManager.flush();
            entityManager.clear();

            // then
            SettlementPolicy found = adapter.findById(saved.getId()).orElseThrow();
            assertThat(found.getPgFeeRate().getValue()).isEqualTo(400);
            assertThat(found.getPlatformFeeRate().getValue()).isEqualTo(600);
            assertThat(found.getSettlementCycle()).isEqualTo(SettlementCycle.WEEKLY);
            assertThat(found.getMinPayoutAmount().getValue()).isEqualTo(20000L);
        }
    }
}
