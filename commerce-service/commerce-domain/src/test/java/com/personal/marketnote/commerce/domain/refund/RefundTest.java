package com.personal.marketnote.commerce.domain.refund;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Refund 도메인 테스트")
class RefundTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("전체 환불을 정상적으로 생성한다")
        void shouldCreateFullRefund() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(1L)
                    .orderId(10L)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(50000L)
                    .cancelReason("고객 요청")
                    .processedBy("SYSTEM")
                    .pgRefundKey("tno_123")
                    .pgRawResponse("{\"res_cd\":\"0000\"}")
                    .build();

            // when
            Refund refund = Refund.from(state);

            // then
            assertThat(refund.getPaymentId()).isEqualTo(1L);
            assertThat(refund.getOrderId()).isEqualTo(10L);
            assertThat(refund.getRefundType()).isEqualTo(RefundType.FULL_REFUND);
            assertThat(refund.getRefundAmount()).isEqualTo(50000L);
            assertThat(refund.getCancelReason()).isEqualTo("고객 요청");
            assertThat(refund.getProcessedBy()).isEqualTo("SYSTEM");
            assertThat(refund.getPgRefundKey()).isEqualTo("tno_123");
            assertThat(refund.getPgRawResponse()).isEqualTo("{\"res_cd\":\"0000\"}");
        }

        @Test
        @DisplayName("부분 환불을 정상적으로 생성한다")
        void shouldCreatePartialRefund() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(1L)
                    .orderId(10L)
                    .refundType(RefundType.PARTIAL_REFUND)
                    .refundAmount(20000L)
                    .cancelReason("부분 취소")
                    .processedBy("SYSTEM")
                    .build();

            // when
            Refund refund = Refund.from(state);

            // then
            assertThat(refund.getRefundType()).isEqualTo(RefundType.PARTIAL_REFUND);
            assertThat(refund.getRefundAmount()).isEqualTo(20000L);
        }
    }

    @Nested
    @DisplayName("생성 검증 실패 테스트")
    class CreateValidationFailureTest {

        @Test
        @DisplayName("결제 ID가 null이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenPaymentIdIsNull() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(null)
                    .orderId(10L)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(50000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Refund.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("결제 ID");
        }

        @Test
        @DisplayName("주문 ID가 null이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenOrderIdIsNull() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(1L)
                    .orderId(null)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(50000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Refund.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("주문 ID");
        }

        @Test
        @DisplayName("환불 유형이 null이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenRefundTypeIsNull() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(1L)
                    .orderId(10L)
                    .refundType(null)
                    .refundAmount(50000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Refund.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("환불 유형");
        }

        @Test
        @DisplayName("환불 금액이 null이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenRefundAmountIsNull() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(1L)
                    .orderId(10L)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> Refund.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("환불 금액");
        }

        @Test
        @DisplayName("환불 금액이 0이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenRefundAmountIsZero() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(1L)
                    .orderId(10L)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(0L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Refund.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("환불 금액");
        }

        @Test
        @DisplayName("환불 금액이 음수이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenRefundAmountIsNegative() {
            // given
            RefundCreateState state = RefundCreateState.builder()
                    .paymentId(1L)
                    .orderId(10L)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(-1000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> Refund.from(state))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("환불 금액");
        }
    }

    @Nested
    @DisplayName("스냅샷 복원 테스트")
    class SnapshotRestoreTest {

        @Test
        @DisplayName("스냅샷 상태로부터 환불을 정상적으로 복원한다")
        void shouldRestoreFromSnapshotState() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2026, 2, 24, 10, 0);
            RefundSnapshotState state = RefundSnapshotState.builder()
                    .id(100L)
                    .paymentId(1L)
                    .orderId(10L)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(50000L)
                    .cancelReason("고객 요청")
                    .processedBy("SYSTEM")
                    .pgRefundKey("tno_123")
                    .pgRawResponse("{\"res_cd\":\"0000\"}")
                    .createdAt(createdAt)
                    .build();

            // when
            Refund refund = Refund.from(state);

            // then
            assertThat(refund.getId()).isEqualTo(100L);
            assertThat(refund.getPaymentId()).isEqualTo(1L);
            assertThat(refund.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("술어 메서드 테스트")
    class PredicateTest {

        @Test
        @DisplayName("전체 환불이면 isFullRefund가 true를 반환한다")
        void shouldReturnTrueForFullRefund() {
            // given
            Refund refund = createRefund(RefundType.FULL_REFUND, 50000L);

            // then
            assertThat(refund.isFullRefund()).isTrue();
            assertThat(refund.isPartialRefund()).isFalse();
        }

        @Test
        @DisplayName("부분 환불이면 isPartialRefund가 true를 반환한다")
        void shouldReturnTrueForPartialRefund() {
            // given
            Refund refund = createRefund(RefundType.PARTIAL_REFUND, 20000L);

            // then
            assertThat(refund.isPartialRefund()).isTrue();
            assertThat(refund.isFullRefund()).isFalse();
        }
    }

    private Refund createRefund(RefundType refundType, Long amount) {
        RefundCreateState state = RefundCreateState.builder()
                .paymentId(1L)
                .orderId(10L)
                .refundType(refundType)
                .refundAmount(amount)
                .cancelReason("테스트")
                .processedBy("SYSTEM")
                .build();
        return Refund.from(state);
    }
}
