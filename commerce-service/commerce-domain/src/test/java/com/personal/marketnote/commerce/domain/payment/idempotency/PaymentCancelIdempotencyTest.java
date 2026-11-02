package com.personal.marketnote.commerce.domain.payment.idempotency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentCancelIdempotency 도메인 테스트")
class PaymentCancelIdempotencyTest {

    private static final String KEY = "11111111-2222-4333-8444-555555555555";
    private static final Long PAYMENT_ID = 100L;
    private static final Long BUYER_ID = 200L;
    private static final String RESULT_JSON = "{\"status\":\"REFUNDED\"}";

    @Nested
    @DisplayName("from(CreateState)")
    class CreateFromCreateStateTest {

        @Test
        @DisplayName("신규 생성 시 상태는 IN_PROGRESS 이며 result/failedReason 은 null 이다")
        void shouldCreateInProgressWhenFromCreateState() {
            PaymentCancelIdempotency idempotency = PaymentCancelIdempotency.from(
                    PaymentCancelIdempotencyCreateState.builder()
                            .idempotencyKey(KEY)
                            .paymentId(PAYMENT_ID)
                            .buyerId(BUYER_ID)
                            .build()
            );

            assertThat(idempotency.isInProgress()).isTrue();
            assertThat(idempotency.isCompleted()).isFalse();
            assertThat(idempotency.isFailed()).isFalse();
            assertThat(idempotency.getIdempotencyKey()).isEqualTo(KEY);
            assertThat(idempotency.getPaymentId()).isEqualTo(PAYMENT_ID);
            assertThat(idempotency.getBuyerId()).isEqualTo(BUYER_ID);
            assertThat(idempotency.getResult()).isNull();
            assertThat(idempotency.getFailedReason()).isNull();
        }
    }

    @Nested
    @DisplayName("complete")
    class CompleteTest {

        @Test
        @DisplayName("IN_PROGRESS 에서 complete 시 COMPLETED 로 전이되고 result 가 저장된다")
        void shouldTransitionToCompletedWhenInProgress() {
            PaymentCancelIdempotency idempotency = newInProgress();

            idempotency.complete(RESULT_JSON);

            assertThat(idempotency.isCompleted()).isTrue();
            assertThat(idempotency.getResult()).isEqualTo(RESULT_JSON);
            assertThat(idempotency.getFailedReason()).isNull();
        }

        @Test
        @DisplayName("COMPLETED 에서 complete 재호출 시 예외가 발생한다")
        void shouldThrowWhenAlreadyCompleted() {
            PaymentCancelIdempotency idempotency = newInProgress();
            idempotency.complete(RESULT_JSON);

            assertThatThrownBy(() -> idempotency.complete(RESULT_JSON))
                    .isInstanceOf(InvalidPaymentCancelIdempotencyStatusTransitionException.class);
        }

        @Test
        @DisplayName("FAILED 에서 complete 호출 시 예외가 발생한다")
        void shouldThrowWhenFailed() {
            PaymentCancelIdempotency idempotency = newInProgress();
            idempotency.fail("DB 오류");

            assertThatThrownBy(() -> idempotency.complete(RESULT_JSON))
                    .isInstanceOf(InvalidPaymentCancelIdempotencyStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("fail")
    class FailTest {

        @Test
        @DisplayName("IN_PROGRESS 에서 fail 시 FAILED 로 전이되고 reason 이 저장된다")
        void shouldTransitionToFailedWhenInProgress() {
            PaymentCancelIdempotency idempotency = newInProgress();

            idempotency.fail("PG 타임아웃");

            assertThat(idempotency.isFailed()).isTrue();
            assertThat(idempotency.getFailedReason()).isEqualTo("PG 타임아웃");
        }

        @Test
        @DisplayName("reason 이 blank 이면 'unknown' 으로 저장된다")
        void shouldStoreUnknownWhenReasonBlank() {
            PaymentCancelIdempotency idempotency = newInProgress();

            idempotency.fail("");

            assertThat(idempotency.getFailedReason()).isEqualTo("unknown");
        }

        @Test
        @DisplayName("COMPLETED 에서 fail 호출 시 예외가 발생한다")
        void shouldThrowWhenCompleted() {
            PaymentCancelIdempotency idempotency = newInProgress();
            idempotency.complete(RESULT_JSON);

            assertThatThrownBy(() -> idempotency.fail("reason"))
                    .isInstanceOf(InvalidPaymentCancelIdempotencyStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("retry")
    class RetryTest {

        @Test
        @DisplayName("FAILED 에서 retry 시 IN_PROGRESS 로 전이되고 failedReason 이 null 로 초기화된다")
        void shouldTransitionToInProgressWhenFailed() {
            PaymentCancelIdempotency idempotency = newInProgress();
            idempotency.fail("PG 타임아웃");

            idempotency.retry();

            assertThat(idempotency.isInProgress()).isTrue();
            assertThat(idempotency.getFailedReason()).isNull();
        }

        @Test
        @DisplayName("IN_PROGRESS 에서 retry 호출 시 예외가 발생한다")
        void shouldThrowWhenInProgress() {
            PaymentCancelIdempotency idempotency = newInProgress();

            assertThatThrownBy(idempotency::retry)
                    .isInstanceOf(InvalidPaymentCancelIdempotencyStatusTransitionException.class);
        }

        @Test
        @DisplayName("COMPLETED 에서 retry 호출 시 예외가 발생한다")
        void shouldThrowWhenCompleted() {
            PaymentCancelIdempotency idempotency = newInProgress();
            idempotency.complete(RESULT_JSON);

            assertThatThrownBy(idempotency::retry)
                    .isInstanceOf(InvalidPaymentCancelIdempotencyStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("canRetry 술어")
    class CanRetryTest {

        @Test
        @DisplayName("FAILED 일 때만 canRetry 는 true 이다")
        void shouldReturnTrueOnlyWhenFailed() {
            PaymentCancelIdempotency idempotency = newInProgress();
            assertThat(idempotency.canRetry()).isFalse();

            idempotency.fail("reason");
            assertThat(idempotency.canRetry()).isTrue();

            idempotency.retry();
            idempotency.complete(RESULT_JSON);
            assertThat(idempotency.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class CreateFromSnapshotStateTest {

        @Test
        @DisplayName("스냅샷 상태를 그대로 복원한다")
        void shouldRestoreFromSnapshot() {
            PaymentCancelIdempotency idempotency = PaymentCancelIdempotency.from(
                    PaymentCancelIdempotencySnapshotState.builder()
                            .id(1L)
                            .idempotencyKey(KEY)
                            .paymentId(PAYMENT_ID)
                            .buyerId(BUYER_ID)
                            .status(PaymentCancelIdempotencyStatus.COMPLETED)
                            .result(RESULT_JSON)
                            .build()
            );

            assertThat(idempotency.getId()).isEqualTo(1L);
            assertThat(idempotency.isCompleted()).isTrue();
            assertThat(idempotency.getResult()).isEqualTo(RESULT_JSON);
        }
    }

    private PaymentCancelIdempotency newInProgress() {
        return PaymentCancelIdempotency.from(
                PaymentCancelIdempotencyCreateState.builder()
                        .idempotencyKey(KEY)
                        .paymentId(PAYMENT_ID)
                        .buyerId(BUYER_ID)
                        .build()
        );
    }
}
