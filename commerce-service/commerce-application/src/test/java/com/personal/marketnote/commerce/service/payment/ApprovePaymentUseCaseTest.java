package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovePaymentUseCase 테스트")
class ApprovePaymentUseCaseTest {

    @InjectMocks
    private ApprovePaymentService approvePaymentService;

    @Mock
    private PaymentApprovalTransactionHelper txHelper;

    @Mock
    private PaymentVendorPort paymentVendorPort;

    private static final Long BUYER_ID = 100L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("결제 승인 성공")
    class ApproveSuccessTest {

        @Test
        @DisplayName("KCP 승인 성공 시 commitSuccess가 호출되고 결과가 반환된다")
        void shouldApprovePaymentSuccessfully() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalContext context = createContext();
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_123", "50000");
            ApprovePaymentResult expectedResult = createExpectedResult("tno_123");

            when(txHelper.prepareExecution(command)).thenReturn(context);
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);
            when(txHelper.commitSuccess(eq(context), eq(vendorResult), any(Short.class))).thenReturn(expectedResult);

            ApprovePaymentResult result = approvePaymentService.approve(command);

            assertThat(result.pgPaymentKey()).isEqualTo("tno_123");
            assertThat(result.resultCode()).isEqualTo("0000");
            verify(txHelper).prepareExecution(command);
            verify(txHelper).commitSuccess(eq(context), eq(vendorResult), eq((short) 0));
            verify(txHelper, never()).commitFailure(any(), any(), any());
            verify(txHelper, never()).commitUnknown(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("결제 승인 실패")
    class ApproveFailureTest {

        @Test
        @DisplayName("KCP 승인 응답 res_cd가 0000이 아닌 경우 commitFailure가 호출된다")
        void shouldCommitFailureWhenKcpReturnsError() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalContext context = createContext();
            PaymentApprovalVendorResult vendorResult = PaymentApprovalVendorResult.builder()
                    .resCd("8001")
                    .resMsg("카드 인증 실패")
                    .rawResponse("{}")
                    .build();

            when(txHelper.prepareExecution(command)).thenReturn(context);
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentApprovalException.class);

            verify(txHelper).commitFailure(context, "8001", "카드 인증 실패");
            verify(txHelper, never()).commitSuccess(any(), any(), any());
            verify(txHelper, never()).commitUnknown(any(), any(), any());
        }

        @Test
        @DisplayName("KCP 통신 중 예외 발생 시 commitUnknown이 호출된다")
        void shouldCommitUnknownWhenVendorCommunicationFails() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalContext context = createContext();

            when(txHelper.prepareExecution(command)).thenReturn(context);
            when(paymentVendorPort.approvePayment(any())).thenThrow(new RuntimeException("Connection timeout"));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentApprovalException.class);

            verify(txHelper).commitUnknown(eq(context), eq("UNKNOWN"), contains("Connection timeout"));
            verify(txHelper, never()).commitSuccess(any(), any(), any());
            verify(txHelper, never()).commitFailure(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("검증 실패 (prepareExecution)")
    class ValidationFailureTest {

        @Test
        @DisplayName("결제를 찾을 수 없으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);

            when(txHelper.prepareExecution(command)).thenThrow(new PaymentNotFoundException(ORDER_KEY_STR));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }

        @Test
        @DisplayName("거래 등록이 없으면 PaymentEventNotFoundException이 발생한다")
        void shouldThrowWhenPaymentEventNotFound() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);

            when(txHelper.prepareExecution(command)).thenThrow(new PaymentEventNotFoundException(ORDER_KEY_STR));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentEventNotFoundException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }

        @Test
        @DisplayName("주문 소유자가 아니면 UnauthorizedOrderAccessException이 발생한다")
        void shouldThrowWhenBuyerIsNotOrderOwner() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);

            when(txHelper.prepareExecution(command)).thenThrow(new UnauthorizedOrderAccessException());

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }

        @Test
        @DisplayName("결제 금액 불일치 시 PaymentAmountMismatchException이 발생한다")
        void shouldThrowWhenAmountMismatch() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);

            when(txHelper.prepareExecution(command)).thenThrow(new PaymentAmountMismatchException(60000L, 50000L));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentAmountMismatchException.class);

            verify(paymentVendorPort, never()).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("트랜잭션 분리 검증")
    class TransactionSeparationTest {

        @Test
        @DisplayName("승인 성공 시 prepareExecution → KCP호출 → commitSuccess 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalContext context = createContext();
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult("tno_order", "50000");
            ApprovePaymentResult expectedResult = createExpectedResult("tno_order");

            when(txHelper.prepareExecution(command)).thenReturn(context);
            when(paymentVendorPort.approvePayment(any())).thenReturn(vendorResult);
            when(txHelper.commitSuccess(eq(context), eq(vendorResult), any(Short.class))).thenReturn(expectedResult);

            approvePaymentService.approve(command);

            var inOrder = inOrder(txHelper, paymentVendorPort);
            inOrder.verify(txHelper).prepareExecution(command);
            inOrder.verify(paymentVendorPort).approvePayment(any());
            inOrder.verify(txHelper).commitSuccess(eq(context), eq(vendorResult), any(Short.class));
        }

        @Test
        @DisplayName("KCP 통신 예외 시 prepareExecution → KCP호출 → commitUnknown 순서로 호출된다")
        void shouldCallCommitUnknownAfterCommunicationFailure() {
            ApprovePaymentCommand command = createApproveCommand(ORDER_KEY_STR);
            PaymentApprovalContext context = createContext();

            when(txHelper.prepareExecution(command)).thenReturn(context);
            when(paymentVendorPort.approvePayment(any())).thenThrow(new RuntimeException("Timeout"));

            assertThatThrownBy(() -> approvePaymentService.approve(command))
                    .isInstanceOf(PaymentApprovalException.class);

            var inOrder = inOrder(txHelper, paymentVendorPort);
            inOrder.verify(txHelper).prepareExecution(command);
            inOrder.verify(paymentVendorPort).approvePayment(any());
            inOrder.verify(txHelper).commitUnknown(eq(context), eq("UNKNOWN"), any());
        }
    }

    private PaymentApprovalContext createContext() {
        Payment payment = createPayment(1L, ORDER_KEY, 50000L);
        PspPaymentEvent event = createReadyEvent(1L, ORDER_KEY_STR, 50000L);
        return PaymentApprovalContext.of(payment, event);
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

    private ApprovePaymentResult createExpectedResult(String tno) {
        return ApprovePaymentResult.builder()
                .orderId(1L)
                .orderKey(ORDER_KEY_STR)
                .pgPaymentKey(tno)
                .amount(50000L)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .payMethod("PACA")
                .build();
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
