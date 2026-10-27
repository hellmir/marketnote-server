package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.order.ShippingAddressCreateState;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.service.payment.idempotency.PaymentCancelIdempotencyHelper;
import com.personal.marketnote.commerce.service.payment.idempotency.PaymentCancelIdempotencyResolution;
import com.personal.marketnote.commerce.service.payment.idempotency.PaymentCancelResultSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CancelPaymentService 멱등성 처리 테스트 (#4392)
 *
 * <p>IdempotencyHelper 를 통한 멱등 키 조회/상태 전이(findOrCreate, markCompleted, markFailed)
 * 오케스트레이션을 검증한다. 일반 취소 플로우/원자성 검증은 {@link CancelPaymentUseCaseTest} 에서 수행한다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPaymentUseCase 멱등성 처리 테스트 (#4392)")
class CancelPaymentIdempotencyUseCaseTest {

    @InjectMocks
    private CancelPaymentService cancelPaymentService;

    @Mock
    private PaymentCancelTransactionHelper txHelper;

    @Mock
    private PaymentVendorPort paymentVendorPort;

    @Mock
    private PaymentCancelIdempotencyHelper idempotencyHelper;

    @Mock
    private PaymentCancelResultSerializer resultSerializer;

    private static final Long BUYER_ID = 100L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();
    private static final String IDEMPOTENCY_KEY = "11111111-2222-4333-8444-555555555555";

    @BeforeEach
    void setUpIdempotency() {
        org.mockito.Mockito.lenient().when(
                idempotencyHelper.findOrCreate(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyLong())
        ).thenReturn(PaymentCancelIdempotencyResolution.newlyCreated());
        org.mockito.Mockito.lenient().when(resultSerializer.serialize(org.mockito.ArgumentMatchers.any()))
                .thenReturn("{}");
    }

    @Test
    @DisplayName("findOrCreate 가 CACHED 를 반환하면 비즈니스 플로우는 실행되지 않는다")
    void shouldSkipFlowWhenCached() {
        CancelPaymentCommand command = createFullCancelCommand();
        org.mockito.Mockito.reset(idempotencyHelper);
        when(idempotencyHelper.findOrCreate(IDEMPOTENCY_KEY, ORDER_KEY_STR, BUYER_ID))
                .thenReturn(PaymentCancelIdempotencyResolution.cached("{\"ok\":true}"));

        cancelPaymentService.cancel(command);

        verify(txHelper, never()).prepareCancel(any());
        verify(paymentVendorPort, never()).cancelPayment(any());
        verify(idempotencyHelper, never()).markCompleted(any(), any());
        verify(idempotencyHelper, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("정상 플로우 완료 시 markCompleted 가 호출된다")
    void shouldMarkCompletedAfterSuccess() {
        CancelPaymentCommand command = createFullCancelCommand();
        PaymentCancelContext context = createContext(false, 50000L, 0L);
        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
        when(txHelper.prepareCancel(command)).thenReturn(context);
        when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

        cancelPaymentService.cancel(command);

        verify(idempotencyHelper).markCompleted(org.mockito.ArgumentMatchers.eq(IDEMPOTENCY_KEY),
                org.mockito.ArgumentMatchers.anyString());
        verify(idempotencyHelper, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("비즈니스 예외 발생 시 markFailed 가 호출되고 원본 예외가 재전파된다")
    void shouldMarkFailedWhenBusinessThrows() {
        CancelPaymentCommand command = createFullCancelCommand();
        when(txHelper.prepareCancel(command))
                .thenThrow(new com.personal.marketnote.commerce.exception.PaymentNotFoundException(ORDER_KEY_STR));

        assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                .isInstanceOf(com.personal.marketnote.commerce.exception.PaymentNotFoundException.class);

        verify(idempotencyHelper).markFailed(org.mockito.ArgumentMatchers.eq(IDEMPOTENCY_KEY),
                org.mockito.ArgumentMatchers.anyString());
        verify(idempotencyHelper, never()).markCompleted(any(), any());
    }

    @Test
    @DisplayName("비즈니스 완료 후 markCompleted 실패 시 markFailed 호출되지 않는다")
    void shouldNotMarkFailedWhenMarkCompletedFailsAfterBusinessSuccess() {
        CancelPaymentCommand command = createFullCancelCommand();
        PaymentCancelContext context = createContext(false, 50000L, 0L);
        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
        when(txHelper.prepareCancel(command)).thenReturn(context);
        when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
        when(resultSerializer.serialize(any()))
                .thenThrow(new RuntimeException("직렬화 실패"));

        assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("직렬화 실패");

        verify(idempotencyHelper, never()).markFailed(any(), any());
        verify(idempotencyHelper, never()).markCompleted(any(), any());
    }

    @Test
    @DisplayName("비즈니스 완료 후 markCompleted 자체가 예외를 던져도 markFailed 호출되지 않는다")
    void shouldNotMarkFailedWhenMarkCompletedThrowsAfterBusinessSuccess() {
        CancelPaymentCommand command = createFullCancelCommand();
        PaymentCancelContext context = createContext(false, 50000L, 0L);
        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
        when(txHelper.prepareCancel(command)).thenReturn(context);
        when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
        org.mockito.Mockito.doThrow(new RuntimeException("멱등 레코드 갱신 실패"))
                .when(idempotencyHelper).markCompleted(eq(IDEMPOTENCY_KEY), org.mockito.ArgumentMatchers.anyString());

        assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("멱등 레코드 갱신 실패");

        verify(idempotencyHelper, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("IN_PROGRESS 재요청 시 IdempotencyKeyInProgressException 이 전파되고 비즈니스 플로우는 실행되지 않는다")
    void shouldPropagateInProgressException() {
        CancelPaymentCommand command = createFullCancelCommand();
        org.mockito.Mockito.reset(idempotencyHelper);
        when(idempotencyHelper.findOrCreate(IDEMPOTENCY_KEY, ORDER_KEY_STR, BUYER_ID))
                .thenThrow(new com.personal.marketnote.commerce.exception.IdempotencyKeyInProgressException(IDEMPOTENCY_KEY));

        assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                .isInstanceOf(com.personal.marketnote.commerce.exception.IdempotencyKeyInProgressException.class);

        verify(txHelper, never()).prepareCancel(any());
    }

    private PaymentCancelContext createContext(boolean isFullCancel, Long adjustedCancelAmount, Long shippingFeeDeduction) {
        Payment payment = createSuccessPayment();
        PspPaymentEvent event = createCompleteEvent();
        Order order = createOrder();
        boolean fullFlag = isFullCancel;
        String modType = fullFlag ? "STSC" : "STPC";
        return PaymentCancelContext.of(
                payment, event, order,
                50000L, adjustedCancelAmount, 50000L - adjustedCancelAmount, 0L, shippingFeeDeduction,
                fullFlag, "고객 요청", "고객 요청", fullFlag ? null : UUID.randomUUID().toString(),
                "tno_123", modType, createFullCancelCommand()
        );
    }

    private Payment createSuccessPayment() {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(1L)
                .orderKey(ORDER_KEY)
                .paymentAmount(50000L)
                .build();
        Payment payment = Payment.from(state);
        payment.markAsSuccess("tno_123");
        return payment;
    }

    private PspPaymentEvent createCompleteEvent() {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(1L)
                .orderKey(ORDER_KEY_STR)
                .pgCompanyKey("NHN_KCP")
                .pgPaymentKey("tno_123")
                .poStatus(PaymentEventStatus.COMPLETE)
                .method("CARD")
                .amount(50000L)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .paidAt(LocalDateTime.of(2026, 2, 10, 15, 30, 0))
                .build();
        return PspPaymentEvent.from(state);
    }

    private Order createOrder() {
        OrderProductSnapshotState productState = OrderProductSnapshotState.builder()
                .pricePolicyId(100L)
                .quantity(2)
                .sellerId(10L)
                .unitAmount(25000L)
                .accumulatedPoint(0L)
                .build();
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(1L)
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.from(OrderAmountCreateState.builder().totalAmount(50000L).paidAmount(null).couponAmount(null).pointAmount(0L).shippingFee(null).build()))
                .shippingAddress(ShippingAddress.from(ShippingAddressCreateState.builder().recipientName("수령인").recipientPhoneNumber("010-1234-5678").zipCode("12345").address("서울시 강남구").addressDetail("상세주소").deliveryRequestType(null).deliveryRequestMessage(null).build()))
                .orderProductStates(List.of(productState))
                .build();
        return Order.from(state);
    }

    private CancelPaymentCommand createFullCancelCommand() {
        return CancelPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY_STR)
                .cancelType(CancelPaymentCommand.CancelType.FULL)
                .cancelReason("고객 요청")
                .idempotencyKey(IDEMPOTENCY_KEY)
                .build();
    }

    private PaymentCancelVendorResult createSuccessVendorResult() {
        return PaymentCancelVendorResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("취소 성공")
                .amount("50000")
                .rawResponse("{\"res_cd\":\"0000\"}")
                .build();
    }
}
