package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPaymentUseCase 테스트")
class CancelPaymentUseCaseTest {

    @InjectMocks
    private CancelPaymentService cancelPaymentService;

    @Mock
    private FindOrderPort findOrderPort;

    @Mock
    private FindPaymentPort findPaymentPort;

    @Mock
    private UpdatePaymentPort updatePaymentPort;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    @Mock
    private UpdatePspPaymentEventPort updatePspPaymentEventPort;

    @Mock
    private PaymentVendorPort paymentVendorPort;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Mock
    private RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @Mock
    private ModifyUserPointPort modifyUserPointPort;

    private static final Long BUYER_ID = 100L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("전체 취소 성공")
    class FullCancelSuccessTest {

        @Test
        @DisplayName("전체 취소 성공 시 Payment가 환불 상태로 변경된다")
        void shouldMarkPaymentAsRefunded() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(argThat(p ->
                    Boolean.TRUE.equals(p.getRefundedYn()) && p.getRefundAmount().equals(50000L)
            ));
        }

        @Test
        @DisplayName("전체 취소 시 KCP에 STSC 모드와 전체 금액이 전송된다")
        void shouldSendFullCancelToVendor() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(paymentVendorPort).cancelPayment(argThat(c ->
                    "STSC".equals(c.modType())
                            && c.modMny().equals(50000L)
                            && c.remMny().equals(0L)
                            && "tno_123".equals(c.tno())
            ));
        }

        @Test
        @DisplayName("전체 취소 성공 시 주문 상태가 CANCEL_REQUESTED로 변경된다")
        void shouldChangeOrderStatusToCancelRequested() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(changeOrderStatusUseCase).changeOrderStatus(argThat(c ->
                    c.orderStatus() == OrderStatus.CANCEL_REQUESTED && c.id().equals(1L)
            ));
        }

        @Test
        @DisplayName("이미 부분환불된 결제의 전체 취소 시 잔여액만 취소된다")
        void shouldCancelOnlyRemainingAmountWhenPartiallyRefunded() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            payment.markAsPartiallyRefunded(10000L);
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(paymentVendorPort).cancelPayment(argThat(c ->
                    c.modMny().equals(40000L) && c.remMny().equals(0L)
            ));
        }
    }

    @Nested
    @DisplayName("부분 취소 성공")
    class PartialCancelSuccessTest {

        @Test
        @DisplayName("부분 취소 성공 시 환불 금액이 누적된다")
        void shouldAccumulateRefundAmount() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(argThat(p ->
                    p.getRefundAmount().equals(20000L) && Boolean.FALSE.equals(p.getRefundedYn())
            ));
        }

        @Test
        @DisplayName("부분 취소 시 KCP에 STPC 모드와 부분 금액이 전송된다")
        void shouldSendPartialCancelToVendor() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(paymentVendorPort).cancelPayment(argThat(c ->
                    "STPC".equals(c.modType())
                            && c.modMny().equals(20000L)
                            && c.remMny().equals(30000L)
            ));
        }

        @Test
        @DisplayName("부분 취소 시 주문 상태는 변경되지 않는다")
        void shouldNotChangeOrderStatusOnPartialCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(changeOrderStatusUseCase, never()).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("부분 취소 금액 검증")
    class PartialCancelValidationTest {

        @Test
        @DisplayName("부분 취소 금액이 null이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenCancelAmountIsNull() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = CancelPaymentCommand.builder()
                    .buyerId(BUYER_ID)
                    .orderKey(ORDER_KEY_STR)
                    .cancelType(CancelPaymentCommand.CancelType.PARTIAL)
                    .cancelAmount(null)
                    .build();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("부분 취소 금액이 0이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenCancelAmountIsZero() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 0L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("부분 취소 금액이 음수이면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenCancelAmountIsNegative() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, -10000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야 합니다");

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("부분 취소 금액이 환불 가능 금액을 초과하면 IllegalArgumentException이 발생한다")
        void shouldThrowWhenCancelAmountExceedsRefundable() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 60000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("환불 가능 금액");

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("이미 부분환불된 상태에서 잔여액을 초과하는 부분 취소는 실패한다")
        void shouldThrowWhenCancelAmountExceedsRemainingAfterPartialRefund() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            payment.markAsPartiallyRefunded(30000L);
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 25000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("환불 가능 금액");

            verify(paymentVendorPort, never()).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("주문 소유자 검증")
    class OrderOwnerVerificationTest {

        @Test
        @DisplayName("주문 소유자가 아닌 사용자가 결제 취소 시 UnauthorizedOrderAccessException이 발생한다")
        void shouldThrowWhenBuyerIsNotOrderOwner() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            Long attackerBuyerId = 999L;
            CancelPaymentCommand command = CancelPaymentCommand.builder()
                    .buyerId(attackerBuyerId)
                    .orderKey(ORDER_KEY_STR)
                    .cancelType(CancelPaymentCommand.CancelType.FULL)
                    .cancelReason("고객 요청")
                    .build();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 OrderNotFoundException이 발생한다")
        void shouldThrowWhenOrderNotFound() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(com.personal.marketnote.commerce.exception.OrderNotFoundException.class);

            verify(paymentVendorPort, never()).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("분개 기록 검증")
    class LedgerEntryRecordingTest {

        @Test
        @DisplayName("전체 취소 성공 시 역분개가 기록된다")
        void shouldRecordReverseLedgerEntryOnFullCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(recordLedgerEntryUseCase).recordPaymentCancellation(
                    eq(1L), eq(50000L), eq("PAYMENT_CANCELLATION:1")
            );
        }

        @Test
        @DisplayName("부분 취소 성공 시 부분 환불 역분개가 기록된다")
        void shouldRecordReverseLedgerEntryOnPartialCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(recordLedgerEntryUseCase).recordPaymentCancellation(
                    eq(1L), eq(20000L), eq("PAYMENT_PARTIAL_REFUND:1:20000:0")
            );
        }

        @Test
        @DisplayName("이미 부분환불된 상태에서 추가 부분 취소 시 누적 환불액이 멱등성 키에 포함된다")
        void shouldIncludeAccumulatedRefundInIdempotencyKey() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            payment.markAsPartiallyRefunded(10000L);
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(recordLedgerEntryUseCase).recordPaymentCancellation(
                    eq(1L), eq(20000L), eq("PAYMENT_PARTIAL_REFUND:1:20000:10000")
            );
        }

        @Test
        @DisplayName("역분개 실패 시에도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenLedgerRecordingFails() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            doThrow(new RuntimeException("분개 실패")).when(recordLedgerEntryUseCase)
                    .recordPaymentCancellation(anyLong(), anyLong(), anyString());

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
            verify(changeOrderStatusUseCase).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("재고 복구 검증")
    class InventoryRestoreTest {

        @Test
        @DisplayName("전체 취소 성공 시 재고 복구가 호출된다")
        void shouldRestoreInventoryOnFullCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(restoreProductInventoryUseCase).restore(anyList(), anyString());
        }

        @Test
        @DisplayName("부분 취소 시 재고 복구가 호출되지 않는다")
        void shouldNotRestoreInventoryOnPartialCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyString());
        }

        @Test
        @DisplayName("재고 복구 실패 시에도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenInventoryRestoreFails() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            doThrow(new RuntimeException("재고 복구 실패")).when(restoreProductInventoryUseCase)
                    .restore(anyList(), anyString());

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
            verify(changeOrderStatusUseCase).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("KCP 취소 실패")
    class VendorFailureTest {

        @Test
        @DisplayName("KCP 취소 응답이 실패이면 PaymentCancelException이 발생한다")
        void shouldThrowWhenVendorReturnsError() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = PaymentCancelVendorResult.builder()
                    .resCd("8001")
                    .resMsg("취소 불가")
                    .rawResponse("{}")
                    .build();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(PaymentCancelException.class);

            verify(updatePaymentPort, never()).update(any());
        }
    }

    @Nested
    @DisplayName("결제 미존재")
    class PaymentNotFoundTest {

        @Test
        @DisplayName("orderKey에 해당하는 결제가 없으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("PspPaymentEvent가 없으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenEventNotFound() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(paymentVendorPort, never()).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("포인트 환불 검증")
    class PointRefundTest {

        @Test
        @DisplayName("전체 취소 시 포인트가 사용된 주문이면 포인트 환불이 호출된다")
        void shouldRefundPointsOnFullCancelWhenPointUsed() {
            Long pointAmount = 5000L;
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 45000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 45000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID, pointAmount)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort).refundOrderPoints(BUYER_ID, pointAmount, 1L);
        }

        @Test
        @DisplayName("전체 취소 시 포인트 미사용 주문이면 포인트 환불이 호출되지 않는다")
        void shouldNotRefundPointsWhenNoPointUsed() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID, 0L)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort, never()).refundOrderPoints(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("부분 취소 시 포인트 환불이 호출되지 않는다")
        void shouldNotRefundPointsOnPartialCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID, 5000L)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort, never()).refundOrderPoints(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("포인트 환불 실패 시에도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenPointRefundFails() {
            Long pointAmount = 5000L;
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 45000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 45000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID, pointAmount)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            doThrow(new RuntimeException("포인트 환불 실패")).when(modifyUserPointPort)
                    .refundOrderPoints(anyLong(), anyLong(), anyLong());

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
            verify(changeOrderStatusUseCase).changeOrderStatus(any());
        }
    }

    private Payment createSuccessPayment(Long orderId, UUID orderKey, Long amount, String pgPaymentKey) {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(orderId)
                .orderKey(orderKey)
                .paymentAmount(amount)
                .build();
        Payment payment = Payment.from(state);
        payment.markAsSuccess(pgPaymentKey);
        return payment;
    }

    private PspPaymentEvent createCompleteEvent(String orderKey, String tno, Long amount) {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(1L)
                .orderKey(orderKey)
                .pgCompanyKey("NHN_KCP")
                .pgPaymentKey(tno)
                .poStatus(PaymentEventStatus.COMPLETE)
                .method("CARD")
                .amount(amount)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .paidAt(LocalDateTime.of(2026, 2, 10, 15, 30, 0))
                .build();
        return PspPaymentEvent.from(state);
    }

    private Order createOrder(Long orderId, Long buyerId) {
        return createOrder(orderId, buyerId, 0L);
    }

    private Order createOrder(Long orderId, Long buyerId, Long pointAmount) {
        OrderProductSnapshotState productState = OrderProductSnapshotState.builder()
                .pricePolicyId(100L)
                .quantity(2)
                .sellerId(10L)
                .unitAmount(25000L)
                .build();
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(50000L)
                .pointAmount(pointAmount)
                .orderProductStates(List.of(productState))
                .build();
        return Order.from(state);
    }

    private CancelPaymentCommand createFullCancelCommand(String orderKey) {
        return CancelPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(orderKey)
                .cancelType(CancelPaymentCommand.CancelType.FULL)
                .cancelReason("고객 요청")
                .build();
    }

    private CancelPaymentCommand createPartialCancelCommand(String orderKey, Long cancelAmount) {
        return CancelPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(orderKey)
                .cancelType(CancelPaymentCommand.CancelType.PARTIAL)
                .cancelAmount(cancelAmount)
                .cancelReason("부분 취소")
                .build();
    }

    private PaymentCancelVendorResult createSuccessVendorResult() {
        return PaymentCancelVendorResult.builder()
                .resCd("0000")
                .resMsg("취소 성공")
                .amount("50000")
                .rawResponse("{\"res_cd\":\"0000\"}")
                .build();
    }
}
