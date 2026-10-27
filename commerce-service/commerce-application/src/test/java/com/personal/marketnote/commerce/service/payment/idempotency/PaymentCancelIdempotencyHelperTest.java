package com.personal.marketnote.commerce.service.payment.idempotency;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencyCreateState;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencySnapshotState;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencyStatus;
import com.personal.marketnote.commerce.exception.DuplicateIdempotencyKeyException;
import com.personal.marketnote.commerce.exception.IdempotencyKeyInProgressException;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.FindPaymentCancelIdempotencyPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.SavePaymentCancelIdempotencyPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.UpdatePaymentCancelIdempotencyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelIdempotencyHelper 테스트")
class PaymentCancelIdempotencyHelperTest {

    @InjectMocks
    private PaymentCancelIdempotencyHelper helper;

    @Mock
    private FindPaymentPort findPaymentPort;

    @Mock
    private FindPaymentCancelIdempotencyPort findIdempotencyPort;

    @Mock
    private SavePaymentCancelIdempotencyPort saveIdempotencyPort;

    @Mock
    private UpdatePaymentCancelIdempotencyPort updateIdempotencyPort;

    private static final String KEY = "11111111-2222-4333-8444-555555555555";
    private static final Long BUYER_ID = 100L;
    private static final Long PAYMENT_ID = 7L;
    private static final UUID ORDER_KEY_UUID = UUID.randomUUID();
    private static final String ORDER_KEY = ORDER_KEY_UUID.toString();

    @Nested
    @DisplayName("findOrCreate")
    class FindOrCreateTest {

        @Test
        @DisplayName("기존 레코드가 없으면 신규 INSERT 후 NEW 시그널을 반환한다")
        void shouldReturnNewWhenAbsent() {
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());
            when(findPaymentPort.findByOrderKey(ORDER_KEY_UUID)).thenReturn(Optional.of(samplePayment()));

            PaymentCancelIdempotencyResolution result = helper.findOrCreate(KEY, ORDER_KEY, BUYER_ID);

            assertThat(result.signal()).isEqualTo(PaymentCancelIdempotencyResolution.Signal.NEW);
            verify(saveIdempotencyPort).save(any(PaymentCancelIdempotency.class));
        }

        @Test
        @DisplayName("기존 레코드가 COMPLETED 이면 CACHED 시그널과 저장된 result 를 반환한다")
        void shouldReturnCachedWhenCompleted() {
            PaymentCancelIdempotency completed = snapshot(PaymentCancelIdempotencyStatus.COMPLETED, "{\"ok\":true}");
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.of(completed));

            PaymentCancelIdempotencyResolution result = helper.findOrCreate(KEY, ORDER_KEY, BUYER_ID);

            assertThat(result.signal()).isEqualTo(PaymentCancelIdempotencyResolution.Signal.CACHED);
            assertThat(result.cachedResult()).isEqualTo("{\"ok\":true}");
            verify(saveIdempotencyPort, never()).save(any());
        }

        @Test
        @DisplayName("기존 레코드가 IN_PROGRESS 이면 IdempotencyKeyInProgressException 을 던진다")
        void shouldThrowWhenInProgress() {
            PaymentCancelIdempotency inProgress = snapshot(PaymentCancelIdempotencyStatus.IN_PROGRESS, null);
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.of(inProgress));

            assertThatThrownBy(() -> helper.findOrCreate(KEY, ORDER_KEY, BUYER_ID))
                    .isInstanceOf(IdempotencyKeyInProgressException.class);
            verify(saveIdempotencyPort, never()).save(any());
        }

        @Test
        @DisplayName("기존 레코드가 FAILED 이면 retry() 후 RETRY 시그널을 반환한다")
        void shouldTransitionToInProgressAndReturnRetryWhenFailed() {
            PaymentCancelIdempotency failed = snapshot(PaymentCancelIdempotencyStatus.FAILED, null);
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.of(failed));

            PaymentCancelIdempotencyResolution result = helper.findOrCreate(KEY, ORDER_KEY, BUYER_ID);

            assertThat(result.signal()).isEqualTo(PaymentCancelIdempotencyResolution.Signal.RETRY);
            verify(updateIdempotencyPort).update(failed);
            assertThat(failed.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("INSERT UNIQUE 충돌 시 재조회로 경합을 해소한다 — 상대가 COMPLETED 면 CACHED 반환")
        void shouldResolveRaceWhenDuplicateInsert() {
            when(findIdempotencyPort.findByIdempotencyKey(KEY))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(snapshot(PaymentCancelIdempotencyStatus.COMPLETED, "{\"from\":\"peer\"}")));
            when(findPaymentPort.findByOrderKey(ORDER_KEY_UUID)).thenReturn(Optional.of(samplePayment()));
            when(saveIdempotencyPort.save(any(PaymentCancelIdempotency.class)))
                    .thenThrow(new DuplicateIdempotencyKeyException(KEY));

            PaymentCancelIdempotencyResolution result = helper.findOrCreate(KEY, ORDER_KEY, BUYER_ID);

            assertThat(result.signal()).isEqualTo(PaymentCancelIdempotencyResolution.Signal.CACHED);
            assertThat(result.cachedResult()).isEqualTo("{\"from\":\"peer\"}");
        }
    }

    @Nested
    @DisplayName("markCompleted")
    class MarkCompletedTest {

        @Test
        @DisplayName("IN_PROGRESS 인 레코드를 COMPLETED 로 전이하고 result 를 저장한다")
        void shouldCompleteWhenInProgress() {
            PaymentCancelIdempotency inProgress = snapshot(PaymentCancelIdempotencyStatus.IN_PROGRESS, null);
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.of(inProgress));

            helper.markCompleted(KEY, "{\"done\":true}");

            ArgumentCaptor<PaymentCancelIdempotency> captor = ArgumentCaptor.forClass(PaymentCancelIdempotency.class);
            verify(updateIdempotencyPort).update(captor.capture());
            assertThat(captor.getValue().isCompleted()).isTrue();
            assertThat(captor.getValue().getResult()).isEqualTo("{\"done\":true}");
        }

        @Test
        @DisplayName("레코드 없음 → 업데이트 스킵 (예외 던지지 않음)")
        void shouldSkipWhenAbsent() {
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());

            helper.markCompleted(KEY, "{}");

            verify(updateIdempotencyPort, never()).update(any());
        }
    }

    @Nested
    @DisplayName("markFailed")
    class MarkFailedTest {

        @Test
        @DisplayName("IN_PROGRESS 인 레코드를 FAILED 로 전이한다")
        void shouldFailWhenInProgress() {
            PaymentCancelIdempotency inProgress = snapshot(PaymentCancelIdempotencyStatus.IN_PROGRESS, null);
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.of(inProgress));

            helper.markFailed(KEY, "DB 오류");

            ArgumentCaptor<PaymentCancelIdempotency> captor = ArgumentCaptor.forClass(PaymentCancelIdempotency.class);
            verify(updateIdempotencyPort).update(captor.capture());
            assertThat(captor.getValue().isFailed()).isTrue();
            assertThat(captor.getValue().getFailedReason()).isEqualTo("DB 오류");
        }

        @Test
        @DisplayName("이미 COMPLETED 인 레코드는 업데이트 스킵")
        void shouldSkipWhenAlreadyCompleted() {
            PaymentCancelIdempotency completed = snapshot(PaymentCancelIdempotencyStatus.COMPLETED, "{}");
            when(findIdempotencyPort.findByIdempotencyKey(KEY)).thenReturn(Optional.of(completed));

            helper.markFailed(KEY, "reason");

            verify(updateIdempotencyPort, never()).update(any());
        }

        @Test
        @DisplayName("내부 예외가 발생해도 전파되지 않는다 (원본 실패 원인 가림 방지)")
        void shouldSwallowInternalException() {
            when(findIdempotencyPort.findByIdempotencyKey(KEY))
                    .thenThrow(new RuntimeException("조회 실패"));

            helper.markFailed(KEY, "reason");

            verify(updateIdempotencyPort, never()).update(any());
        }
    }

    private Payment samplePayment() {
        Payment payment = Payment.from(PaymentCreateState.builder()
                .orderId(1L)
                .orderKey(ORDER_KEY_UUID)
                .paymentAmount(10000L)
                .build());
        payment.markAsSuccess("tno");
        return PaymentCancelIdempotencyHelperTest.inject(payment, PAYMENT_ID);
    }

    private static Payment inject(Payment payment, Long id) {
        try {
            java.lang.reflect.Field field = Payment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(payment, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return payment;
    }

    private PaymentCancelIdempotency snapshot(PaymentCancelIdempotencyStatus status, String result) {
        return PaymentCancelIdempotency.from(
                PaymentCancelIdempotencySnapshotState.builder()
                        .id(1L)
                        .idempotencyKey(KEY)
                        .paymentId(PAYMENT_ID)
                        .buyerId(BUYER_ID)
                        .status(status)
                        .result(result)
                        .build()
        );
    }
}
