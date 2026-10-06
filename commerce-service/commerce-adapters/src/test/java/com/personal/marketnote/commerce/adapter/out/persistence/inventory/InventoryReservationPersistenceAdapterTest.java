package com.personal.marketnote.commerce.adapter.out.persistence.inventory;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryReservationJpaRepository;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.commerce.domain.inventory.InventoryReservationCreateState;
import com.personal.marketnote.commerce.exception.DuplicateInventoryReservationException;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, InventoryReservationPersistenceAdapter.class})
@DisplayName("InventoryReservationPersistenceAdapter 통합 테스트")
class InventoryReservationPersistenceAdapterTest {

    @Autowired
    private InventoryReservationPersistenceAdapter adapter;

    @Autowired
    private InventoryReservationJpaRepository inventoryReservationJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        inventoryReservationJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private InventoryReservation buildReservation(Long orderId, Long pricePolicyId, int quantity, LocalDateTime reservedAt) {
        return InventoryReservation.from(
                InventoryReservationCreateState.builder()
                        .orderId(orderId)
                        .pricePolicyId(pricePolicyId)
                        .quantity(quantity)
                        .reservedAt(reservedAt)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("재고 예약 목록을 저장한다")
        void shouldSaveReservations() {
            // given
            LocalDateTime now = LocalDateTime.now();
            List<InventoryReservation> reservations = List.of(
                    buildReservation(1L, 10L, 2, now),
                    buildReservation(1L, 11L, 1, now)
            );

            // when
            adapter.save(reservations);

            // then
            assertThat(inventoryReservationJpaRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("동일 주문/가격정책 조합 중복 시 DuplicateInventoryReservationException을 던진다")
        void shouldThrowOnDuplicate() {
            // given
            LocalDateTime now = LocalDateTime.now();
            adapter.save(List.of(buildReservation(1L, 10L, 2, now)));
            entityManager.flush();
            entityManager.clear();

            // when & then
            assertThatThrownBy(() -> adapter.save(List.of(buildReservation(1L, 10L, 3, now))))
                    .isInstanceOf(DuplicateInventoryReservationException.class);
        }
    }

    @Nested
    @DisplayName("findByOrderIdAndPricePolicyIds")
    class FindByOrderIdAndPricePolicyIds {

        @Test
        @DisplayName("주문 ID와 가격정책 ID 집합으로 예약을 조회한다")
        void shouldFindByOrderIdAndPricePolicyIds() {
            // given
            LocalDateTime now = LocalDateTime.now();
            adapter.save(List.of(
                    buildReservation(1L, 10L, 2, now),
                    buildReservation(1L, 11L, 1, now),
                    buildReservation(1L, 12L, 5, now),
                    buildReservation(2L, 10L, 3, now)
            ));
            entityManager.flush();
            entityManager.clear();

            // when
            List<InventoryReservation> result = adapter.findByOrderIdAndPricePolicyIds(1L, Set.of(10L, 11L));

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(InventoryReservation::getPricePolicyId).containsExactlyInAnyOrder(10L, 11L);
        }

        @Test
        @DisplayName("매칭되는 예약이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNotFound() {
            // when
            List<InventoryReservation> result = adapter.findByOrderIdAndPricePolicyIds(999L, Set.of(10L));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findExpiredBefore")
    class FindExpiredBefore {

        @Test
        @DisplayName("cutoff 이전 예약을 reservedAt 오름차순으로 조회한다")
        void shouldFindExpiredBeforeCutoff() {
            // given
            LocalDateTime base = LocalDateTime.of(2026, 4, 15, 10, 0);
            adapter.save(List.of(buildReservation(1L, 10L, 1, base.minusMinutes(30))));
            adapter.save(List.of(buildReservation(2L, 11L, 1, base.minusMinutes(20))));
            adapter.save(List.of(buildReservation(3L, 12L, 1, base.plusMinutes(10))));
            entityManager.flush();
            entityManager.clear();

            // when
            List<InventoryReservation> result = adapter.findExpiredBefore(base);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(InventoryReservation::getOrderId).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("expired 예약이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyWhenNoExpired() {
            // given
            LocalDateTime base = LocalDateTime.of(2026, 4, 15, 10, 0);
            adapter.save(List.of(buildReservation(1L, 10L, 1, base.plusMinutes(10))));
            entityManager.flush();
            entityManager.clear();

            // when
            List<InventoryReservation> result = adapter.findExpiredBefore(base);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByOrderIdAndPricePolicyIds")
    class DeleteByOrderIdAndPricePolicyIds {

        @Test
        @DisplayName("주문 ID와 가격정책 ID 집합에 해당하는 예약을 삭제한다")
        void shouldDeleteMatchingReservations() {
            // given
            LocalDateTime now = LocalDateTime.now();
            adapter.save(List.of(
                    buildReservation(1L, 10L, 1, now),
                    buildReservation(1L, 11L, 1, now),
                    buildReservation(1L, 12L, 1, now),
                    buildReservation(2L, 10L, 1, now)
            ));
            entityManager.flush();
            entityManager.clear();

            // when
            adapter.deleteByOrderIdAndPricePolicyIds(1L, Set.of(10L, 11L));
            entityManager.flush();
            entityManager.clear();

            // then
            List<InventoryReservation> remaining = adapter.findByOrderIdAndPricePolicyIds(1L, Set.of(10L, 11L, 12L));
            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getPricePolicyId()).isEqualTo(12L);
        }
    }
}
