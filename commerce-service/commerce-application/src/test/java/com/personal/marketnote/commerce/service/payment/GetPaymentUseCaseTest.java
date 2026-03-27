package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.result.payment.GetPaymentResult;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPaymentUseCase 테스트")
class GetPaymentUseCaseTest {

    @InjectMocks
    private GetPaymentService getPaymentService;

    @Mock
    private FindOrderPort findOrderPort;

    @Mock
    private FindPaymentPort findPaymentPort;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    private static final Long BUYER_ID = 100L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("결제 조회 성공")
    class GetPaymentSuccessTest {

        @Test
        @DisplayName("PspPaymentEvent가 있으면 결제 정보와 이벤트 정보가 함께 반환된다")
        void shouldReturnPaymentWithEvent() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123");

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            GetPaymentResult result = getPaymentService.getPayment(BUYER_ID, ORDER_KEY_STR);

            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.orderKey()).isEqualTo(ORDER_KEY_STR);
            assertThat(result.paymentAmount()).isEqualTo(50000L);
            assertThat(result.successYn()).isTrue();
            assertThat(result.pgPaymentKey()).isEqualTo("tno_123");
            assertThat(result.pgCompanyKey()).isEqualTo("NHN_KCP");
            assertThat(result.method()).isEqualTo("CARD");
            assertThat(result.cardNumber()).isEqualTo("1234-****-****-5678");
            assertThat(result.approvalNumber()).isEqualTo("12345678");
            assertThat(result.installment()).isEqualTo((short) 0);
            assertThat(result.issueCompanyName()).isEqualTo("신한카드");
            assertThat(result.resultCode()).isEqualTo("0000");
        }

        @Test
        @DisplayName("PspPaymentEvent가 없으면 결제 기본 정보만 반환된다")
        void shouldReturnPaymentWithoutEvent() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());

            GetPaymentResult result = getPaymentService.getPayment(BUYER_ID, ORDER_KEY_STR);

            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.paymentAmount()).isEqualTo(50000L);
            assertThat(result.pgCompanyKey()).isNull();
            assertThat(result.method()).isNull();
            assertThat(result.cardNumber()).isNull();
            assertThat(result.approvalNumber()).isNull();
        }

        @Test
        @DisplayName("환불된 결제 정보가 정확히 반환된다")
        void shouldReturnRefundedPayment() {
            Payment payment = createSuccessPayment(1L, ORDER_KEY, 50000L, "tno_123");
            payment.markAsRefunded();
            PspPaymentEvent event = createCompleteEvent(ORDER_KEY_STR, "tno_123");

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            GetPaymentResult result = getPaymentService.getPayment(BUYER_ID, ORDER_KEY_STR);

            assertThat(result.refundedYn()).isTrue();
            assertThat(result.refundAmount()).isEqualTo(50000L);
        }
    }

    @Nested
    @DisplayName("주문 소유자 검증")
    class OrderOwnerVerificationTest {

        @Test
        @DisplayName("주문 소유자가 아닌 사용자가 결제 조회 시 UnauthorizedOrderAccessException이 발생한다")
        void shouldThrowWhenBuyerIsNotOrderOwner() {
            Long attackerBuyerId = 999L;
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.of(createOrder(1L, BUYER_ID)));

            assertThatThrownBy(() -> getPaymentService.getPayment(attackerBuyerId, ORDER_KEY_STR))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verify(findPspPaymentEventPort, never()).findByOrderKey(any());
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 OrderNotFoundException이 발생한다")
        void shouldThrowWhenOrderNotFound() {
            Payment payment = createPayment(1L, ORDER_KEY, 50000L);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getPaymentService.getPayment(BUYER_ID, ORDER_KEY_STR))
                    .isInstanceOf(com.personal.marketnote.commerce.exception.OrderNotFoundException.class);

            verify(findPspPaymentEventPort, never()).findByOrderKey(any());
        }
    }

    @Nested
    @DisplayName("결제 미존재")
    class PaymentNotFoundTest {

        @Test
        @DisplayName("orderKey에 해당하는 결제가 없으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getPaymentService.getPayment(BUYER_ID, ORDER_KEY_STR))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(findPspPaymentEventPort, never()).findByOrderKey(any());
        }
    }

    private Order createOrder(Long orderId, Long buyerId) {
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .build();
        return Order.from(state);
    }

    private Payment createPayment(Long orderId, UUID orderKey, Long amount) {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(orderId)
                .orderKey(orderKey)
                .paymentAmount(amount)
                .build();
        return Payment.from(state);
    }

    private Payment createSuccessPayment(Long orderId, UUID orderKey, Long amount, String pgPaymentKey) {
        Payment payment = createPayment(orderId, orderKey, amount);
        payment.markAsSuccess(pgPaymentKey);
        return payment;
    }

    private PspPaymentEvent createCompleteEvent(String orderKey, String tno) {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(1L)
                .orderKey(orderKey)
                .pgCompanyKey("NHN_KCP")
                .pgPaymentKey(tno)
                .poStatus(PaymentEventStatus.COMPLETE)
                .method("CARD")
                .amount(50000L)
                .cardNumber("1234-****-****-5678")
                .approvalNumber("12345678")
                .installment((short) 0)
                .issueCompanyCode("CCLG")
                .issueCompanyName("신한카드")
                .resultCode("0000")
                .resultMessage("승인 성공")
                .paidAt(LocalDateTime.of(2026, 2, 10, 15, 30, 0))
                .build();
        return PspPaymentEvent.from(state);
    }
}
