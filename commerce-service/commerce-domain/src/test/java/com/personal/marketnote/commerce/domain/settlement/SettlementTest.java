package com.personal.marketnote.commerce.domain.settlement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Settlement 도메인 테스트")
class SettlementTest {

    @Nested
    @DisplayName("from CreateState")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로부터 Settlement을 생성하면 PENDING 상태이다")
        void shouldCreateWithPendingStatus() {
            // given
            SettlementCreateState state = SettlementCreateState.builder()
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(100000L)
                    .pgFeeAmount(3000L)
                    .platformFeeAmount(5000L)
                    .sellerPayoutAmount(92000L)
                    .build();

            // when
            Settlement settlement = Settlement.from(state);

            // then
            assertThat(settlement.getSellerId()).isEqualTo(10L);
            assertThat(settlement.getYear()).isEqualTo(2026);
            assertThat(settlement.getMonth()).isEqualTo(2);
            assertThat(settlement.getTotalAllocatedAmount()).isEqualTo(100000L);
            assertThat(settlement.getPgFeeAmount()).isEqualTo(3000L);
            assertThat(settlement.getPlatformFeeAmount()).isEqualTo(5000L);
            assertThat(settlement.getSellerPayoutAmount()).isEqualTo(92000L);
            assertThat(settlement.isPending()).isTrue();
            assertThat(settlement.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("totalAllocatedAmount가 0 이하이면 예외를 던진다")
        void shouldThrowWhenTotalAllocatedAmountIsZeroOrNegative() {
            // given
            SettlementCreateState state = SettlementCreateState.builder()
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(0L)
                    .pgFeeAmount(0L)
                    .platformFeeAmount(0L)
                    .sellerPayoutAmount(0L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Settlement.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("sellerPayoutAmount가 0 이하이면 예외를 던진다")
        void shouldThrowWhenSellerPayoutAmountIsZeroOrNegative() {
            // given
            SettlementCreateState state = SettlementCreateState.builder()
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(100000L)
                    .pgFeeAmount(50000L)
                    .platformFeeAmount(50000L)
                    .sellerPayoutAmount(0L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Settlement.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("pgFeeAmount가 음수이면 예외를 던진다")
        void shouldThrowWhenPgFeeAmountIsNegative() {
            // given
            SettlementCreateState state = SettlementCreateState.builder()
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(100000L)
                    .pgFeeAmount(-1L)
                    .platformFeeAmount(5000L)
                    .sellerPayoutAmount(96000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Settlement.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수일 수 없습니다");
        }

        @Test
        @DisplayName("platformFeeAmount가 음수이면 예외를 던진다")
        void shouldThrowWhenPlatformFeeAmountIsNegative() {
            // given
            SettlementCreateState state = SettlementCreateState.builder()
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(100000L)
                    .pgFeeAmount(3000L)
                    .platformFeeAmount(-1L)
                    .sellerPayoutAmount(98000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Settlement.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("from SnapshotState")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 Settlement을 복원한다")
        void shouldRestoreFromSnapshotState() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2026, 2, 16, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2026, 2, 16, 11, 0);
            SettlementSnapshotState state = SettlementSnapshotState.builder()
                    .id(1L)
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(100000L)
                    .pgFeeAmount(3000L)
                    .platformFeeAmount(5000L)
                    .sellerPayoutAmount(92000L)
                    .status(SettlementStatus.COMPLETED)
                    .version(1L)
                    .createdAt(createdAt)
                    .modifiedAt(modifiedAt)
                    .build();

            // when
            Settlement settlement = Settlement.from(state);

            // then
            assertThat(settlement.getId()).isEqualTo(1L);
            assertThat(settlement.getSellerId()).isEqualTo(10L);
            assertThat(settlement.getYear()).isEqualTo(2026);
            assertThat(settlement.getMonth()).isEqualTo(2);
            assertThat(settlement.isCompleted()).isTrue();
            assertThat(settlement.getVersion()).isEqualTo(1L);
            assertThat(settlement.getCreatedAt()).isEqualTo(createdAt);
            assertThat(settlement.getModifiedAt()).isEqualTo(modifiedAt);
        }
    }

    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("complete() 호출 시 COMPLETED 상태로 전이된다")
        void shouldTransitionToCompleted() {
            // given
            Settlement settlement = Settlement.from(
                    SettlementCreateState.builder()
                            .sellerId(10L)
                            .year(2026)
                            .month(2)
                            .totalAllocatedAmount(100000L)
                            .pgFeeAmount(3000L)
                            .platformFeeAmount(5000L)
                            .sellerPayoutAmount(92000L)
                            .build()
            );

            // when
            settlement.complete();

            // then
            assertThat(settlement.isCompleted()).isTrue();
            assertThat(settlement.isPending()).isFalse();
        }

        @Test
        @DisplayName("fail() 호출 시 FAILED 상태로 전이된다")
        void shouldTransitionToFailed() {
            // given
            Settlement settlement = Settlement.from(
                    SettlementCreateState.builder()
                            .sellerId(10L)
                            .year(2026)
                            .month(2)
                            .totalAllocatedAmount(100000L)
                            .pgFeeAmount(3000L)
                            .platformFeeAmount(5000L)
                            .sellerPayoutAmount(92000L)
                            .build()
            );

            // when
            settlement.fail();

            // then
            assertThat(settlement.isPending()).isFalse();
            assertThat(settlement.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED 상태에서 complete() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenCompleteFromCompleted() {
            // given
            Settlement settlement = Settlement.from(
                    SettlementCreateState.builder()
                            .sellerId(10L).year(2026).month(2)
                            .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                            .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                            .build()
            );
            settlement.complete();

            // when & then
            assertThatThrownBy(settlement::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("COMPLETED 상태에서 fail() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenFailFromCompleted() {
            // given
            Settlement settlement = Settlement.from(
                    SettlementCreateState.builder()
                            .sellerId(10L).year(2026).month(2)
                            .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                            .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                            .build()
            );
            settlement.complete();

            // when & then
            assertThatThrownBy(settlement::fail)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("FAILED 상태에서 resetToPending() 호출 시 PENDING 상태로 전이된다")
        void shouldResetToPendingFromFailed() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.fail();
            assertThat(settlement.isFailed()).isTrue();

            // when
            settlement.resetToPending();

            // then
            assertThat(settlement.isPending()).isTrue();
            assertThat(settlement.isFailed()).isFalse();
        }

        @Test
        @DisplayName("PENDING 상태에서 resetToPending() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenResetToPendingFromPending() {
            // given
            Settlement settlement = createPendingSettlement();

            // when & then
            assertThatThrownBy(settlement::resetToPending)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("COMPLETED 상태에서 resetToPending() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenResetToPendingFromCompleted() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.complete();

            // when & then
            assertThatThrownBy(settlement::resetToPending)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("COMPLETED 상태에서 cancel() 호출 시 CANCELLED 상태로 전이된다")
        void shouldTransitionToCancelled() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.complete();

            // when
            settlement.cancel();

            // then
            assertThat(settlement.isCancelled()).isTrue();
            assertThat(settlement.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("PENDING 상태에서 cancel() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenCancelFromPending() {
            // given
            Settlement settlement = createPendingSettlement();

            // when & then
            assertThatThrownBy(settlement::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("FAILED 상태에서 cancel() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenCancelFromFailed() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.fail();

            // when & then
            assertThatThrownBy(settlement::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("CANCELLED 상태에서 cancel() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenCancelFromCancelled() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.complete();
            settlement.cancel();

            // when & then
            assertThatThrownBy(settlement::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("CANCELLED 상태에서 resetCancelledToPending() 호출 시 PENDING 상태로 전이된다")
        void shouldResetCancelledToPending() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.complete();
            settlement.cancel();
            assertThat(settlement.isCancelled()).isTrue();

            // when
            settlement.resetCancelledToPending();

            // then
            assertThat(settlement.isPending()).isTrue();
            assertThat(settlement.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("PENDING 상태에서 resetCancelledToPending() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenResetCancelledFromPending() {
            // given
            Settlement settlement = createPendingSettlement();

            // when & then
            assertThatThrownBy(settlement::resetCancelledToPending)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("COMPLETED 상태에서 resetCancelledToPending() 호출 시 IllegalStateException을 던진다")
        void shouldThrowWhenResetCancelledFromCompleted() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.complete();

            // when & then
            assertThatThrownBy(settlement::resetCancelledToPending)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("현재 상태");
        }

        @Test
        @DisplayName("CANCELLED → PENDING → COMPLETED 전체 흐름이 성공한다")
        void shouldCompleteCancelReExecuteFlow() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.complete();
            settlement.cancel();
            settlement.resetCancelledToPending();

            // when
            settlement.complete();

            // then
            assertThat(settlement.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("FAILED 상태에서 resetToPending 후 complete() 호출 시 COMPLETED 상태로 전이된다")
        void shouldCompleteAfterResetFromFailed() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.fail();
            settlement.resetToPending();

            // when
            settlement.complete();

            // then
            assertThat(settlement.isCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("술어 메서드")
    class PredicateTest {

        @Test
        @DisplayName("isFailed()는 FAILED 상태에서 true를 반환한다")
        void shouldReturnTrueWhenFailed() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.fail();

            // then
            assertThat(settlement.isFailed()).isTrue();
            assertThat(settlement.isPending()).isFalse();
            assertThat(settlement.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("isCancelled()는 CANCELLED 상태에서 true를 반환한다")
        void shouldReturnTrueWhenCancelled() {
            // given
            Settlement settlement = createPendingSettlement();
            settlement.complete();
            settlement.cancel();

            // then
            assertThat(settlement.isCancelled()).isTrue();
            assertThat(settlement.isPending()).isFalse();
            assertThat(settlement.isCompleted()).isFalse();
            assertThat(settlement.isFailed()).isFalse();
        }

        @Test
        @DisplayName("isFailed()는 PENDING 상태에서 false를 반환한다")
        void shouldReturnFalseWhenPending() {
            // given
            Settlement settlement = createPendingSettlement();

            // then
            assertThat(settlement.isFailed()).isFalse();
        }
    }

    private Settlement createPendingSettlement() {
        return Settlement.from(
                SettlementCreateState.builder()
                        .sellerId(10L).year(2026).month(2)
                        .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                        .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                        .build()
        );
    }
}
