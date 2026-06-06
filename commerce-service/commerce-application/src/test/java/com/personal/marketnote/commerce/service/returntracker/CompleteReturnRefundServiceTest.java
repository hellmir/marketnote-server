package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnRefundStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerSnapshotState;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.exception.ReturnPgRefundFailedException;
import com.personal.marketnote.commerce.exception.ReturnTrackerNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.RefundPaymentUseCase;
import com.personal.marketnote.commerce.port.out.returntracker.FindReturnTrackerPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteReturnRefundService 테스트")
class CompleteReturnRefundServiceTest {

    @InjectMocks
    private CompleteReturnRefundService service;

    @Mock
    private RefundPaymentUseCase refundPaymentUseCase;

    @Mock
    private FindReturnTrackerPort findReturnTrackerPort;

    @Mock
    private UpdateReturnTrackerPort updateReturnTrackerPort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-03T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Nested
    @DisplayName("PG 환불 성공")
    class RefundSuccess {

        @Test
        @DisplayName("PG 환불 성공 시 ReturnTracker refundStatus가 COMPLETED로 변경된다")
        void shouldCompleteRefundAndUpdateTrackerStatus() {
            ReturnTracker tracker = createTracker(ReturnRefundStatus.PENDING);
            when(findReturnTrackerPort.findByOrderId(1L)).thenReturn(Optional.of(tracker));

            CompleteReturnRefundCommand command = createCommand();

            service.completeReturnRefund(command);

            verify(refundPaymentUseCase).refund(argThat(cmd ->
                    cmd.orderKey().equals("ORDER-KEY-001")
                            && cmd.orderId().equals(1L)
                            && cmd.cancelAmount().equals(30000L)
                            && cmd.paymentAmount().equals(50000L)
                            && cmd.isFullCancel()
                            && cmd.returnShippingFee().equals(3000L)));
            assertThat(tracker.isRefundCompleted()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
        }
    }

    @Nested
    @DisplayName("PG 환불 실패")
    class RefundFailure {

        @Test
        @DisplayName("PG 환불 실패 시 ReturnTracker refundStatus가 FAILED로 변경되고 ReturnPgRefundFailedException을 던진다")
        void shouldMarkRefundAsFailedOnPgErrorAndThrowException() {
            ReturnTracker tracker = createTracker(ReturnRefundStatus.PENDING);
            when(findReturnTrackerPort.findByOrderId(1L)).thenReturn(Optional.of(tracker));
            doThrow(new PaymentCancelException("PG 환불 실패"))
                    .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

            CompleteReturnRefundCommand command = createCommand();

            assertThatThrownBy(() -> service.completeReturnRefund(command))
                    .isInstanceOf(ReturnPgRefundFailedException.class);

            assertThat(tracker.isRefundFailed()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
        }

        @Test
        @DisplayName("ReturnTracker를 찾을 수 없으면 ReturnTrackerNotFoundException을 던진다")
        void shouldThrowWhenTrackerNotFound() {
            when(findReturnTrackerPort.findByOrderId(1L)).thenReturn(Optional.empty());

            CompleteReturnRefundCommand command = createCommand();

            assertThatThrownBy(() -> service.completeReturnRefund(command))
                    .isInstanceOf(ReturnTrackerNotFoundException.class);

            verifyNoInteractions(refundPaymentUseCase);
            verifyNoInteractions(updateReturnTrackerPort);
        }
    }

    @Nested
    @DisplayName("멱등성 처리")
    class Idempotency {

        @Test
        @DisplayName("이미 환불 완료된 결제이면 refundStatus를 변경하지 않고 정상 종료한다")
        void shouldHandleAlreadyRefundedPayment() {
            ReturnTracker tracker = createTracker(ReturnRefundStatus.PENDING);
            when(findReturnTrackerPort.findByOrderId(1L)).thenReturn(Optional.of(tracker));
            doThrow(new PaymentAlreadyRefundedException("ORDER-KEY-001"))
                    .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

            CompleteReturnRefundCommand command = createCommand();

            service.completeReturnRefund(command);

            assertThat(tracker.isRefundCompleted()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
        }

        @Test
        @DisplayName("이미 환불 완료된 ReturnTracker이면 PG 환불을 호출하지 않고 정상 종료한다")
        void shouldSkipRefundWhenTrackerAlreadyCompleted() {
            ReturnTracker tracker = createTracker(ReturnRefundStatus.COMPLETED);
            when(findReturnTrackerPort.findByOrderId(1L)).thenReturn(Optional.of(tracker));

            CompleteReturnRefundCommand command = createCommand();

            service.completeReturnRefund(command);

            verifyNoInteractions(refundPaymentUseCase);
            assertThat(tracker.isRefundCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("DLT 재시도")
    class DltRetry {

        @Test
        @DisplayName("FAILED 상태의 ReturnTracker를 PENDING으로 리셋하고 PG 환불을 재시도한다")
        void shouldRetryRefundWhenTrackerIsFailed() {
            ReturnTracker tracker = createTracker(ReturnRefundStatus.FAILED);
            when(findReturnTrackerPort.findByOrderId(1L)).thenReturn(Optional.of(tracker));

            CompleteReturnRefundCommand command = createCommand();

            service.completeReturnRefund(command);

            verify(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));
            assertThat(tracker.isRefundCompleted()).isTrue();
            verify(updateReturnTrackerPort, times(2)).update(tracker);
        }

        @Test
        @DisplayName("FAILED 상태에서 재시도 후 다시 실패하면 FAILED로 유지하고 예외를 던진다")
        void shouldFailAgainOnRetryAndThrowException() {
            ReturnTracker tracker = createTracker(ReturnRefundStatus.FAILED);
            when(findReturnTrackerPort.findByOrderId(1L)).thenReturn(Optional.of(tracker));
            doThrow(new PaymentCancelException("PG 환불 재실패"))
                    .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

            CompleteReturnRefundCommand command = createCommand();

            assertThatThrownBy(() -> service.completeReturnRefund(command))
                    .isInstanceOf(ReturnPgRefundFailedException.class);

            assertThat(tracker.isRefundFailed()).isTrue();
            verify(updateReturnTrackerPort, times(2)).update(tracker);
        }
    }

    private CompleteReturnRefundCommand createCommand() {
        return CompleteReturnRefundCommand.builder()
                .orderId(1L)
                .orderKey("ORDER-KEY-001")
                .buyerId(100L)
                .returnAmount(30000L)
                .paymentAmount(50000L)
                .pointAmount(1000L)
                .shippingFee(3000L)
                .isFullReturn(true)
                .returnShippingFee(3000L)
                .build();
    }

    private ReturnTracker createTracker(ReturnRefundStatus refundStatus) {
        return ReturnTracker.from(ReturnTrackerSnapshotState.builder()
                .id(1L)
                .orderId(1L)
                .returnSlipNumber("RS-001")
                .inspectionStatus(ReturnInspectionStatus.PASSED)
                .refundStatus(refundStatus)
                .inspectedAt(LocalDateTime.of(2026, 4, 9, 10, 0))
                .build());
    }
}
