package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.domain.payment.PaymentEventStatus;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.PaymentApprovalException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
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
    private SavePspPaymentEventPort savePspPaymentEventPort;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    @Mock
    private UpdatePspPaymentEventPort updatePspPaymentEventPort;

    @Mock
    private PaymentVendorPort paymentVendorPort;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

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

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.getVendorSiteCd()).thenReturn("T0000");
            when(savePspPaymentEventPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID, 99000L, 0L, 0L)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.getVendorSiteCd()).thenReturn("T0000");
            when(savePspPaymentEventPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.getVendorSiteCd()).thenReturn("T0000");
            when(savePspPaymentEventPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.getVendorSiteCd()).thenReturn("T0000");
            when(savePspPaymentEventPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("주문 금액과 결제 금액이 일치하지 않습니다");

            verify(paymentVendorPort, never()).approvePayment(any());
        }

        @Test
        @DisplayName("쿠폰/포인트 할인 적용 후 금액이 일치하면 승인이 진행된다")
        void shouldProceedWhenDiscountedAmountMatches() {
            Payment payment = createPayment(1L, ORDER_KEY, 40000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, BUYER_ID, 50000L, 5000L, 5000L);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_disc", "40000");

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.getVendorSiteCd()).thenReturn("T0000");
            when(savePspPaymentEventPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("주문 금액과 결제 금액이 일치하지 않습니다");

            verify(paymentVendorPort, never()).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("PspPaymentEvent 상태 전이 검증")
    class EventStatusTransitionTest {

        @Test
        @DisplayName("기존 이벤트가 없으면 새로 생성하고 save가 호출된다")
        void shouldCreateNewEventWhenNotExists() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_789", "50000");

            java.util.concurrent.atomic.AtomicReference<PaymentEventStatus> statusAtSave = new java.util.concurrent.atomic.AtomicReference<>();
            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.getVendorSiteCd()).thenReturn("T0000");
            when(savePspPaymentEventPort.save(any())).thenAnswer(invocation -> {
                PspPaymentEvent e = invocation.getArgument(0);
                statusAtSave.set(e.getPoStatus());
                return e;
            });
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            approvePaymentService.approve(command);

            verify(savePspPaymentEventPort).save(any());
            assertThat(statusAtSave.get()).isEqualTo(PaymentEventStatus.EXECUTING);
        }

        @Test
        @DisplayName("승인 성공 시 이벤트가 COMPLETE 상태로 업데이트된다")
        void shouldUpdateEventToCompleteOnSuccess() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_999", "50000");

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.getVendorSiteCd()).thenReturn("T0000");
            when(savePspPaymentEventPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            approvePaymentService.approve(command);

            verify(updatePspPaymentEventPort).update(argThat(e ->
                    e.getPoStatus() == PaymentEventStatus.COMPLETE
                            && "tno_999".equals(e.getPgPaymentKey())
            ));
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
