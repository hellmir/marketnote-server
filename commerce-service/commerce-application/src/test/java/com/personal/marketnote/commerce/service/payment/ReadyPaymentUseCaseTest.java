package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.DuplicatePaymentReadyException;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusForPaymentException;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorResult;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadyPaymentUseCase 테스트")
class ReadyPaymentUseCaseTest {

    @InjectMocks
    private ReadyPaymentService readyPaymentService;

    @Mock
    private FindOrderPort findOrderPort;

    @Mock
    private FindPaymentPort findPaymentPort;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    @Mock
    private PaymentVendorPort paymentVendorPort;

    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("거래 등록 성공")
    class ReadySuccessTest {

        @Test
        @DisplayName("결제 준비 성공 시 KCP 거래등록 결과가 반환된다")
        void shouldReturnTradeRegisterResult() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            TradeRegisterVendorResult vendorResult = createVendorResult();
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.registerTrade(any())).thenReturn(vendorResult);

            ReadyPaymentResult result = readyPaymentService.ready(command);

            assertThat(result.orderKey()).isEqualTo(ORDER_KEY_STR);
            assertThat(result.approvalKey()).isEqualTo("approval_key_123");
            assertThat(result.payUrl()).isEqualTo("https://pay.kcp.co.kr/test");
            assertThat(result.traceNo()).isEqualTo("trace_001");
        }

        @Test
        @DisplayName("서버 DB의 결제 금액이 KCP 거래등록에 사용된다")
        void shouldUseServerPaymentAmountForTrade() {
            Payment payment = createPayment(1L, ORDER_KEY, 77000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            TradeRegisterVendorResult vendorResult = createVendorResult();
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.registerTrade(any())).thenReturn(vendorResult);

            readyPaymentService.ready(command);

            verify(paymentVendorPort).registerTrade(argThat(c ->
                    "77000".equals(c.goodMny()) && ORDER_KEY_STR.equals(c.orderKey())
            ));
        }

        @Test
        @DisplayName("command의 payMethod와 goodName이 KCP 요청에 전달된다")
        void shouldPassPayMethodAndGoodName() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = ReadyPaymentCommand.builder()
                    .orderKey(ORDER_KEY_STR)
                    .payMethod("CARD")
                    .goodName("테스트 상품")
                    .build();
            TradeRegisterVendorResult vendorResult = createVendorResult();
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.registerTrade(any())).thenReturn(vendorResult);

            readyPaymentService.ready(command);

            verify(paymentVendorPort).registerTrade(argThat(c ->
                    "CARD".equals(c.payMethod()) && "테스트 상품".equals(c.goodName())
            ));
        }
    }

    @Nested
    @DisplayName("결제 미존재")
    class PaymentNotFoundTest {

        @Test
        @DisplayName("orderKey에 해당하는 결제가 없으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(paymentVendorPort, never()).registerTrade(any());
        }
    }

    @Nested
    @DisplayName("주문 미존재")
    class OrderNotFoundTest {

        @Test
        @DisplayName("orderId에 해당하는 주문이 없으면 OrderNotFoundException이 발생한다")
        void shouldThrowWhenOrderNotFound() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(paymentVendorPort, never()).registerTrade(any());
        }
    }

    @Nested
    @DisplayName("주문 상태 검증 실패")
    class InvalidOrderStatusTest {

        @Test
        @DisplayName("주문이 CANCELLED 상태이면 InvalidOrderStatusForPaymentException이 발생한다")
        void shouldThrowWhenOrderIsCancelled() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, OrderStatus.CANCELLED);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(InvalidOrderStatusForPaymentException.class)
                    .hasMessageContaining("주문 취소");

            verify(paymentVendorPort, never()).registerTrade(any());
        }

        @Test
        @DisplayName("주문이 PAID 상태이면 InvalidOrderStatusForPaymentException이 발생한다")
        void shouldThrowWhenOrderIsPaid() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, OrderStatus.PAID);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(InvalidOrderStatusForPaymentException.class)
                    .hasMessageContaining("결제 완료");

            verify(paymentVendorPort, never()).registerTrade(any());
        }

        @Test
        @DisplayName("주문 상태 검증 실패 시 KCP 거래등록이 호출되지 않는다")
        void shouldNotCallVendorWhenOrderStatusInvalid() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, OrderStatus.PREPARING);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(InvalidOrderStatusForPaymentException.class);

            verify(paymentVendorPort, never()).registerTrade(any());
            verify(findPspPaymentEventPort, never()).findByOrderKey(any());
        }
    }

    @Nested
    @DisplayName("중복 거래 등록 방지")
    class DuplicatePaymentReadyTest {

        @Test
        @DisplayName("이미 READY 상태의 PspPaymentEvent가 존재하면 DuplicatePaymentReadyException이 발생한다")
        void shouldThrowWhenReadyEventExists() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent activeEvent = createPspPaymentEvent(PaymentEventStatus.READY);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(activeEvent));

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(DuplicatePaymentReadyException.class)
                    .hasMessageContaining(ORDER_KEY_STR);

            verify(paymentVendorPort, never()).registerTrade(any());
        }

        @Test
        @DisplayName("이미 EXECUTING 상태의 PspPaymentEvent가 존재하면 DuplicatePaymentReadyException이 발생한다")
        void shouldThrowWhenExecutingEventExists() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent activeEvent = createPspPaymentEvent(PaymentEventStatus.EXECUTING);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(activeEvent));

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(DuplicatePaymentReadyException.class);

            verify(paymentVendorPort, never()).registerTrade(any());
        }

        @Test
        @DisplayName("COMPLETE 상태의 PspPaymentEvent가 존재해도 거래 등록은 정상 진행된다")
        void shouldProceedWhenCompleteEventExists() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            TradeRegisterVendorResult vendorResult = createVendorResult();
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent completedEvent = createPspPaymentEvent(PaymentEventStatus.COMPLETE);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(completedEvent));
            when(paymentVendorPort.registerTrade(any())).thenReturn(vendorResult);

            ReadyPaymentResult result = readyPaymentService.ready(command);

            assertThat(result.orderKey()).isEqualTo(ORDER_KEY_STR);
            verify(paymentVendorPort).registerTrade(any());
        }

        @Test
        @DisplayName("CANCELLED 상태의 PspPaymentEvent가 존재해도 거래 등록은 정상 진행된다")
        void shouldProceedWhenCancelledEventExists() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            TradeRegisterVendorResult vendorResult = createVendorResult();
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent cancelledEvent = createPspPaymentEvent(PaymentEventStatus.CANCELLED);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(cancelledEvent));
            when(paymentVendorPort.registerTrade(any())).thenReturn(vendorResult);

            ReadyPaymentResult result = readyPaymentService.ready(command);

            assertThat(result.orderKey()).isEqualTo(ORDER_KEY_STR);
            verify(paymentVendorPort).registerTrade(any());
        }
    }

    @Nested
    @DisplayName("KCP 통신 실패")
    class VendorFailureTest {

        @Test
        @DisplayName("KCP 거래등록 중 예외 발생 시 그대로 전파된다")
        void shouldPropagateVendorException() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);
            ReadyPaymentCommand command = createReadyCommand(ORDER_KEY_STR);
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(paymentVendorPort.registerTrade(any())).thenThrow(new RuntimeException("KCP 거래등록 실패"));

            assertThatThrownBy(() -> readyPaymentService.ready(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("KCP 거래등록 실패");
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

    private Order createOrder(Long orderId, OrderStatus orderStatus) {
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(ORDER_KEY)
                .orderNumber("ORD-TEST-001")
                .orderStatus(orderStatus)
                .totalAmount(50000L)
                .build();
        return Order.from(state);
    }

    private PspPaymentEvent createPspPaymentEvent(PaymentEventStatus status) {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(1L)
                .orderKey(ORDER_KEY_STR)
                .poStatus(status)
                .build();
        return PspPaymentEvent.from(state);
    }

    private ReadyPaymentCommand createReadyCommand(String orderKey) {
        return ReadyPaymentCommand.builder()
                .orderKey(orderKey)
                .payMethod("CARD")
                .goodName("테스트 상품")
                .build();
    }

    private TradeRegisterVendorResult createVendorResult() {
        return TradeRegisterVendorResult.builder()
                .resCd("0000")
                .resMsg("성공")
                .approvalKey("approval_key_123")
                .payUrl("https://pay.kcp.co.kr/test")
                .traceNo("trace_001")
                .rawResponse("{}")
                .build();
    }
}
