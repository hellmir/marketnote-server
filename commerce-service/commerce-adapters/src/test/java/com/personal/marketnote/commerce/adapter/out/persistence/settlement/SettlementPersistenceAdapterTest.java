package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository.SettlementJpaRepository;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementCreateState;
import com.personal.marketnote.commerce.domain.settlement.SettlementSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, SettlementPersistenceAdapter.class})
@DisplayName("SettlementPersistenceAdapter 통합 테스트")
class SettlementPersistenceAdapterTest {

    @Autowired
    private SettlementPersistenceAdapter adapter;

    @Autowired
    private SettlementJpaRepository settlementJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        settlementJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Settlement createAndSaveSettlement(Long sellerId, Integer year, Integer month, Long totalAllocated) {
        long shippingFee = 3000L;
        long pgFee = 1000L;
        long platformFee = 2000L;
        Settlement settlement = Settlement.from(
                SettlementCreateState.builder()
                        .sellerId(sellerId)
                        .year(year)
                        .month(month)
                        .totalAllocatedAmount(totalAllocated)
                        .shippingFee(shippingFee)
                        .pgFeeAmount(pgFee)
                        .platformFeeAmount(platformFee)
                        .sellerPayoutAmount(totalAllocated + shippingFee - pgFee - platformFee)
                        .build()
        );
        return adapter.save(settlement);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("정산을 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSaveSettlementAndReturnWithId() {
            // when
            Settlement saved = createAndSaveSettlement(1L, 2026, 4, 100000L);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getSellerId()).isEqualTo(1L);
            assertThat(saved.getYear()).isEqualTo(2026);
            assertThat(saved.getMonth()).isEqualTo(4);
            assertThat(saved.getTotalAllocatedAmount().getValue()).isEqualTo(100000L);
            assertThat(saved.getShippingFee().getValue()).isEqualTo(3000L);
            assertThat(saved.getPgFeeAmount().getValue()).isEqualTo(1000L);
            assertThat(saved.getPlatformFeeAmount().getValue()).isEqualTo(2000L);
            assertThat(saved.getSellerPayoutAmount().getValue()).isEqualTo(100000L);
            assertThat(saved.isPending()).isTrue();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("ID로 정산을 조회한다")
        void shouldFindById() {
            // given
            Settlement saved = createAndSaveSettlement(1L, 2026, 4, 100000L);

            // when
            Optional<Settlement> found = adapter.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getSellerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<Settlement> found = adapter.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySellerIdAndYearAndMonth")
    class FindBySellerIdAndYearAndMonth {

        @Test
        @DisplayName("판매자 ID와 연/월로 정산을 조회한다")
        void shouldFindBySellerIdAndYearAndMonth() {
            // given
            createAndSaveSettlement(1L, 2026, 4, 100000L);

            // when
            Optional<Settlement> found = adapter.findBySellerIdAndYearAndMonth(1L, 2026, 4);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getSellerId()).isEqualTo(1L);
            assertThat(found.get().getYear()).isEqualTo(2026);
            assertThat(found.get().getMonth()).isEqualTo(4);
        }

        @Test
        @DisplayName("존재하지 않는 조합으로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<Settlement> found = adapter.findBySellerIdAndYearAndMonth(999L, 2026, 4);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByYearAndMonth")
    class FindAllByYearAndMonth {

        @Test
        @DisplayName("연/월로 모든 정산을 조회한다")
        void shouldFindAllByYearAndMonth() {
            // given
            createAndSaveSettlement(1L, 2026, 4, 100000L);
            createAndSaveSettlement(2L, 2026, 4, 200000L);
            createAndSaveSettlement(3L, 2026, 5, 300000L);

            // when
            List<Settlement> result = adapter.findAllByYearAndMonth(2026, 4);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Settlement::getSellerId).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("결과가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoneFound() {
            // when
            List<Settlement> result = adapter.findAllByYearAndMonth(1999, 1);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsBySellerIdAndYearAndMonth")
    class ExistsBySellerIdAndYearAndMonth {

        @Test
        @DisplayName("정산이 존재하면 true를 반환한다")
        void shouldReturnTrueWhenExists() {
            // given
            createAndSaveSettlement(1L, 2026, 4, 100000L);

            // when
            boolean exists = adapter.existsBySellerIdAndYearAndMonth(1L, 2026, 4);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("정산이 존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenNotExists() {
            // when
            boolean exists = adapter.existsBySellerIdAndYearAndMonth(999L, 2026, 4);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllBySellerIdAndYear")
    class FindAllBySellerIdAndYear {

        @Test
        @DisplayName("판매자 ID와 연도로 모든 정산을 조회한다")
        void shouldFindAllBySellerIdAndYear() {
            // given
            createAndSaveSettlement(1L, 2026, 4, 100000L);
            createAndSaveSettlement(1L, 2026, 5, 150000L);
            createAndSaveSettlement(1L, 2025, 12, 200000L);
            createAndSaveSettlement(2L, 2026, 4, 300000L);

            // when
            List<Settlement> result = adapter.findAllBySellerIdAndYear(1L, 2026);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Settlement::getMonth).containsExactlyInAnyOrder(4, 5);
        }
    }

    @Nested
    @DisplayName("findAllByStatus")
    class FindAllByStatus {

        @Test
        @DisplayName("상태로 정산 목록을 조회한다")
        void shouldFindAllByStatus() {
            // given
            createAndSaveSettlement(1L, 2026, 4, 100000L);
            createAndSaveSettlement(2L, 2026, 4, 200000L);

            // when
            List<Settlement> result = adapter.findAllByStatus(SettlementStatus.PENDING);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(Settlement::isPending);
        }

        @Test
        @DisplayName("해당 상태가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoneFound() {
            // when
            List<Settlement> result = adapter.findAllByStatus(SettlementStatus.COMPLETED);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("정산 상태를 COMPLETED로 업데이트한다")
        void shouldUpdateSettlementStatus() {
            // given
            Settlement saved = createAndSaveSettlement(1L, 2026, 4, 100000L);
            entityManager.flush();
            entityManager.clear();

            Settlement toUpdate = Settlement.from(
                    SettlementSnapshotState.builder()
                            .id(saved.getId())
                            .sellerId(saved.getSellerId())
                            .year(saved.getYear())
                            .month(saved.getMonth())
                            .totalAllocatedAmount(saved.getTotalAllocatedAmount().getValue())
                            .shippingFee(saved.getShippingFee().getValue())
                            .pgFeeAmount(saved.getPgFeeAmount().getValue())
                            .platformFeeAmount(saved.getPlatformFeeAmount().getValue())
                            .sellerPayoutAmount(saved.getSellerPayoutAmount().getValue())
                            .status(SettlementStatus.COMPLETED)
                            .version(saved.getVersion())
                            .createdAt(saved.getCreatedAt())
                            .modifiedAt(saved.getModifiedAt())
                            .build()
            );

            // when
            adapter.update(toUpdate);
            entityManager.flush();
            entityManager.clear();

            // then
            Settlement found = adapter.findById(saved.getId()).orElseThrow();
            assertThat(found.isCompleted()).isTrue();
        }
    }
}
