package com.personal.marketnote.commerce.domain.returntracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReturnTracker 도메인 테스트")
class ReturnTrackerTest {

    @Nested
    @DisplayName("from CreateState")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로부터 ReturnTracker를 생성하면 검수 상태와 환불 상태가 PENDING이다")
        void shouldCreateWithPendingStatuses() {
            // given
            ReturnTrackerCreateState state = ReturnTrackerCreateState.builder()
                .orderId(1L)
                .returnSlipNumber("RTN20260409001")
                .build();

            // when
            ReturnTracker tracker = ReturnTracker.from(state);

            // then
            assertThat(tracker.getOrderId()).isEqualTo(1L);
            assertThat(tracker.getReturnSlipNumber()).isEqualTo("RTN20260409001");
            assertThat(tracker.isInspectionPending()).isTrue();
            assertThat(tracker.isRefundPending()).isTrue();
            assertThat(tracker.getInspectedAt()).isNull();
            assertThat(tracker.getRefundedAt()).isNull();
        }

        @Test
        @DisplayName("orderId가 null이면 ReturnTrackerOrderIdNoValueException을 던진다")
        void shouldThrowWhenOrderIdIsNull() {
            // given
            ReturnTrackerCreateState state = ReturnTrackerCreateState.builder()
                .returnSlipNumber("RTN20260409001")
                .build();

            // when & then
            assertThatThrownBy(() -> ReturnTracker.from(state))
                .isInstanceOf(ReturnTrackerOrderIdNoValueException.class);
        }

        @Test
        @DisplayName("returnSlipNumber가 null이어도 생성에 성공한다")
        void shouldCreateWithNullReturnSlipNumber() {
            // given
            ReturnTrackerCreateState state = ReturnTrackerCreateState.builder()
                .orderId(1L)
                .build();

            // when
            ReturnTracker tracker = ReturnTracker.from(state);

            // then
            assertThat(tracker.getOrderId()).isEqualTo(1L);
            assertThat(tracker.getReturnSlipNumber()).isNull();
            assertThat(tracker.isInspectionPending()).isTrue();
        }
    }

    @Nested
    @DisplayName("from SnapshotState")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 ReturnTracker를 복원하면 모든 필드가 정확히 매핑된다")
        void shouldRestoreFromSnapshotState() {
            // given
            LocalDateTime inspectedAt = LocalDateTime.of(2026, 4, 9, 14, 0);
            LocalDateTime refundedAt = LocalDateTime.of(2026, 4, 9, 15, 0);
            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 9, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2026, 4, 9, 15, 0);

            ReturnTrackerSnapshotState state = ReturnTrackerSnapshotState.builder()
                .id(100L)
                .orderId(1L)
                .returnSlipNumber("RTN20260409001")
                .inspectionStatus(ReturnInspectionStatus.PASSED)
                .refundStatus(ReturnRefundStatus.COMPLETED)
                .inspectedAt(inspectedAt)
                .refundedAt(refundedAt)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

            // when
            ReturnTracker tracker = ReturnTracker.from(state);

            // then
            assertThat(tracker.getId()).isEqualTo(100L);
            assertThat(tracker.getOrderId()).isEqualTo(1L);
            assertThat(tracker.getReturnSlipNumber()).isEqualTo("RTN20260409001");
            assertThat(tracker.isInspectionPassed()).isTrue();
            assertThat(tracker.isRefundCompleted()).isTrue();
            assertThat(tracker.getInspectedAt()).isEqualTo(inspectedAt);
            assertThat(tracker.getRefundedAt()).isEqualTo(refundedAt);
            assertThat(tracker.getCreatedAt()).isEqualTo(createdAt);
            assertThat(tracker.getModifiedAt()).isEqualTo(modifiedAt);
        }
    }

    @Nested
    @DisplayName("검수 상태 전이")
    class InspectionStatusTransition {

        @Test
        @DisplayName("PENDING 상태에서 passInspection 호출 시 PASSED로 전이되고 inspectedAt이 설정된다")
        void shouldTransitionToPassed() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 14, 0);

            // when
            tracker.passInspection(now);

            // then
            assertThat(tracker.isInspectionPassed()).isTrue();
            assertThat(tracker.isInspectionPending()).isFalse();
            assertThat(tracker.getInspectedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("PENDING 상태에서 failInspection 호출 시 FAILED로 전이되고 inspectedAt이 설정된다")
        void shouldTransitionToFailed() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 14, 0);

            // when
            tracker.failInspection(now);

            // then
            assertThat(tracker.isInspectionFailed()).isTrue();
            assertThat(tracker.getInspectedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("PENDING 상태에서 holdInspection 호출 시 ON_HOLD로 전이되고 inspectedAt은 설정되지 않는다")
        void shouldTransitionToOnHold() {
            // given
            ReturnTracker tracker = createPendingTracker();

            // when
            tracker.holdInspection();

            // then
            assertThat(tracker.isInspectionOnHold()).isTrue();
            assertThat(tracker.getInspectedAt()).isNull();
        }

        @Test
        @DisplayName("PASSED 상태에서 passInspection 호출 시 InvalidReturnInspectionStatusTransitionException을 던진다")
        void shouldThrowWhenPassFromPassed() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 14, 0);
            tracker.passInspection(now);

            // when & then
            assertThatThrownBy(() -> tracker.passInspection(now))
                .isInstanceOf(InvalidReturnInspectionStatusTransitionException.class);
        }

        @Test
        @DisplayName("FAILED 상태에서 failInspection 호출 시 InvalidReturnInspectionStatusTransitionException을 던진다")
        void shouldThrowWhenFailFromFailed() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 14, 0);
            tracker.failInspection(now);

            // when & then
            assertThatThrownBy(() -> tracker.failInspection(now))
                .isInstanceOf(InvalidReturnInspectionStatusTransitionException.class);
        }

        @Test
        @DisplayName("ON_HOLD 상태에서 holdInspection 호출 시 InvalidReturnInspectionStatusTransitionException을 던진다")
        void shouldThrowWhenHoldFromOnHold() {
            // given
            ReturnTracker tracker = createPendingTracker();
            tracker.holdInspection();

            // when & then
            assertThatThrownBy(tracker::holdInspection)
                .isInstanceOf(InvalidReturnInspectionStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("환불 상태 전이")
    class RefundStatusTransition {

        @Test
        @DisplayName("PENDING 상태에서 completeRefund 호출 시 COMPLETED로 전이되고 refundedAt이 설정된다")
        void shouldTransitionToCompleted() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 15, 0);

            // when
            tracker.completeRefund(now);

            // then
            assertThat(tracker.isRefundCompleted()).isTrue();
            assertThat(tracker.isRefundPending()).isFalse();
            assertThat(tracker.getRefundedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("PENDING 상태에서 failRefund 호출 시 FAILED로 전이된다")
        void shouldTransitionToFailed() {
            // given
            ReturnTracker tracker = createPendingTracker();

            // when
            tracker.failRefund();

            // then
            assertThat(tracker.isRefundFailed()).isTrue();
            assertThat(tracker.isRefundPending()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태에서 retryRefund 호출 시 PENDING으로 전이되고 refundedAt이 null이 된다")
        void shouldRetryFromFailed() {
            // given
            ReturnTracker tracker = createPendingTracker();
            tracker.failRefund();

            // when
            tracker.retryRefund();

            // then
            assertThat(tracker.isRefundPending()).isTrue();
            assertThat(tracker.isRefundFailed()).isFalse();
            assertThat(tracker.getRefundedAt()).isNull();
        }

        @Test
        @DisplayName("COMPLETED 상태에서 completeRefund 호출 시 InvalidReturnRefundStatusTransitionException을 던진다")
        void shouldThrowWhenCompleteFromCompleted() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 15, 0);
            tracker.completeRefund(now);

            // when & then
            assertThatThrownBy(() -> tracker.completeRefund(now))
                .isInstanceOf(InvalidReturnRefundStatusTransitionException.class);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 failRefund 호출 시 InvalidReturnRefundStatusTransitionException을 던진다")
        void shouldThrowWhenFailFromCompleted() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 15, 0);
            tracker.completeRefund(now);

            // when & then
            assertThatThrownBy(tracker::failRefund)
                .isInstanceOf(InvalidReturnRefundStatusTransitionException.class);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 retryRefund 호출 시 InvalidReturnRefundStatusTransitionException을 던진다")
        void shouldThrowWhenRetryFromCompleted() {
            // given
            ReturnTracker tracker = createPendingTracker();
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 15, 0);
            tracker.completeRefund(now);

            // when & then
            assertThatThrownBy(tracker::retryRefund)
                .isInstanceOf(InvalidReturnRefundStatusTransitionException.class);
        }

        @Test
        @DisplayName("PENDING 상태에서 retryRefund 호출 시 InvalidReturnRefundStatusTransitionException을 던진다")
        void shouldThrowWhenRetryFromPending() {
            // given
            ReturnTracker tracker = createPendingTracker();

            // when & then
            assertThatThrownBy(tracker::retryRefund)
                .isInstanceOf(InvalidReturnRefundStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("술어 메서드")
    class PredicateTest {

        @Test
        @DisplayName("생성 직후 isInspectionPending과 isRefundPending이 true이다")
        void shouldReturnTrueForPendingAfterCreation() {
            // given
            ReturnTracker tracker = createPendingTracker();

            // then
            assertThat(tracker.isInspectionPending()).isTrue();
            assertThat(tracker.isInspectionPassed()).isFalse();
            assertThat(tracker.isInspectionFailed()).isFalse();
            assertThat(tracker.isInspectionOnHold()).isFalse();
            assertThat(tracker.isRefundPending()).isTrue();
            assertThat(tracker.isRefundCompleted()).isFalse();
            assertThat(tracker.isRefundFailed()).isFalse();
        }

        @Test
        @DisplayName("검수 통과 후 isInspectionPassed만 true이다")
        void shouldReturnTrueOnlyForPassedAfterPassInspection() {
            // given
            ReturnTracker tracker = createPendingTracker();
            tracker.passInspection(LocalDateTime.now());

            // then
            assertThat(tracker.isInspectionPending()).isFalse();
            assertThat(tracker.isInspectionPassed()).isTrue();
            assertThat(tracker.isInspectionFailed()).isFalse();
            assertThat(tracker.isInspectionOnHold()).isFalse();
        }

        @Test
        @DisplayName("환불 완료 후 isRefundCompleted만 true이다")
        void shouldReturnTrueOnlyForCompletedAfterCompleteRefund() {
            // given
            ReturnTracker tracker = createPendingTracker();
            tracker.completeRefund(LocalDateTime.now());

            // then
            assertThat(tracker.isRefundPending()).isFalse();
            assertThat(tracker.isRefundCompleted()).isTrue();
            assertThat(tracker.isRefundFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("ReturnInspectionStatus enum")
    class InspectionStatusEnumTest {

        @Test
        @DisplayName("PENDING에서 PASSED, FAILED, ON_HOLD로 전이 가능하다")
        void shouldAllowTransitionFromPending() {
            assertThat(ReturnInspectionStatus.PENDING.canTransitionTo(ReturnInspectionStatus.PASSED)).isTrue();
            assertThat(ReturnInspectionStatus.PENDING.canTransitionTo(ReturnInspectionStatus.FAILED)).isTrue();
            assertThat(ReturnInspectionStatus.PENDING.canTransitionTo(ReturnInspectionStatus.ON_HOLD)).isTrue();
        }

        @Test
        @DisplayName("PASSED는 종단 상태로 다른 상태로 전이 불가하다")
        void shouldNotAllowTransitionFromPassed() {
            assertThat(ReturnInspectionStatus.PASSED.canTransitionTo(ReturnInspectionStatus.PENDING)).isFalse();
            assertThat(ReturnInspectionStatus.PASSED.canTransitionTo(ReturnInspectionStatus.FAILED)).isFalse();
            assertThat(ReturnInspectionStatus.PASSED.canTransitionTo(ReturnInspectionStatus.ON_HOLD)).isFalse();
        }

        @Test
        @DisplayName("FAILED는 종단 상태로 다른 상태로 전이 불가하다")
        void shouldNotAllowTransitionFromFailed() {
            assertThat(ReturnInspectionStatus.FAILED.canTransitionTo(ReturnInspectionStatus.PENDING)).isFalse();
            assertThat(ReturnInspectionStatus.FAILED.canTransitionTo(ReturnInspectionStatus.PASSED)).isFalse();
            assertThat(ReturnInspectionStatus.FAILED.canTransitionTo(ReturnInspectionStatus.ON_HOLD)).isFalse();
        }

        @Test
        @DisplayName("ON_HOLD는 종단 상태로 다른 상태로 전이 불가하다")
        void shouldNotAllowTransitionFromOnHold() {
            assertThat(ReturnInspectionStatus.ON_HOLD.canTransitionTo(ReturnInspectionStatus.PENDING)).isFalse();
            assertThat(ReturnInspectionStatus.ON_HOLD.canTransitionTo(ReturnInspectionStatus.PASSED)).isFalse();
            assertThat(ReturnInspectionStatus.ON_HOLD.canTransitionTo(ReturnInspectionStatus.FAILED)).isFalse();
        }

        @Test
        @DisplayName("술어 메서드가 올바른 상태에서 true를 반환한다")
        void shouldReturnCorrectPredicateValues() {
            assertThat(ReturnInspectionStatus.PENDING.isPending()).isTrue();
            assertThat(ReturnInspectionStatus.PASSED.isPassed()).isTrue();
            assertThat(ReturnInspectionStatus.FAILED.isFailed()).isTrue();
            assertThat(ReturnInspectionStatus.ON_HOLD.isOnHold()).isTrue();
        }
    }

    @Nested
    @DisplayName("ReturnRefundStatus enum")
    class RefundStatusEnumTest {

        @Test
        @DisplayName("PENDING에서 COMPLETED, FAILED로 전이 가능하다")
        void shouldAllowTransitionFromPending() {
            assertThat(ReturnRefundStatus.PENDING.canTransitionTo(ReturnRefundStatus.COMPLETED)).isTrue();
            assertThat(ReturnRefundStatus.PENDING.canTransitionTo(ReturnRefundStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("FAILED에서 PENDING으로 전이 가능하다 (재시도)")
        void shouldAllowRetryFromFailed() {
            assertThat(ReturnRefundStatus.FAILED.canTransitionTo(ReturnRefundStatus.PENDING)).isTrue();
        }

        @Test
        @DisplayName("COMPLETED는 종단 상태로 다른 상태로 전이 불가하다")
        void shouldNotAllowTransitionFromCompleted() {
            assertThat(ReturnRefundStatus.COMPLETED.canTransitionTo(ReturnRefundStatus.PENDING)).isFalse();
            assertThat(ReturnRefundStatus.COMPLETED.canTransitionTo(ReturnRefundStatus.FAILED)).isFalse();
        }

        @Test
        @DisplayName("FAILED에서 COMPLETED로 직접 전이 불가하다")
        void shouldNotAllowDirectTransitionFromFailedToCompleted() {
            assertThat(ReturnRefundStatus.FAILED.canTransitionTo(ReturnRefundStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("술어 메서드가 올바른 상태에서 true를 반환한다")
        void shouldReturnCorrectPredicateValues() {
            assertThat(ReturnRefundStatus.PENDING.isPending()).isTrue();
            assertThat(ReturnRefundStatus.COMPLETED.isCompleted()).isTrue();
            assertThat(ReturnRefundStatus.FAILED.isFailed()).isTrue();
        }
    }

    private ReturnTracker createPendingTracker() {
        return ReturnTracker.from(
            ReturnTrackerCreateState.builder()
                .orderId(1L)
                .returnSlipNumber("RTN20260409001")
                .build()
        );
    }
}
