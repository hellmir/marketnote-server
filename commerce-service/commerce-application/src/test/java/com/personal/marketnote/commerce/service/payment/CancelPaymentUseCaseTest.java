package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.order.ShippingAddressCreateState;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.PaymentCancelException;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CancelPaymentService 오케스트레이션 테스트
 *
 * <p>서비스는 PaymentCancelTransactionHelper를 순차 호출하는 역할만 수행하므로,
 * 이 테스트는 TX-1 → PG 호출 → TX-2A → TX-2B 호출 순서와 예외 전파만 검증한다.
 * Helper의 내부 비즈니스 로직은 {@link PaymentCancelTransactionHelperTest}에서 검증된다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPaymentUseCase 테스트")
class CancelPaymentUseCaseTest {

    @InjectMocks
    private CancelPaymentService cancelPaymentService;

    @Mock
    private PaymentCancelTransactionHelper txHelper;

    @Mock
    private PaymentVendorPort paymentVendorPort;

    private static final Long BUYER_ID = 100L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("일반 취소 경로 (PG 호출)")
    class NormalCancelFlowTest {

        @Test
        @DisplayName("PG 취소 성공 시 TX-1 → PG → TX-2A → TX-2B 순으로 호출된다")
        void shouldCallHelperMethodsInOrderWhenVendorSucceeds() {
            CancelPaymentCommand command = createFullCancelCommand();
            PaymentCancelContext context = createContext(false, 50000L, 0L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(txHelper.prepareCancel(command)).thenReturn(context);
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            InOrder inOrder = inOrder(txHelper, paymentVendorPort);
            inOrder.verify(txHelper).prepareCancel(command);
            inOrder.verify(paymentVendorPort).cancelPayment(any());
            inOrder.verify(txHelper).commitPspEventCancel(context, vendorResult);
            inOrder.verify(txHelper).commitPaymentAndPostProcess(context, vendorResult);
            verify(txHelper, never()).commitCancelSkipPg(any());
        }

        @Test
        @DisplayName("PG 취소 실패 시 PaymentCancelException이 발생하고 TX-2A/TX-2B가 호출되지 않는다")
        void shouldThrowAndSkipCommitsWhenVendorFails() {
            CancelPaymentCommand command = createFullCancelCommand();
            PaymentCancelContext context = createContext(false, 50000L, 0L);
            PaymentCancelVendorResult vendorResult = PaymentCancelVendorResult.builder()
                    .success(false)
                    .resultCode("8001")
                    .resultMessage("취소 불가")
                    .rawResponse("{}")
                    .build();

            when(txHelper.prepareCancel(command)).thenReturn(context);
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(PaymentCancelException.class)
                    .hasMessageContaining("8001")
                    .hasMessageContaining("취소 불가");

            verify(txHelper, never()).commitPspEventCancel(any(), any());
            verify(txHelper, never()).commitPaymentAndPostProcess(any(), any());
            verify(txHelper, never()).commitCancelSkipPg(any());
        }

        @Test
        @DisplayName("PG 호출 시 Context의 transactionNumber/modType/금액이 전달된다")
        void shouldPassVendorCommandFieldsFromContext() {
            CancelPaymentCommand command = createFullCancelCommand();
            PaymentCancelContext context = createContext(true, 50000L, 0L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(txHelper.prepareCancel(command)).thenReturn(context);
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(paymentVendorPort).cancelPayment(org.mockito.ArgumentMatchers.argThat(c ->
                    "tno_123".equals(c.transactionId())
                            && "STSC".equals(c.cancelType())
                            && c.cancelAmount().equals(50000L)
                            && c.remainAmount().equals(0L)
            ));
        }
    }

    @Nested
    @DisplayName("배송비 차감 환불 0원 경로 (PG 호출 생략)")
    class SkipPgFlowTest {

        @Test
        @DisplayName("adjustedCancelAmount가 0이고 shippingFeeDeduction > 0이면 commitCancelSkipPg만 호출된다")
        void shouldCallCommitCancelSkipPgWhenAdjustedIsZero() {
            CancelPaymentCommand command = createPartialCancelCommand();
            PaymentCancelContext context = createContext(false, 0L, 3000L);

            when(txHelper.prepareCancel(command)).thenReturn(context);

            cancelPaymentService.cancel(command);

            verify(txHelper).commitCancelSkipPg(context);
            verify(paymentVendorPort, never()).cancelPayment(any());
            verify(txHelper, never()).commitPspEventCancel(any(), any());
            verify(txHelper, never()).commitPaymentAndPostProcess(any(), any());
        }

        @Test
        @DisplayName("adjustedCancelAmount가 0이어도 shippingFeeDeduction이 0이면 PG 경로를 탄다")
        void shouldFollowPgFlowWhenShippingFeeDeductionIsZeroEvenIfAdjustedIsZero() {
            CancelPaymentCommand command = createPartialCancelCommand();
            PaymentCancelContext context = createContext(false, 0L, 0L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(txHelper.prepareCancel(command)).thenReturn(context);
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(paymentVendorPort).cancelPayment(any());
            verify(txHelper, never()).commitCancelSkipPg(any());
        }

        @Test
        @DisplayName("adjustedCancelAmount가 양수면 shippingFeeDeduction이 있어도 PG 경로를 탄다")
        void shouldFollowPgFlowWhenAdjustedIsPositive() {
            CancelPaymentCommand command = createPartialCancelCommand();
            PaymentCancelContext context = createContext(false, 17000L, 3000L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(txHelper.prepareCancel(command)).thenReturn(context);
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            verify(paymentVendorPort).cancelPayment(any());
            verify(txHelper, never()).commitCancelSkipPg(any());
        }
    }

    @Nested
    @DisplayName("prepareCancel 예외 전파")
    class PrepareFailureTest {

        @Test
        @DisplayName("prepareCancel에서 예외 발생 시 PG/Commit 메서드가 호출되지 않는다")
        void shouldNotCallSubsequentStepsWhenPrepareFails() {
            CancelPaymentCommand command = createFullCancelCommand();

            when(txHelper.prepareCancel(command))
                    .thenThrow(new com.personal.marketnote.commerce.exception.PaymentNotFoundException(ORDER_KEY_STR));

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(com.personal.marketnote.commerce.exception.PaymentNotFoundException.class);

            verify(paymentVendorPort, never()).cancelPayment(any());
            verify(txHelper, never()).commitPspEventCancel(any(), any());
            verify(txHelper, never()).commitPaymentAndPostProcess(any(), any());
            verify(txHelper, never()).commitCancelSkipPg(any());
        }
    }

    @Nested
    @DisplayName("원자성 보장 (#4097)")
    class AtomicityTest {

        @Test
        @DisplayName("PG 성공 후 TX-2A(PSP Event)가 TX-2B(payment)보다 먼저 커밋된다")
        void shouldCommitPspEventBeforePaymentAndPostProcess() {
            CancelPaymentCommand command = createFullCancelCommand();
            PaymentCancelContext context = createContext(false, 50000L, 0L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(txHelper.prepareCancel(command)).thenReturn(context);
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);

            cancelPaymentService.cancel(command);

            InOrder inOrder = inOrder(txHelper);
            inOrder.verify(txHelper).commitPspEventCancel(context, vendorResult);
            inOrder.verify(txHelper).commitPaymentAndPostProcess(context, vendorResult);
        }

        @Test
        @DisplayName("TX-2B 실패 시에도 TX-2A(PSP Event)는 이미 커밋되어 있다 — 수동 복구 근거 확보")
        void shouldKeepPspEventCommittedWhenPaymentCommitFails() {
            CancelPaymentCommand command = createFullCancelCommand();
            PaymentCancelContext context = createContext(false, 50000L, 0L);
            PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

            when(txHelper.prepareCancel(command)).thenReturn(context);
            when(paymentVendorPort.cancelPayment(any())).thenReturn(vendorResult);
            org.mockito.Mockito.doThrow(new RuntimeException("DB 오류"))
                    .when(txHelper).commitPaymentAndPostProcess(context, vendorResult);

            assertThatThrownBy(() -> cancelPaymentService.cancel(command))
                    .isInstanceOf(RuntimeException.class);

            // TX-2A는 TX-2B 예외 발생 전에 별도 트랜잭션으로 이미 호출되었음
            verify(txHelper).commitPspEventCancel(context, vendorResult);
        }
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
                .build();
    }

    private CancelPaymentCommand createPartialCancelCommand() {
        return CancelPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY_STR)
                .cancelType(CancelPaymentCommand.CancelType.PARTIAL)
                .cancelAmount(20000L)
                .cancelReason("부분 취소")
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
