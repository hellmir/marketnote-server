package com.personal.marketnote.commerce.adapter.out.persistence.returntracker;

import com.personal.marketnote.commerce.adapter.out.persistence.returntracker.repository.ReturnTrackerJpaRepository;
import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerCreateState;
import com.personal.marketnote.common.configuration.AuditConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ReturnTrackerPersistenceAdapter.class})
@DisplayName("ReturnTrackerPersistenceAdapter 통합 테스트")
class ReturnTrackerPersistenceAdapterTest {

    @Autowired
    private ReturnTrackerPersistenceAdapter adapter;

    @Autowired
    private ReturnTrackerJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("ReturnTracker를 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSaveAndReturnWithId() {
            // given
            ReturnTracker tracker = ReturnTracker.from(
                    ReturnTrackerCreateState.builder()
                            .orderId(1L)
                            .returnSlipNumber("RTN20260409001")
                            .build()
            );

            // when
            ReturnTracker saved = adapter.save(tracker);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getOrderId()).isEqualTo(1L);
            assertThat(saved.getReturnSlipNumber()).isEqualTo("RTN20260409001");
            assertThat(saved.isInspectionPending()).isTrue();
            assertThat(saved.isRefundPending()).isTrue();
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("동일 orderId로 중복 저장 시 DataIntegrityViolationException이 발생한다")
        void shouldThrowWhenDuplicateOrderId() {
            // given
            ReturnTracker first = ReturnTracker.from(
                    ReturnTrackerCreateState.builder()
                            .orderId(1L)
                            .returnSlipNumber("RTN001")
                            .build()
            );
            adapter.save(first);

            ReturnTracker duplicate = ReturnTracker.from(
                    ReturnTrackerCreateState.builder()
                            .orderId(1L)
                            .returnSlipNumber("RTN002")
                            .build()
            );

            // when & then
            assertThatThrownBy(() -> adapter.save(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("orderId로 ReturnTracker를 조회한다")
        void shouldFindByOrderId() {
            // given
            ReturnTracker tracker = ReturnTracker.from(
                    ReturnTrackerCreateState.builder()
                            .orderId(100L)
                            .returnSlipNumber("RTN100")
                            .build()
            );
            adapter.save(tracker);

            // when
            Optional<ReturnTracker> found = adapter.findByOrderId(100L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOrderId()).isEqualTo(100L);
            assertThat(found.get().getReturnSlipNumber()).isEqualTo("RTN100");
        }

        @Test
        @DisplayName("존재하지 않는 orderId로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<ReturnTracker> found = adapter.findByOrderId(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByInspectionStatus")
    class FindByInspectionStatus {

        @Test
        @DisplayName("PENDING 상태의 ReturnTracker 목록을 조회한다")
        void shouldFindPendingTrackers() {
            // given
            adapter.save(ReturnTracker.from(
                    ReturnTrackerCreateState.builder().orderId(1L).returnSlipNumber("RTN001").build()));
            adapter.save(ReturnTracker.from(
                    ReturnTrackerCreateState.builder().orderId(2L).returnSlipNumber("RTN002").build()));

            // when
            List<ReturnTracker> pendingTrackers = adapter.findByInspectionStatus(ReturnInspectionStatus.PENDING);

            // then
            assertThat(pendingTrackers).hasSize(2);
            assertThat(pendingTrackers).allMatch(ReturnTracker::isInspectionPending);
        }

        @Test
        @DisplayName("해당 상태의 ReturnTracker가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoMatch() {
            // given
            adapter.save(ReturnTracker.from(
                    ReturnTrackerCreateState.builder().orderId(1L).returnSlipNumber("RTN001").build()));

            // when
            List<ReturnTracker> passedTrackers = adapter.findByInspectionStatus(ReturnInspectionStatus.PASSED);

            // then
            assertThat(passedTrackers).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("검수 상태를 PASSED로 업데이트한다")
        void shouldUpdateInspectionStatus() {
            // given
            ReturnTracker saved = adapter.save(ReturnTracker.from(
                    ReturnTrackerCreateState.builder().orderId(1L).returnSlipNumber("RTN001").build()));

            LocalDateTime inspectedAt = LocalDateTime.of(2026, 4, 9, 14, 0);
            saved.passInspection(inspectedAt);

            // when
            ReturnTracker updated = adapter.update(saved);

            // then
            assertThat(updated.isInspectionPassed()).isTrue();
            assertThat(updated.getInspectedAt()).isEqualTo(inspectedAt);
        }

        @Test
        @DisplayName("환불 상태를 COMPLETED로 업데이트한다")
        void shouldUpdateRefundStatus() {
            // given
            ReturnTracker saved = adapter.save(ReturnTracker.from(
                    ReturnTrackerCreateState.builder().orderId(1L).returnSlipNumber("RTN001").build()));

            LocalDateTime refundedAt = LocalDateTime.of(2026, 4, 9, 15, 0);
            saved.completeRefund(refundedAt);

            // when
            ReturnTracker updated = adapter.update(saved);

            // then
            assertThat(updated.isRefundCompleted()).isTrue();
            assertThat(updated.getRefundedAt()).isEqualTo(refundedAt);
        }
    }
}
