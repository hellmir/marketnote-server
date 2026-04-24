package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundPaymentUseCase 테스트")
class RefundPaymentUseCaseTest {
    @Mock
    private FindPaymentPort findPaymentPort;
    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;
    @Mock
    private UpdatePaymentPort updatePaymentPort;
    @Mock
    private UpdatePspPaymentEventPort updatePspPaymentEventPort;
    @Mock
    private PaymentVendorPort paymentVendorPort;
    @Mock
    private SaveRefundPort saveRefundPort;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private RefundPaymentService refundPaymentService;

    private static final String ORDER_KEY = UUID.randomUUID().toString();
    private static final Long ORDER_ID = 1L;
    private static final Long PAYMENT_AMOUNT = 50000L;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(mock(TransactionStatus.class));
    }

    // ==================================================================================
    // 전체 취소 PG 환불 성공
    // ==================================================================================

    @Nested
    @DisplayName("전체 취소 PG 환불")
    class FullCancelRefundTest {

        @Test
        @DisplayName("전체 취소 시 PG STSC 모드로 취소하고 Payment/PspPaymentEvent를 업데이트하고 Refund를 저장한다")
        void refund_fullCancel_success() {
            Payment payment = createPayment(false, 0L);
            PspPaymentEvent event = createPspPaymentEvent(PaymentEventStatus.COMPLETE);
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any(PaymentCancelVendorCommand.class)))
                    .thenReturn(createSuccessVendorResult());

            RefundPaymentCommand command = createCommand(true, PAYMENT_AMOUNT, 0L);

            assertThatCode(() -> refundPaymentService.refund(command))
                    .doesNotThrowAnyException();

            ArgumentCaptor<PaymentCancelVendorCommand> vendorCaptor = ArgumentCaptor.forClass(PaymentCancelVendorCommand.class);
            verify(paymentVendorPort).cancelPayment(vendorCaptor.capture());
            PaymentCancelVendorCommand vendorCommand = vendorCaptor.getValue();
            assertThat(vendorCommand.cancelType()).isEqualTo("STSC");
            assertThat(vendorCommand.cancelAmount()).isEqualTo(PAYMENT_AMOUNT);
            assertThat(vendorCommand.remainAmount()).isEqualTo(0L);

            verify(updatePaymentPort).update(payment);
            verify(updatePspPaymentEventPort).update(event);
            verify(saveRefundPort).save(any(Refund.class));
        }
    }

    // ==================================================================================
    // 멱등성 — 이미 환불된 결제
    // ==================================================================================

    @Nested
    @DisplayName("멱등성 처리")
    class IdempotencyTest {

        @Test
        @DisplayName("Payment가 이미 환불된 상태이면 PaymentAlreadyRefundedException이 발생한다")
        void refund_alreadyRefundedPayment_throwsException() {
            Payment payment = createPayment(true, PAYMENT_AMOUNT);
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));

            RefundPaymentCommand command = createCommand(true, PAYMENT_AMOUNT, 0L);

            assertThatThrownBy(() -> refundPaymentService.refund(command))
                    .isInstanceOf(PaymentAlreadyRefundedException.class);

            verifyNoInteractions(paymentVendorPort);
            verifyNoInteractions(updatePaymentPort);
        }

        @Test
        @DisplayName("PspPaymentEvent가 환불 불가 상태이면 PaymentAlreadyRefundedException이 발생한다")
        void refund_nonRefundableEvent_throwsException() {
            Payment payment = createPayment(false, 0L);
            PspPaymentEvent event = createPspPaymentEvent(PaymentEventStatus.CANCELLED);
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));

            RefundPaymentCommand command = createCommand(true, PAYMENT_AMOUNT, 0L);

            assertThatThrownBy(() -> refundPaymentService.refund(command))
                    .isInstanceOf(PaymentAlreadyRefundedException.class);

            verifyNoInteractions(paymentVendorPort);
        }
    }

    // ==================================================================================
    // 조회 실패
    // ==================================================================================

    @Nested
    @DisplayName("조회 실패")
    class NotFoundTest {

        @Test
        @DisplayName("Payment 조회 실패 시 PaymentNotFoundException이 발생한다")
        void refund_paymentNotFound_throwsException() {
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.empty());

            RefundPaymentCommand command = createCommand(true, PAYMENT_AMOUNT, 0L);

            assertThatThrownBy(() -> refundPaymentService.refund(command))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        @DisplayName("PspPaymentEvent 조회 실패 시 PaymentNotFoundException이 발생한다")
        void refund_pspEventNotFound_throwsException() {
            Payment payment = createPayment(false, 0L);
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            RefundPaymentCommand command = createCommand(true, PAYMENT_AMOUNT, 0L);

            assertThatThrownBy(() -> refundPaymentService.refund(command))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    // ==================================================================================
    // PG 벤더 취소 실패
    // ==================================================================================

    @Nested
    @DisplayName("PG 벤더 취소 실패")
    class VendorFailureTest {

        @Test
        @DisplayName("PG 벤더 취소 실패 시 PaymentCancelException이 발생한다")
        void refund_vendorFailed_throwsException() {
            Payment payment = createPayment(false, 0L);
            PspPaymentEvent event = createPspPaymentEvent(PaymentEventStatus.COMPLETE);
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any(PaymentCancelVendorCommand.class)))
                    .thenReturn(createFailureVendorResult());

            RefundPaymentCommand command = createCommand(true, PAYMENT_AMOUNT, 0L);

            assertThatThrownBy(() -> refundPaymentService.refund(command))
                    .isInstanceOf(PaymentCancelException.class);

            verifyNoInteractions(updatePaymentPort);
            verifyNoInteractions(saveRefundPort);
        }
    }

    // ==================================================================================
    // 부분 취소
    // ==================================================================================

    @Nested
    @DisplayName("부분 취소 PG 환불")
    class PartialCancelRefundTest {

        @Test
        @DisplayName("부분 취소 시 PG STPC 모드로 취소하고 부분 환불 처리한다")
        void refund_partialCancel_success() {
            Payment payment = createPayment(false, 0L);
            PspPaymentEvent event = createPspPaymentEvent(PaymentEventStatus.COMPLETE);
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any(PaymentCancelVendorCommand.class)))
                    .thenReturn(createSuccessVendorResult());

            Long cancelAmount = 20000L;
            RefundPaymentCommand command = createCommand(false, cancelAmount, 0L);

            assertThatCode(() -> refundPaymentService.refund(command))
                    .doesNotThrowAnyException();

            ArgumentCaptor<PaymentCancelVendorCommand> vendorCaptor = ArgumentCaptor.forClass(PaymentCancelVendorCommand.class);
            verify(paymentVendorPort).cancelPayment(vendorCaptor.capture());
            PaymentCancelVendorCommand vendorCommand = vendorCaptor.getValue();
            assertThat(vendorCommand.cancelType()).isEqualTo("STPC");
            assertThat(vendorCommand.cancelAmount()).isEqualTo(cancelAmount);
            assertThat(vendorCommand.remainAmount()).isEqualTo(PAYMENT_AMOUNT - cancelAmount);

            verify(updatePaymentPort).update(payment);
            verify(updatePspPaymentEventPort).update(event);
            verify(saveRefundPort).save(any(Refund.class));
        }
    }

    // ==================================================================================
    // Refund 저장 실패해도 Payment/PspPaymentEvent 업데이트는 유지
    // ==================================================================================

    @Nested
    @DisplayName("Refund 저장 실패 격리")
    class RefundSaveFailureTest {

        @Test
        @DisplayName("Refund 저장 실패해도 예외가 전파되지 않는다")
        void refund_refundSaveFailed_doesNotThrow() {
            Payment payment = createPayment(false, 0L);
            PspPaymentEvent event = createPspPaymentEvent(PaymentEventStatus.COMPLETE);
            when(findPaymentPort.findByOrderKey(UUID.fromString(ORDER_KEY))).thenReturn(Optional.of(payment));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any(PaymentCancelVendorCommand.class)))
                    .thenReturn(createSuccessVendorResult());
            doThrow(new RuntimeException("DB 장애")).when(saveRefundPort).save(any(Refund.class));

            RefundPaymentCommand command = createCommand(true, PAYMENT_AMOUNT, 0L);

            assertThatCode(() -> refundPaymentService.refund(command))
                    .doesNotThrowAnyException();

            verify(updatePaymentPort).update(payment);
            verify(updatePspPaymentEventPort).update(event);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private RefundPaymentCommand createCommand(boolean isFullCancel, Long cancelAmount, Long alreadyRefunded) {
        return RefundPaymentCommand.builder()
                .orderKey(ORDER_KEY)
                .orderId(ORDER_ID)
                .cancelAmount(cancelAmount)
                .paymentAmount(PAYMENT_AMOUNT)
                .isFullCancel(isFullCancel)
                .alreadyRefunded(alreadyRefunded)
                .build();
    }

    private Payment createPayment(boolean refundedYn, Long refundAmount) {
        return Payment.from(PaymentSnapshotState.builder()
                .id(10L)
                .orderId(ORDER_ID)
                .orderKey(UUID.fromString(ORDER_KEY))
                .pgPaymentKey("TID-001")
                .paymentAmount(PAYMENT_AMOUNT)
                .successYn(true)
                .refundedYn(refundedYn)
                .refundAmount(refundAmount)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private PspPaymentEvent createPspPaymentEvent(PaymentEventStatus status) {
        return PspPaymentEvent.from(PspPaymentEventSnapshotState.builder()
                .id(20L)
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY)
                .pgPaymentKey("TID-001")
                .poStatus(status)
                .amount(PAYMENT_AMOUNT)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private PaymentCancelVendorResult createSuccessVendorResult() {
        return PaymentCancelVendorResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("성공")
                .rawResponse("{}")
                .build();
    }

    private PaymentCancelVendorResult createFailureVendorResult() {
        return PaymentCancelVendorResult.builder()
                .success(false)
                .resultCode("9999")
                .resultMessage("PG 통신 오류")
                .rawResponse("{}")
                .build();
    }

    private static org.assertj.core.api.AbstractObjectAssert<?, ?> assertThat(Object actual) {
        return org.assertj.core.api.Assertions.assertThat(actual);
    }
}
