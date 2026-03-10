package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovePaymentUseCase 테스트")
class ApprovePaymentUseCaseTest {

    @InjectMocks
    private ApprovePaymentService approvePaymentService;

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

    private static final Long BUYER_ID = 100L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("결제 승인 성공")
    class ApproveSuccessTest {

        @Test
        @DisplayName("KCP 승인 성공 시 Payment가 성공 상태로 변경되고 주문이 PAID가 된다")
        void shouldApprovePaymentSuccessfully() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_123", "50000");
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            ApprovePaymentResult result = approvePaymentService.approve(command);

            assertThat(result.pgPaymentKey()).isEqualTo("tno_123");
            assertThat(result.resultCode()).isEqualTo("0000");
            assertThat(result.orderId()).isEqualTo(1L);

            verify(updatePaymentPort).update(argThat(p -> p.getSuccessYn() && "tno_123".equals(p.getPgPaymentKey())));
            verify(changeOrderStatusUseCase).changeOrderStatus(argThat(c -> c.orderStatus() == OrderStatus.PAID));
        }

        @Test
        @DisplayName("서버 DB의 금액이 KCP 승인 요청에 사용된다")
        void shouldUseServerAmountForApproval() {
            Payment payment = createPayment(1L, ORDER_KEY, 99000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_456", "99000");
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, 99000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID, 99000L, 0L, 0L)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            approvePaymentService.approve(command);

            verify(paymentVendorPort).approvePayment(argThat(c -> "99000".equals(c.ordrMony())));
        }
    }

    @Nested
    @DisplayName("결제 승인 실패")
    class ApproveFailureTest {

        @Test
        @DisplayName("KCP 승인 응답 res_cd가 0000이 아닌 경우 Payment가 실패 상태로 변경된다")
        void shouldMarkFailedWhenKcpReturnsError() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = PaymentApprovalVendorResult.builder()
                    .resCd("8001")
                    .resMsg("카드 인증 실패")
                    .rawResponse("{}")
                    .build();
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentApprovalException.class);

            verify(updatePaymentPort).update(argThat(p -> Boolean.FALSE.equals(p.getSuccessYn())));
            verify(changeOrderStatusUseCase).changeOrderStatus(argThat(c -> c.orderStatus() == OrderStatus.FAILED));
        }

        @Test
        @DisplayName("KCP 통신 중 예외 발생 시 결제 실패 처리된다")
        void shouldHandleVendorCommunicationException() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
            when(paymentVendorPort.approvePayment(any())).thenThrow(new RuntimeException("Connection timeout"));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentApprovalException.class);

            verify(updatePaymentPort).update(argThat(p -> Boolean.FALSE.equals(p.getSuccessYn())));
        }
    }

    @Nested
    @DisplayName("결제 미존재")
    class PaymentNotFoundTest {

        @Test
        @DisplayName("orderKey에 해당하는 결제가 없으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("거래 등록 미존재")
    class PaymentEventNotFoundTest {

        @Test
        @DisplayName("거래 등록(Ready)이 선행되지 않으면 PaymentEventNotFoundException이 발생한다")
        void shouldThrowWhenPaymentEventNotFound() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentEventNotFoundException.class)
                    .hasMessageContaining("거래 등록 정보를 찾을 수 없습니다");

            verify(paymentVendorPort, never()).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("주문 소유자 검증")
    class OrderOwnerVerificationTest {

        @Test
        @DisplayName("주문 소유자가 아닌 사용자가 결제 승인 시 UnauthorizedOrderAccessException이 발생한다")
        void shouldThrowWhenBuyerIsNotOrderOwner() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            Long attackerBuyerId = 999L;
            ApprovePaymentCommand command = ApprovePaymentCommand.builder()
                    .buyerId(attackerBuyerId)
                    .orderKey(ORDER_KEY_STR)
                    .encData("enc_data_test")
                    .encInfo("enc_info_test")
                    .payType("PACA")
                    .build();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 OrderNotFoundException이 발생한다")
        void shouldThrowWhenOrderNotFound() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(com.personal.marketnote.commerce.exception.OrderNotFoundException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("결제 금액 검증")
    class PaymentAmountVerificationTest {

        @Test
        @DisplayName("주문 금액과 결제 금액이 불일치하면 예외가 발생한다")
        void shouldThrowWhenAmountMismatch() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, BUYER_ID, 60000L, 0L, 0L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentAmountMismatchException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }

        @Test
        @DisplayName("쿠폰/포인트 할인 적용 후 금액이 일치하면 승인이 진행된다")
        void shouldProceedWhenDiscountedAmountMatches() {
            Payment payment = createPayment(1L, ORDER_KEY, 40000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, BUYER_ID, 50000L, 5000L, 5000L);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_disc", "40000");
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, 40000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            ApprovePaymentResult result = approvePaymentService.approve(command);

            assertThat(result.pgPaymentKey()).isEqualTo("tno_disc");
            verify(paymentVendorPort).approvePayment(argThat(c -> "40000".equals(c.ordrMony())));
        }

        @Test
        @DisplayName("쿠폰/포인트 할인 적용 후 금액이 불일치하면 예외가 발생한다")
        void shouldThrowWhenDiscountedAmountMismatch() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, BUYER_ID, 60000L, 5000L, 3000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentAmountMismatchException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("PspPaymentEvent 상태 전이 검증")
    class EventStatusTransitionTest {

        @Test
        @DisplayName("거래 등록된 이벤트가 EXECUTING 상태로 전이 후 update가 호출된다")
        void shouldUpdateExistingEventToExecuting() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_789", "50000");
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            approvePaymentService.approve(command);

            verify(updatePspPaymentEventPort, times(2)).update(any());
        }

        @Test
        @DisplayName("승인 성공 시 이벤트가 COMPLETE 상태로 업데이트된다")
        void shouldUpdateEventToCompleteOnSuccess() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_999", "50000");
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            approvePaymentService.approve(command);

            verify(updatePspPaymentEventPort, atLeastOnce()).update(argThat(e ->
                    e.getPoStatus() == PaymentEventStatus.COMPLETE
                            && "tno_999".equals(e.getPgPaymentKey())
            ));
        }
    }

    @Nested
    @DisplayName("결제 승인 시 자동 분개")
    class LedgerEntryRecordingTest {

        private void setupSuccessScenario(Payment payment) {
            PspPaymentEvent readyEvent = createReadyEvent(1L, ORDER_KEY_STR, payment.getPaymentAmount());

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(readyEvent));
        }

        @Test
        @DisplayName("결제 승인 성공 시 recordPaymentApproval이 orderId와 결제금액으로 호출된다")
        void shouldRecordLedgerEntryOnPaymentApproval() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_ledger", "50000");
            setupSuccessScenario(payment);
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            approvePaymentService.approve(command);

            verify(recordLedgerEntryUseCase).recordPaymentApproval(1L, 50000L);
        }

        @Test
        @DisplayName("분개 기록 실패 시에도 결제 승인은 정상적으로 완료된다")
        void shouldCompletePaymentEvenWhenLedgerRecordingFails() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_fail", "50000");
            setupSuccessScenario(payment);
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            doThrow(new RuntimeException("분개 기록 실패"))
                    .when(recordLedgerEntryUseCase).recordPaymentApproval(anyLong(), anyLong());

            ApprovePaymentResult result = approvePaymentService.approve(command);

            assertThat(result.pgPaymentKey()).isEqualTo("tno_fail");
            verify(updatePaymentPort).update(argThat(p -> p.getSuccessYn()));
            verify(changeOrderStatusUseCase).changeOrderStatus(argThat(c -> c.orderStatus() == OrderStatus.PAID));
        }

        @Test
        @DisplayName("결제 실패 시 분개가 기록되지 않는다")
        void shouldNotRecordLedgerEntryOnPaymentFailure() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = PaymentApprovalVendorResult.builder()
                    .resCd("8001")
                    .resMsg("카드 인증 실패")
                    .rawResponse("{}")
                    .build();
            setupSuccessScenario(payment);
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentApprovalException.class);

            verify(recordLedgerEntryUseCase, never()).recordPaymentApproval(anyLong(), anyLong());
        }
    }

    private Payment createPayment(Long orderId, UUID orderKey, Long amount) {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(orderId)
                .orderKey(orderKey)
                .paymentAmount(amount)
                .build();
        return Payment.from(state);
    }

    private PspPaymentEvent createReadyEvent(Long id, String orderKey, Long amount) {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(id)
                .orderId(1L)
                .orderKey(orderKey)
                .pgCompanyKey("NHN_KCP")
                .pgShopKey("T0000")
                .poStatus(PaymentEventStatus.READY)
                .method("PACA")
                .amount(amount)
                .build();
        return PspPaymentEvent.from(state);
    }

    private ApprovePaymentCommand createApproveCommand(String orderKey) {
        return ApprovePaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(orderKey)
                .encData("enc_data_test")
                .encInfo("enc_info_test")
                .payType("PACA")
                .build();
    }

    private Order createOrder(Long orderId, Long buyerId) {
        return createOrder(orderId, buyerId, 50000L, 0L, 0L);
    }

    private Order createOrder(Long orderId, Long buyerId, Long totalAmount, Long couponAmount, Long pointAmount) {
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .totalAmount(totalAmount)
                .couponAmount(couponAmount)
                .pointAmount(pointAmount)
                .build();
        return Order.from(state);
    }

    private PaymentApprovalVendorResult createSuccessVendorResult(String tno, String amount) {
        return PaymentApprovalVendorResult.builder()
                .resCd("0000")
                .resMsg("승인 성공")
                .tno(tno)
                .amount(amount)
                .payMethod("PACA")
                .cardCd("CCLG")
                .cardName("신한카드")
                .cardNo("1234-****-****-5678")
                .appNo("12345678")
                .appTime("20260210153000")
                .quota("00")
                .partcancYn("Y")
                .rawResponse("{\"res_cd\":\"0000\"}")
                .build();
    }
}
