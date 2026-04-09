package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.InvalidPaymentEventResolveStatusException;
import com.personal.marketnote.commerce.exception.PaymentEventNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ResolveUnknownPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ResolveUnknownPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePaymentPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePspPaymentEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResolveUnknownPaymentUseCase 테스트")
class ResolveUnknownPaymentUseCaseTest {

    @InjectMocks
    private ResolveUnknownPaymentService resolveUnknownPaymentService;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    @Mock
    private UpdatePspPaymentEventPort updatePspPaymentEventPort;

    @Mock
    private FindPaymentPort findPaymentPort;

    @Mock
    private UpdatePaymentPort updatePaymentPort;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    private static final String ORDER_KEY = UUID.randomUUID().toString();
    private static final Long ORDER_ID = 1L;

    @Nested
    @DisplayName("COMPLETE 해소")
    class ResolveToCompleteTest {

        @Test
        @DisplayName("UNKNOWN 상태의 결제 이벤트를 COMPLETE로 해소하면 결제 성공 처리 및 분개 기록이 수행된다")
        void shouldResolveToCompleteSuccessfully() {
            // given
            PspPaymentEvent event = createUnknownEvent();
            Payment payment = createPayment();
            ResolveUnknownPaymentCommand command = ResolveUnknownPaymentCommand.builder()
                    .orderKey(ORDER_KEY)
                    .resolvedStatus("COMPLETE")
                    .resultCode("0000")
                    .resultMessage("승인 성공")
                    .pgPaymentKey("tno_123")
                    .approvalNumber("12345678")
                    .appTime("20260402150000")
                    .build();

            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));

            // when
            ResolveUnknownPaymentResult result = resolveUnknownPaymentService.resolve(command);

            // then
            assertThat(result.orderKey()).isEqualTo(ORDER_KEY);
            assertThat(result.resolvedStatus()).isEqualTo("COMPLETE");
            assertThat(result.orderId()).isEqualTo(ORDER_ID);

            verify(updatePspPaymentEventPort).update(event);
            verify(updatePaymentPort).update(payment);
            verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
            verify(recordLedgerEntryUseCase).recordPaymentApproval(ORDER_ID, 50000L);
        }

        @Test
        @DisplayName("COMPLETE 해소 시 분개 기록이 실패해도 해소 처리는 정상 완료된다")
        void shouldCompleteResolveEvenWhenLedgerRecordFails() {
            // given
            PspPaymentEvent event = createUnknownEvent();
            Payment payment = createPayment();
            ResolveUnknownPaymentCommand command = ResolveUnknownPaymentCommand.builder()
                    .orderKey(ORDER_KEY)
                    .resolvedStatus("COMPLETE")
                    .resultCode("0000")
                    .resultMessage("승인 성공")
                    .pgPaymentKey("tno_123")
                    .approvalNumber("12345678")
                    .appTime("20260402150000")
                    .build();

            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));
            doThrow(new RuntimeException("분개 실패"))
                    .when(recordLedgerEntryUseCase).recordPaymentApproval(ORDER_ID, 50000L);

            // when
            ResolveUnknownPaymentResult result = resolveUnknownPaymentService.resolve(command);

            // then
            assertThat(result.resolvedStatus()).isEqualTo("COMPLETE");
            verify(updatePspPaymentEventPort).update(event);
            verify(updatePaymentPort).update(payment);
            verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }
    }

    @Nested
    @DisplayName("FAILED 해소")
    class ResolveToFailedTest {

        @Test
        @DisplayName("UNKNOWN 상태의 결제 이벤트를 FAILED로 해소하면 결제 실패 처리가 수행된다")
        void shouldResolveToFailedSuccessfully() {
            // given
            PspPaymentEvent event = createUnknownEvent();
            Payment payment = createPayment();
            ResolveUnknownPaymentCommand command = ResolveUnknownPaymentCommand.builder()
                    .orderKey(ORDER_KEY)
                    .resolvedStatus("FAILED")
                    .resultCode("9999")
                    .resultMessage("결제 실패")
                    .build();

            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));

            // when
            ResolveUnknownPaymentResult result = resolveUnknownPaymentService.resolve(command);

            // then
            assertThat(result.orderKey()).isEqualTo(ORDER_KEY);
            assertThat(result.resolvedStatus()).isEqualTo("FAILED");
            assertThat(result.orderId()).isEqualTo(ORDER_ID);

            verify(updatePspPaymentEventPort).update(event);
            verify(updatePaymentPort).update(payment);
            verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
            verifyNoInteractions(recordLedgerEntryUseCase);
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("결제 이벤트가 존재하지 않으면 PaymentEventNotFoundException이 발생한다")
        void shouldThrowWhenPaymentEventNotFound() {
            // given
            ResolveUnknownPaymentCommand command = ResolveUnknownPaymentCommand.builder()
                    .orderKey(ORDER_KEY)
                    .resolvedStatus("COMPLETE")
                    .build();

            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> resolveUnknownPaymentService.resolve(command))
                    .isInstanceOf(PaymentEventNotFoundException.class);

            verifyNoInteractions(findPaymentPort, updatePspPaymentEventPort, updatePaymentPort,
                    changeOrderStatusUseCase, recordLedgerEntryUseCase);
        }

        @Test
        @DisplayName("결제 정보가 존재하지 않으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            // given
            PspPaymentEvent event = createUnknownEvent();
            ResolveUnknownPaymentCommand command = ResolveUnknownPaymentCommand.builder()
                    .orderKey(ORDER_KEY)
                    .resolvedStatus("COMPLETE")
                    .build();

            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> resolveUnknownPaymentService.resolve(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verifyNoInteractions(updatePspPaymentEventPort, updatePaymentPort,
                    changeOrderStatusUseCase, recordLedgerEntryUseCase);
        }

        @Test
        @DisplayName("유효하지 않은 해소 상태를 전달하면 InvalidPaymentEventResolveStatusException이 발생한다")
        void shouldThrowWhenInvalidResolvedStatus() {
            // given
            PspPaymentEvent event = createUnknownEvent();
            Payment payment = createPayment();
            ResolveUnknownPaymentCommand command = ResolveUnknownPaymentCommand.builder()
                    .orderKey(ORDER_KEY)
                    .resolvedStatus("INVALID")
                    .build();

            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));

            // when & then
            assertThatThrownBy(() -> resolveUnknownPaymentService.resolve(command))
                    .isInstanceOf(InvalidPaymentEventResolveStatusException.class);

            verifyNoInteractions(updatePspPaymentEventPort, updatePaymentPort,
                    changeOrderStatusUseCase, recordLedgerEntryUseCase);
        }
    }

    private PspPaymentEvent createUnknownEvent() {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY)
                .poStatus(PaymentEventStatus.UNKNOWN)
                .method("CARD")
                .amount(50000L)
                .resultCode("9999")
                .resultMessage("결제 상태 미확인")
                .createdAt(LocalDateTime.of(2026, 4, 2, 10, 0, 0))
                .build();
        return PspPaymentEvent.from(state);
    }

    private Payment createPayment() {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(ORDER_ID)
                .orderKey(UUID.fromString(ORDER_KEY))
                .paymentAmount(50000L)
                .build();
        return Payment.from(state);
    }
}
