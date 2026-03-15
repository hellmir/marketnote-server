package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.commerce.exception.InvalidCancelProductException;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

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

    @Mock
    private SaveRefundPort saveRefundPort;

    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

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
        @DisplayName("부분 취소 성공 시 cancelId 기반 멱등성 키로 역분개가 기록된다")
        void shouldRecordReverseLedgerEntryOnPartialCancelWithCancelId() {
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
                    eq(1L), eq(20000L), argThat(key -> {
                        String prefix = "PAYMENT_PARTIAL_REFUND:1:";
                        if (!key.startsWith(prefix)) {
                            return false;
                        }
                        String cancelId = key.substring(prefix.length());
                        try {
                            UUID.fromString(cancelId);
                            return true;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    })
            );
        }

        @Test
        @DisplayName("이미 부분환불된 상태에서 추가 부분 취소 시에도 cancelId 기반 멱등성 키가 사용된다")
        void shouldUseCancelIdBasedKeyRegardlessOfPriorRefundHistory() {
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
                    eq(1L), eq(20000L), argThat(key ->
                            key.startsWith("PAYMENT_PARTIAL_REFUND:1:")
                                    && !key.contains("10000")
                                    && !key.contains("20000")
                    )
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

            verify(restoreProductInventoryUseCase).restore(anyList(), anyLong(), anyString());
        }

        @Test
        @DisplayName("부분 취소 시 취소 대상 상품 목록이 없으면 재고 복구가 호출되지 않는다")
        void shouldNotRestoreInventoryOnPartialCancelWithoutCancelProducts() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyLong(), anyString());
        }

        @Test
        @DisplayName("부분 취소 시 취소 대상 상품 목록이 빈 리스트이면 재고 복구가 호출되지 않는다")
        void shouldNotRestoreInventoryOnPartialCancelWithEmptyCancelProducts() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommandWithProducts(ORDER_KEY_STR, 20000L, List.of());
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyLong(), anyString());
        }

        @Test
        @DisplayName("부분 취소 시 취소 대상 상품 목록이 있으면 해당 상품의 재고 복구가 호출된다")
        void shouldRestoreInventoryOnPartialCancelWithCancelProducts() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            List<CancelPaymentCommand.CancelProductItem> cancelProducts = List.of(
                    new CancelPaymentCommand.CancelProductItem(100L, 1)
            );
            CancelPaymentCommand command = createPartialCancelCommandWithProducts(ORDER_KEY_STR, 25000L, cancelProducts);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(restoreProductInventoryUseCase).restore(argThat(products ->
                    products.size() == 1
                            && products.get(0).getPricePolicyId().equals(100L)
                            && products.get(0).getQuantity().equals(1)
            ), eq(1L), eq("주문 부분 취소에 의한 재고 복구"));
        }

        @Test
        @DisplayName("부분 취소 시 취소 대상 상품의 pricePolicyId와 수량이 정확히 전달된다")
        void shouldPassCorrectPricePolicyIdAndQuantityOnPartialCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            List<CancelPaymentCommand.CancelProductItem> cancelProducts = List.of(
                    new CancelPaymentCommand.CancelProductItem(100L, 1),
                    new CancelPaymentCommand.CancelProductItem(200L, 2)
            );
            Order order = createOrderWithMultipleProducts(1L, BUYER_ID, List.of(100L, 200L), List.of(2, 3));
            CancelPaymentCommand command = createPartialCancelCommandWithProducts(ORDER_KEY_STR, 30000L, cancelProducts);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(restoreProductInventoryUseCase).restore(argThat(products ->
                    products.size() == 2
                            && products.get(0).getPricePolicyId().equals(100L)
                            && products.get(0).getQuantity().equals(1)
                            && products.get(1).getPricePolicyId().equals(200L)
                            && products.get(1).getQuantity().equals(2)
            ), eq(1L), eq("주문 부분 취소에 의한 재고 복구"));
        }

        @Test
        @DisplayName("부분 취소 재고 복구 실패 시에도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenPartialInventoryRestoreFails() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            List<CancelPaymentCommand.CancelProductItem> cancelProducts = List.of(
                    new CancelPaymentCommand.CancelProductItem(100L, 1)
            );
            CancelPaymentCommand command = createPartialCancelCommandWithProducts(ORDER_KEY_STR, 25000L, cancelProducts);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            doThrow(new RuntimeException("재고 복구 실패")).when(restoreProductInventoryUseCase)
                    .restore(anyList(), anyLong(), anyString());

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
        }

        @Test
        @DisplayName("전액 취소 시 cancelProducts가 있어도 기존 전체 재고 복구가 수행된다")
        void shouldUseFullRestoreOnFullCancelEvenWithCancelProducts() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            List<CancelPaymentCommand.CancelProductItem> cancelProducts = List.of(
                    new CancelPaymentCommand.CancelProductItem(100L, 1)
            );
            CancelPaymentCommand command = CancelPaymentCommand.builder()
                    .buyerId(BUYER_ID)
                    .orderKey(ORDER_KEY_STR)
                    .cancelType(CancelPaymentCommand.CancelType.FULL)
                    .cancelReason("고객 요청")
                    .cancelProducts(cancelProducts)
                    .build();
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            // 전액 취소는 기존 restoreInventory(order)가 호출되므로 전체 주문 상품 목록으로 복구
            verify(restoreProductInventoryUseCase).restore(anyList(), eq(1L), eq("주문 전액 취소에 의한 재고 복구"));
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
                    .restore(anyList(), anyLong(), anyString());

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
            verify(changeOrderStatusUseCase).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("부분 취소 상품 검증")
    class PartialCancelProductValidationTest {

        @Test
        @DisplayName("부분 취소 시 주문에 존재하지 않는 상품으로 요청하면 InvalidCancelProductException이 발생한다")
        void shouldThrowWhenCancelProductNotInOrder() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            List<CancelPaymentCommand.CancelProductItem> cancelProducts = List.of(
                    new CancelPaymentCommand.CancelProductItem(999L, 1)
            );
            CancelPaymentCommand command = createPartialCancelCommandWithProducts(ORDER_KEY_STR, 25000L, cancelProducts);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(InvalidCancelProductException.class)
                    .hasMessageContaining("주문에 존재하지 않는 상품");

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("부분 취소 시 중복된 pricePolicyId가 포함되면 InvalidCancelProductException이 발생한다")
        void shouldThrowWhenDuplicatePricePolicyIdInCancelProducts() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            List<CancelPaymentCommand.CancelProductItem> cancelProducts = List.of(
                    new CancelPaymentCommand.CancelProductItem(100L, 1),
                    new CancelPaymentCommand.CancelProductItem(100L, 1)
            );
            CancelPaymentCommand command = createPartialCancelCommandWithProducts(ORDER_KEY_STR, 25000L, cancelProducts);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(InvalidCancelProductException.class)
                    .hasMessageContaining("중복된 상품");

            verify(paymentVendorPort, never()).cancelPayment(any());
        }

        @Test
        @DisplayName("부분 취소 시 취소 수량이 주문 수량을 초과하면 InvalidCancelProductException이 발생한다")
        void shouldThrowWhenCancelQuantityExceedsOrderQuantity() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            List<CancelPaymentCommand.CancelProductItem> cancelProducts = List.of(
                    new CancelPaymentCommand.CancelProductItem(100L, 5)
            );
            CancelPaymentCommand command = createPartialCancelCommandWithProducts(ORDER_KEY_STR, 25000L, cancelProducts);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(InvalidCancelProductException.class)
                    .hasMessageContaining("취소 수량이 주문 수량을 초과");

            verify(paymentVendorPort, never()).cancelPayment(any());
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

    @Nested
    @DisplayName("환불 상세 기록 검증")
    class RefundRecordTest {

        private Payment createPaymentWithId(Long paymentId, Long orderId, UUID orderKey, Long amount, String pgPaymentKey) {
            PaymentSnapshotState state = PaymentSnapshotState.builder()
                    .id(paymentId)
                    .orderId(orderId)
                    .orderKey(orderKey)
                    .pgPaymentKey(pgPaymentKey)
                    .paymentAmount(amount)
                    .successYn(true)
                    .refundedYn(false)
                    .refundAmount(0L)
                    .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                    .modifiedAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                    .build();
            return Payment.from(state);
        }

        @Test
        @DisplayName("전체 취소 성공 시 FULL_REFUND 환불 기록이 저장된다")
        void shouldSaveFullRefundRecordOnFullCancel() {
            Payment payment = createPaymentWithId(5L, 1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(saveRefundPort).save(argThat(refund ->
                    refund.getRefundType() == RefundType.FULL_REFUND
                            && refund.getRefundAmount().equals(50000L)
                            && "SYSTEM".equals(refund.getProcessedBy())
            ));
        }

        @Test
        @DisplayName("부분 취소 성공 시 PARTIAL_REFUND 환불 기록이 저장된다")
        void shouldSavePartialRefundRecordOnPartialCancel() {
            Payment payment = createPaymentWithId(5L, 1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(saveRefundPort).save(argThat(refund ->
                    refund.getRefundType() == RefundType.PARTIAL_REFUND
                            && refund.getRefundAmount().equals(20000L)
            ));
        }

        @Test
        @DisplayName("환불 기록에 PG 응답 정보가 포함된다")
        void shouldIncludePgResponseInRefundRecord() {
            Payment payment = createPaymentWithId(5L, 1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(saveRefundPort).save(argThat(refund ->
                    refund.getPgRawResponse() != null
                            && refund.getPgRefundKey() != null
            ));
        }

        @Test
        @DisplayName("환불 기록 저장 실패 시에도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenRefundRecordSaveFails() {
            Payment payment = createPaymentWithId(5L, 1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            doThrow(new RuntimeException("환불 기록 저장 실패")).when(saveRefundPort).save(any(Refund.class));

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
            verify(changeOrderStatusUseCase).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("공유 적립 예정 포인트 회수 검증")
    class SharedPurchasePointRevokeTest {

        @Test
        @DisplayName("전체 취소 시 공유자가 있으면 공유 적립 예정 포인트 회수가 호출된다")
        void shouldRevokeSharedPendingPointsOnFullCancelWithSharers() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
            Order order = createOrderWithSharers(1L, BUYER_ID, 0L, List.of(200L, 300L));

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort).revokePendingSharedPurchasePoints(
                    eq(List.of(200L, 300L)), eq(1L)
            );
        }

        @Test
        @DisplayName("전체 취소 시 공유자가 없으면 공유 적립 예정 포인트 회수가 호출되지 않는다")
        void shouldNotRevokeSharedPendingPointsWhenNoSharers() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort, never()).revokePendingSharedPurchasePoints(anyList(), anyLong());
        }

        @Test
        @DisplayName("부분 취소 시 공유 적립 예정 포인트 회수가 호출되지 않는다")
        void shouldNotRevokeSharedPendingPointsOnPartialCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
            Order order = createOrderWithSharers(1L, BUYER_ID, 0L, List.of(200L));

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort, never()).revokePendingSharedPurchasePoints(anyList(), anyLong());
        }

        @Test
        @DisplayName("공유 적립 예정 포인트 회수 실패 시에도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenSharedPointRevokeFails() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
            Order order = createOrderWithSharers(1L, BUYER_ID, 0L, List.of(200L));

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            doThrow(new RuntimeException("공유 포인트 회수 실패")).when(modifyUserPointPort)
                    .revokePendingSharedPurchasePoints(anyList(), anyLong());

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
            verify(changeOrderStatusUseCase).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("부분 취소 적립 예정 포인트 비례 차감 검증")
    class PartialCancelPendingPointTest {

        @Test
        @DisplayName("부분 취소 시 취소 금액에 비례하여 적립 예정 포인트가 차감된다")
        void shouldReducePendingPointsProportionally() {
            // 결제금액 50000, 부분취소 20000 (40%), 적립포인트 1000 → 비례 400
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
            Order order = createOrderWithAccumulatedPoint(1L, BUYER_ID, 50000L, 100L, 2, 500L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, createProductInfoWithPoint(500L)));

            cancelPaymentService.cancel(command);

            // 총 적립포인트 = 500 * 2 = 1000, 비례 = round(20000/50000 * 1000) = 400
            verify(modifyUserPointPort).reducePartialPendingPoints(BUYER_ID, 400L, 1L);
        }

        @Test
        @DisplayName("부분 취소 시 적립 포인트가 0이면 차감이 호출되지 않는다")
        void shouldNotReduceWhenAccumulatedPointIsZero() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, createProductInfoWithPoint(0L)));

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort, never()).reducePartialPendingPoints(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("부분 취소 시 상품 정보 조회 실패해도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenProductInfoFails() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenThrow(new RuntimeException("상품 서비스 장애"));

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
        }

        @Test
        @DisplayName("부분 취소 시 포인트 차감 실패해도 결제 취소는 정상 완료된다")
        void shouldCompleteCancelEvenWhenPendingPointReduceFails() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
            Order order = createOrderWithAccumulatedPoint(1L, BUYER_ID, 50000L, 100L, 2, 500L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, createProductInfoWithPoint(500L)));
            doThrow(new RuntimeException("포인트 차감 실패")).when(modifyUserPointPort)
                    .reducePartialPendingPoints(anyLong(), anyLong(), anyLong());

            cancelPaymentService.cancel(command);

            verify(updatePaymentPort).update(any());
            verify(updatePspPaymentEventPort).update(any());
        }

        @Test
        @DisplayName("부분 취소 시 상품 정보에 적립 포인트가 null이면 차감이 호출되지 않는다")
        void shouldNotReduceWhenAccumulatedPointIsNull() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 20000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, createProductInfoWithPoint(null)));

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort, never()).reducePartialPendingPoints(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("여러 상품의 적립 포인트를 합산하여 비례 차감한다")
        void shouldSumAccumulatedPointsFromMultipleProducts() {
            // 상품1: 300포인트 * 1개 = 300, 상품2: 200포인트 * 2개 = 400, 합계 = 700
            // 결제금액 70000, 부분취소 35000 (50%) → 비례 350
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 70000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 70000L);
            CancelPaymentCommand command = createPartialCancelCommand(ORDER_KEY_STR, 35000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            List<OrderProductSnapshotState> productStates = List.of(
                    OrderProductSnapshotState.builder()
                            .pricePolicyId(100L).quantity(1).sellerId(10L).unitAmount(30000L).build(),
                    OrderProductSnapshotState.builder()
                            .pricePolicyId(200L).quantity(2).sellerId(10L).unitAmount(20000L).build()
            );
            OrderSnapshotState orderState = OrderSnapshotState.builder()
                    .id(1L).buyerId(BUYER_ID).orderKey(ORDER_KEY).orderStatus(OrderStatus.PAID)
                    .totalAmount(70000L).pointAmount(0L).orderProductStates(productStates).build();
            Order order = Order.from(orderState);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L, 200L)))
                    .thenReturn(Map.of(
                            100L, createProductInfoWithPoint(300L),
                            200L, createProductInfoWithPoint(200L)
                    ));

            cancelPaymentService.cancel(command);

            // 합계 700, 비례 = round(35000/70000 * 700) = 350
            verify(modifyUserPointPort).reducePartialPendingPoints(BUYER_ID, 350L, 1L);
        }

        @Test
        @DisplayName("전체 취소 시에는 비례 차감이 아닌 기존 전체 회수가 호출된다")
        void shouldUseFullRevokeOnFullCancel() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123", 50000L);
            CancelPaymentCommand command = createFullCancelCommand(ORDER_KEY_STR);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(modifyUserPointPort).revokePendingPoints(BUYER_ID, 1L);
            verify(modifyUserPointPort, never()).reducePartialPendingPoints(anyLong(), anyLong(), anyLong());
        }
    }

    private Order createOrderWithAccumulatedPoint(Long orderId, Long buyerId, Long totalAmount,
                                                  Long pricePolicyId, int quantity, Long unitAmount) {
        OrderProductSnapshotState productState = OrderProductSnapshotState.builder()
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .sellerId(10L)
                .unitAmount(unitAmount)
                .build();
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(totalAmount)
                .pointAmount(0L)
                .orderProductStates(List.of(productState))
                .build();
        return Order.from(state);
    }

    private ProductInfoResult createProductInfoWithPoint(Long accumulatedPoint) {
        return new ProductInfoResult(1L, 10L, "테스트 상품", "테스트 브랜드", 25000L, null, accumulatedPoint, List.of());
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

    private Order createOrderWithSharers(Long orderId, Long buyerId, Long pointAmount, List<Long> sharerIds) {
        List<OrderProductSnapshotState> productStates = new ArrayList<>();
        for (int i = 0; i < sharerIds.size(); i++) {
            productStates.add(OrderProductSnapshotState.builder()
                    .pricePolicyId(100L + i)
                    .quantity(1)
                    .sellerId(10L)
                    .unitAmount(25000L)
                    .sharerId(sharerIds.get(i))
                    .build());
        }
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(50000L)
                .pointAmount(pointAmount)
                .orderProductStates(productStates)
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

    private CancelPaymentCommand createPartialCancelCommandWithProducts(
            String orderKey, Long cancelAmount, List<CancelPaymentCommand.CancelProductItem> cancelProducts
    ) {
        return CancelPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(orderKey)
                .cancelType(CancelPaymentCommand.CancelType.PARTIAL)
                .cancelAmount(cancelAmount)
                .cancelReason("부분 취소")
                .cancelProducts(cancelProducts)
                .build();
    }

    private Order createOrderWithMultipleProducts(Long orderId, Long buyerId,
                                                  List<Long> pricePolicyIds, List<Integer> quantities) {
        List<OrderProductSnapshotState> productStates = new ArrayList<>();
        for (int i = 0; i < pricePolicyIds.size(); i++) {
            productStates.add(OrderProductSnapshotState.builder()
                    .pricePolicyId(pricePolicyIds.get(i))
                    .quantity(quantities.get(i))
                    .sellerId(10L)
                    .unitAmount(25000L)
                    .build());
        }
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(50000L)
                .pointAmount(0L)
                .orderProductStates(productStates)
                .build();
        return Order.from(state);
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
