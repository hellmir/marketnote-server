package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCardSnapshotState;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.exception.QuickPaymentApprovalFailedException;
import com.personal.marketnote.commerce.exception.QuickPaymentCardNotFoundException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.ApproveQuickPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.ApproveQuickPaymentResult;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPort;
import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.FindQuickPaymentCardPort;
import com.personal.marketnote.commerce.service.payment.PaymentApprovalContext;
import com.personal.marketnote.commerce.service.payment.PaymentApprovalTransactionHelper;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveQuickPaymentUseCase 테스트")
class ApproveQuickPaymentUseCaseTest {

    @InjectMocks
    private ApproveQuickPaymentService approveQuickPaymentService;

    @Mock
    private PaymentApprovalTransactionHelper txHelper;

    @Mock
    private FindQuickPaymentCardPort findQuickPaymentCardPort;

    @Mock
    private ApproveQuickPaymentPort approveQuickPaymentPort;

    @Mock
    private PaymentVendorPort paymentVendorPort;

    private static final Long BUYER_ID = 100L;
    private static final Long CARD_ID = 10L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("승인 성공")
    class ApproveSuccessTest {

        @Test
        @DisplayName("빠른결제 승인 성공 시 commitSuccess가 호출되고 결과가 반환된다")
        void shouldApproveSuccessfully() {
            ApproveQuickPaymentCommand command = createCommand();
            QuickPaymentCard card = createCard();
            PaymentApprovalContext context = createContext();
            ApproveQuickPaymentPortResult portResult = createSuccessPortResult();
            ApproveQuickPaymentResult expectedResult = createExpectedResult();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, BUYER_ID))
                    .thenReturn(Optional.of(card));
            when(paymentVendorPort.getVendorKey()).thenReturn("NHN_KCP");
            when(paymentVendorPort.getShopCode()).thenReturn("T0000");
            when(txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000"))
                    .thenReturn(context);
            when(approveQuickPaymentPort.approvePayment(any())).thenReturn(portResult);
            when(txHelper.commitSuccess(eq(context), any(PaymentApprovalVendorResult.class), any(Short.class)))
                    .thenReturn(buildApprovePaymentResult());

            ApproveQuickPaymentResult result = approveQuickPaymentService.approve(command);

            assertThat(result.pgPaymentKey()).isEqualTo("tno_quick_123");
            assertThat(result.resultCode()).isEqualTo("0000");
            verify(txHelper).commitSuccess(eq(context), any(PaymentApprovalVendorResult.class), eq((short) 0));
            verify(txHelper, never()).commitFailure(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("승인 실패")
    class ApproveFailureTest {

        @Test
        @DisplayName("KCP 승인 응답 실패 시 commitFailure가 호출된다")
        void shouldCommitFailureWhenKcpReturnsError() {
            ApproveQuickPaymentCommand command = createCommand();
            QuickPaymentCard card = createCard();
            PaymentApprovalContext context = createContext();
            ApproveQuickPaymentPortResult portResult = ApproveQuickPaymentPortResult.builder()
                    .success(false)
                    .resultCode("8001")
                    .resultMessage("카드 인증 실패")
                    .rawResponse("{}")
                    .build();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, BUYER_ID))
                    .thenReturn(Optional.of(card));
            when(paymentVendorPort.getVendorKey()).thenReturn("NHN_KCP");
            when(paymentVendorPort.getShopCode()).thenReturn("T0000");
            when(txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000"))
                    .thenReturn(context);
            when(approveQuickPaymentPort.approvePayment(any())).thenReturn(portResult);

            assertThatThrownBy(() -> approveQuickPaymentService.approve(command))
                    .isInstanceOf(QuickPaymentApprovalFailedException.class);

            verify(txHelper).commitFailure(context, "8001", "카드 인증 실패");
            verify(txHelper, never()).commitSuccess(any(), any(), any());
        }

        @Test
        @DisplayName("KCP 통신 중 예외 발생 시 commitUnknown이 호출된다")
        void shouldCommitUnknownWhenCommunicationFails() {
            ApproveQuickPaymentCommand command = createCommand();
            QuickPaymentCard card = createCard();
            PaymentApprovalContext context = createContext();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, BUYER_ID))
                    .thenReturn(Optional.of(card));
            when(paymentVendorPort.getVendorKey()).thenReturn("NHN_KCP");
            when(paymentVendorPort.getShopCode()).thenReturn("T0000");
            when(txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000"))
                    .thenReturn(context);
            when(approveQuickPaymentPort.approvePayment(any()))
                    .thenThrow(new RuntimeException("Connection timeout"));

            assertThatThrownBy(() -> approveQuickPaymentService.approve(command))
                    .isInstanceOf(QuickPaymentApprovalFailedException.class);

            verify(txHelper).commitUnknown(eq(context), eq("UNKNOWN"), contains("Connection timeout"));
            verify(txHelper, never()).commitSuccess(any(), any(), any());
        }

        @Test
        @DisplayName("PaymentVendorConnectionFailedException 발생 시 commitFailure가 호출된다")
        void shouldCommitFailureWhenConnectionFailed() {
            ApproveQuickPaymentCommand command = createCommand();
            QuickPaymentCard card = createCard();
            PaymentApprovalContext context = createContext();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, BUYER_ID))
                    .thenReturn(Optional.of(card));
            when(paymentVendorPort.getVendorKey()).thenReturn("NHN_KCP");
            when(paymentVendorPort.getShopCode()).thenReturn("T0000");
            when(txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000"))
                    .thenReturn(context);
            when(approveQuickPaymentPort.approvePayment(any()))
                    .thenThrow(new PaymentVendorConnectionFailedException("KCP 연결 실패"));

            assertThatThrownBy(() -> approveQuickPaymentService.approve(command))
                    .isInstanceOf(QuickPaymentApprovalFailedException.class);

            verify(txHelper).commitFailure(eq(context), eq("CONN_FAIL"), contains("KCP 연결 실패"));
        }
    }

    @Nested
    @DisplayName("검증 실패")
    class ValidationFailureTest {

        @Test
        @DisplayName("빠른결제 카드가 존재하지 않으면 QuickPaymentCardNotFoundException이 발생한다")
        void shouldThrowWhenCardNotFound() {
            ApproveQuickPaymentCommand command = createCommand();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, BUYER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> approveQuickPaymentService.approve(command))
                    .isInstanceOf(QuickPaymentCardNotFoundException.class);

            verify(txHelper, never()).prepareExecutionForQuickPayment(any(), any(), any(), any());
            verify(approveQuickPaymentPort, never()).approvePayment(any());
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 PaymentNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            ApproveQuickPaymentCommand command = createCommand();
            QuickPaymentCard card = createCard();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, BUYER_ID))
                    .thenReturn(Optional.of(card));
            when(paymentVendorPort.getVendorKey()).thenReturn("NHN_KCP");
            when(paymentVendorPort.getShopCode()).thenReturn("T0000");
            when(txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000"))
                    .thenThrow(new PaymentNotFoundException(ORDER_KEY_STR));

            assertThatThrownBy(() -> approveQuickPaymentService.approve(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(approveQuickPaymentPort, never()).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("트랜잭션 분리 검증")
    class TransactionSeparationTest {

        @Test
        @DisplayName("승인 성공 시 카드조회 → prepareExecution → KCP호출 → commitSuccess 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            ApproveQuickPaymentCommand command = createCommand();
            QuickPaymentCard card = createCard();
            PaymentApprovalContext context = createContext();
            ApproveQuickPaymentPortResult portResult = createSuccessPortResult();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, BUYER_ID))
                    .thenReturn(Optional.of(card));
            when(paymentVendorPort.getVendorKey()).thenReturn("NHN_KCP");
            when(paymentVendorPort.getShopCode()).thenReturn("T0000");
            when(txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000"))
                    .thenReturn(context);
            when(approveQuickPaymentPort.approvePayment(any())).thenReturn(portResult);
            when(txHelper.commitSuccess(eq(context), any(PaymentApprovalVendorResult.class), any(Short.class)))
                    .thenReturn(buildApprovePaymentResult());

            approveQuickPaymentService.approve(command);

            InOrder inOrder = inOrder(findQuickPaymentCardPort, txHelper, approveQuickPaymentPort);
            inOrder.verify(findQuickPaymentCardPort).findActiveByIdAndUserId(CARD_ID, BUYER_ID);
            inOrder.verify(txHelper).prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000");
            inOrder.verify(approveQuickPaymentPort).approvePayment(any());
            inOrder.verify(txHelper).commitSuccess(eq(context), any(PaymentApprovalVendorResult.class), any(Short.class));
        }
    }

    private ApproveQuickPaymentCommand createCommand() {
        return ApproveQuickPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY_STR)
                .quickPaymentCardId(CARD_ID)
                .goodName("테스트 상품")
                .build();
    }

    private QuickPaymentCard createCard() {
        return QuickPaymentCard.from(QuickPaymentCardSnapshotState.builder()
                .id(CARD_ID)
                .userId(BUYER_ID)
                .batchKey("batch_key_test")
                .groupId("group_test")
                .cardCode("CCDI")
                .cardName("현대카드")
                .cardBinType01("0")
                .cardBinType02("0")
                .status(EntityStatus.ACTIVE)
                .build());
    }

    private PaymentApprovalContext createContext() {
        Payment payment = Payment.from(PaymentCreateState.builder()
                .orderId(1L)
                .orderKey(ORDER_KEY)
                .paymentAmount(50000L)
                .build());
        PspPaymentEvent event = PspPaymentEvent.from(PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(1L)
                .orderKey(ORDER_KEY_STR)
                .pgCompanyKey("NHN_KCP")
                .pgShopKey("T0000")
                .poStatus(PaymentEventStatus.EXECUTING)
                .method("PACA")
                .amount(50000L)
                .build());
        return PaymentApprovalContext.of(payment, event);
    }

    private ApproveQuickPaymentPortResult createSuccessPortResult() {
        return ApproveQuickPaymentPortResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .transactionId("tno_quick_123")
                .amount("50000")
                .payMethod("PACA")
                .cardCode("CCDI")
                .cardName("현대카드")
                .cardNumber("1234****5678")
                .approvalNumber("12345678")
                .approvalTime("20260405171933")
                .installmentMonths("00")
                .cardAmount("50000")
                .partialCancelYn("Y")
                .cardBinType01("0")
                .cardBinType02("0")
                .rawResponse("{}")
                .build();
    }

    private ApproveQuickPaymentResult createExpectedResult() {
        return ApproveQuickPaymentResult.builder()
                .orderId(1L)
                .orderKey(ORDER_KEY_STR)
                .pgPaymentKey("tno_quick_123")
                .amount(50000L)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .payMethod("PACA")
                .build();
    }

    private com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult buildApprovePaymentResult() {
        return com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult.builder()
                .orderId(1L)
                .orderKey(ORDER_KEY_STR)
                .pgPaymentKey("tno_quick_123")
                .amount(50000L)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .payMethod("PACA")
                .build();
    }
}
