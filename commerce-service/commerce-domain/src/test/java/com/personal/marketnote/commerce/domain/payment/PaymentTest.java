package com.personal.marketnote.commerce.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Payment 도메인 테스트")
class PaymentTest {

    @Nested
    @DisplayName("from(PaymentCreateState) 팩토리 메서드")
    class CreateFromStateTest {

        @Test
        @DisplayName("결제 도메인이 올바르게 생성된다")
        void shouldCreatePaymentFromCreateState() {
            UUID orderKey = UUID.randomUUID();
            PaymentCreateState state = PaymentCreateState.builder()
                    .orderId(1L)
                    .orderKey(orderKey)
                    .paymentAmount(50000L)
                    .build();

            Payment payment = Payment.from(state);

            assertThat(payment.getOrderId()).isEqualTo(1L);
            assertThat(payment.getOrderKey()).isEqualTo(orderKey);
            assertThat(payment.getPaymentAmount()).isEqualTo(50000L);
            assertThat(payment.getSuccessYn()).isNull();
            assertThat(payment.getRefundedYn()).isFalse();
            assertThat(payment.getRefundAmount()).isEqualTo(0L);
            assertThat(payment.getPgPaymentKey()).isNull();
        }
    }

    @Nested
    @DisplayName("from(PaymentSnapshotState) 팩토리 메서드")
    class RestoreFromSnapshotTest {

        @Test
        @DisplayName("스냅샷 상태에서 도메인이 복원된다")
        void shouldRestorePaymentFromSnapshot() {
            UUID orderKey = UUID.randomUUID();
            PaymentSnapshotState state = PaymentSnapshotState.builder()
                    .id(10L)
                    .orderId(1L)
                    .orderKey(orderKey)
                    .pgPaymentKey("T0000_tno_123")
                    .paymentAmount(50000L)
                    .successYn(true)
                    .refundedYn(false)
                    .refundAmount(0L)
                    .build();

            Payment payment = Payment.from(state);

            assertThat(payment.getId()).isEqualTo(10L);
            assertThat(payment.getOrderId()).isEqualTo(1L);
            assertThat(payment.getOrderKey()).isEqualTo(orderKey);
            assertThat(payment.getPgPaymentKey()).isEqualTo("T0000_tno_123");
            assertThat(payment.getPaymentAmount()).isEqualTo(50000L);
            assertThat(payment.getSuccessYn()).isTrue();
            assertThat(payment.getRefundedYn()).isFalse();
            assertThat(payment.getRefundAmount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("markAsSuccess 메서드")
    class MarkAsSuccessTest {

        @Test
        @DisplayName("결제 승인 성공 시 pgPaymentKey가 설정되고 successYn이 true가 된다")
        void shouldSetPgPaymentKeyAndSuccess() {
            Payment payment = createDefaultPayment();

            payment.markAsSuccess("tno_12345");

            assertThat(payment.getPgPaymentKey()).isEqualTo("tno_12345");
            assertThat(payment.getSuccessYn()).isTrue();
        }
    }

    @Nested
    @DisplayName("markAsFailed 메서드")
    class MarkAsFailedTest {

        @Test
        @DisplayName("결제 승인 실패 시 successYn이 false가 된다")
        void shouldSetSuccessToFalse() {
            Payment payment = createDefaultPayment();

            payment.markAsFailed();

            assertThat(payment.getSuccessYn()).isFalse();
            assertThat(payment.getPgPaymentKey()).isNull();
        }
    }

    @Nested
    @DisplayName("markAsRefunded 메서드")
    class MarkAsRefundedTest {

        @Test
        @DisplayName("전체 환불 시 refundedYn이 true가 되고 refundAmount가 paymentAmount와 동일해진다")
        void shouldMarkAsFullyRefunded() {
            Payment payment = createSuccessfulPayment(50000L);

            payment.markAsRefunded();

            assertThat(payment.getRefundedYn()).isTrue();
            assertThat(payment.getRefundAmount()).isEqualTo(50000L);
        }
    }

    @Nested
    @DisplayName("markAsPartiallyRefunded 메서드")
    class MarkAsPartiallyRefundedTest {

        @Test
        @DisplayName("부분 환불 시 refundAmount가 누적된다")
        void shouldAccumulateRefundAmount() {
            Payment payment = createSuccessfulPayment(50000L);

            payment.markAsPartiallyRefunded(20000L);

            assertThat(payment.getRefundedYn()).isFalse();
            assertThat(payment.getRefundAmount()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("부분 환불 누적 합이 전체 금액과 같으면 refundedYn이 true가 된다")
        void shouldMarkAsRefundedWhenFullAmountReached() {
            Payment payment = createSuccessfulPayment(50000L);

            payment.markAsPartiallyRefunded(30000L);
            payment.markAsPartiallyRefunded(20000L);

            assertThat(payment.getRefundedYn()).isTrue();
            assertThat(payment.getRefundAmount()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("첫 부분 환불 시 기존 refundAmount가 null이면 0으로 초기화 후 누적한다")
        void shouldInitializeRefundAmountFromNull() {
            Payment payment = createSuccessfulPayment(50000L);

            payment.markAsPartiallyRefunded(10000L);

            assertThat(payment.getRefundAmount()).isEqualTo(10000L);
        }
    }

    private Payment createDefaultPayment() {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(1L)
                .orderKey(UUID.randomUUID())
                .paymentAmount(50000L)
                .build();
        return Payment.from(state);
    }

    private Payment createSuccessfulPayment(Long amount) {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(1L)
                .orderKey(UUID.randomUUID())
                .paymentAmount(amount)
                .build();
        Payment payment = Payment.from(state);
        payment.markAsSuccess("tno_test");
        return payment;
    }
}
