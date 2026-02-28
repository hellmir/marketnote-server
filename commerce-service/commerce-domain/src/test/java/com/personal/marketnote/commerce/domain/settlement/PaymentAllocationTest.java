package com.personal.marketnote.commerce.domain.settlement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentAllocation 도메인 테스트")
class PaymentAllocationTest {

    @Nested
    @DisplayName("from CreateState")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로부터 PaymentAllocation을 생성한다")
        void shouldCreateFromCreateState() {
            // given
            PaymentAllocationCreateState state = PaymentAllocationCreateState.builder()
                    .orderId(100L)
                    .sellerId(10L)
                    .allocatedAmount(50000L)
                    .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                    .targetType(PaymentAllocationTargetType.ORDER)
                    .idempotencyKey("ORDER_ALLOCATION:100:10")
                    .build();

            // when
            PaymentAllocation allocation = PaymentAllocation.from(state);

            // then
            assertThat(allocation.getOrderId()).isEqualTo(100L);
            assertThat(allocation.getSellerId()).isEqualTo(10L);
            assertThat(allocation.getAllocatedAmount()).isEqualTo(50000L);
            assertThat(allocation.getTransactionType()).isEqualTo(PaymentAllocationTransactionType.ORDER_REGISTRATION);
            assertThat(allocation.getTargetType()).isEqualTo(PaymentAllocationTargetType.ORDER);
            assertThat(allocation.getIdempotencyKey()).isEqualTo("ORDER_ALLOCATION:100:10");
            assertThat(allocation.getSettlementId()).isNull();
        }
    }

    @Nested
    @DisplayName("allocatedAmount 검증")
    class AllocatedAmountValidation {

        @Test
        @DisplayName("allocatedAmount가 0이면 IllegalArgumentException을 던진다")
        void shouldThrowWhenAllocatedAmountIsZero() {
            // given
            PaymentAllocationCreateState state = PaymentAllocationCreateState.builder()
                    .orderId(100L)
                    .sellerId(10L)
                    .allocatedAmount(0L)
                    .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                    .targetType(PaymentAllocationTargetType.ORDER)
                    .idempotencyKey("ORDER_ALLOCATION:100:10")
                    .build();

            // when & then
            assertThatThrownBy(() -> PaymentAllocation.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("allocatedAmount가 음수이면 IllegalArgumentException을 던진다")
        void shouldThrowWhenAllocatedAmountIsNegative() {
            // given
            PaymentAllocationCreateState state = PaymentAllocationCreateState.builder()
                    .orderId(100L)
                    .sellerId(10L)
                    .allocatedAmount(-1000L)
                    .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                    .targetType(PaymentAllocationTargetType.ORDER)
                    .idempotencyKey("ORDER_ALLOCATION:100:10")
                    .build();

            // when & then
            assertThatThrownBy(() -> PaymentAllocation.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("allocatedAmount가 null이면 IllegalArgumentException을 던진다")
        void shouldThrowWhenAllocatedAmountIsNull() {
            // given
            PaymentAllocationCreateState state = PaymentAllocationCreateState.builder()
                    .orderId(100L)
                    .sellerId(10L)
                    .allocatedAmount(null)
                    .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                    .targetType(PaymentAllocationTargetType.ORDER)
                    .idempotencyKey("ORDER_ALLOCATION:100:10")
                    .build();

            // when & then
            assertThatThrownBy(() -> PaymentAllocation.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }
    }

    @Nested
    @DisplayName("from SnapshotState")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 PaymentAllocation을 복원한다")
        void shouldRestoreFromSnapshotState() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2026, 2, 16, 10, 0);
            PaymentAllocationSnapshotState state = PaymentAllocationSnapshotState.builder()
                    .id(1L)
                    .orderId(100L)
                    .sellerId(10L)
                    .allocatedAmount(50000L)
                    .settlementId(5L)
                    .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                    .targetType(PaymentAllocationTargetType.ORDER)
                    .idempotencyKey("ORDER_ALLOCATION:100:10")
                    .createdAt(createdAt)
                    .build();

            // when
            PaymentAllocation allocation = PaymentAllocation.from(state);

            // then
            assertThat(allocation.getId()).isEqualTo(1L);
            assertThat(allocation.getOrderId()).isEqualTo(100L);
            assertThat(allocation.getSellerId()).isEqualTo(10L);
            assertThat(allocation.getAllocatedAmount()).isEqualTo(50000L);
            assertThat(allocation.getSettlementId()).isEqualTo(5L);
            assertThat(allocation.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("isSettled")
    class IsSettled {

        @Test
        @DisplayName("settlementId가 없으면 미정산 상태이다")
        void shouldReturnFalseWhenNoSettlementId() {
            // given
            PaymentAllocation allocation = PaymentAllocation.from(
                    PaymentAllocationCreateState.builder()
                            .orderId(100L)
                            .sellerId(10L)
                            .allocatedAmount(50000L)
                            .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                            .targetType(PaymentAllocationTargetType.ORDER)
                            .idempotencyKey("ORDER_ALLOCATION:100:10")
                            .build()
            );

            // when & then
            assertThat(allocation.isSettled()).isFalse();
        }

        @Test
        @DisplayName("settlementId가 있으면 정산 완료 상태이다")
        void shouldReturnTrueWhenSettlementIdExists() {
            // given
            PaymentAllocation allocation = PaymentAllocation.from(
                    PaymentAllocationSnapshotState.builder()
                            .id(1L)
                            .orderId(100L)
                            .sellerId(10L)
                            .allocatedAmount(50000L)
                            .settlementId(5L)
                            .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                            .targetType(PaymentAllocationTargetType.ORDER)
                            .idempotencyKey("ORDER_ALLOCATION:100:10")
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            // when & then
            assertThat(allocation.isSettled()).isTrue();
        }
    }

    @Nested
    @DisplayName("assignSettlement")
    class AssignSettlement {

        @Test
        @DisplayName("정산 ID를 할당한다")
        void shouldAssignSettlementId() {
            // given
            PaymentAllocation allocation = PaymentAllocation.from(
                    PaymentAllocationCreateState.builder()
                            .orderId(100L)
                            .sellerId(10L)
                            .allocatedAmount(50000L)
                            .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                            .targetType(PaymentAllocationTargetType.ORDER)
                            .idempotencyKey("ORDER_ALLOCATION:100:10")
                            .build()
            );

            // when
            allocation.assignSettlement(5L);

            // then
            assertThat(allocation.getSettlementId()).isEqualTo(5L);
            assertThat(allocation.isSettled()).isTrue();
        }
    }
}
