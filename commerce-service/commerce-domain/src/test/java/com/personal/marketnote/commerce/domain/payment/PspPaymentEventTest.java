package com.personal.marketnote.commerce.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PspPaymentEvent 도메인 테스트")
class PspPaymentEventTest {

    @Nested
    @DisplayName("createReady 팩토리 메서드")
    class CreateReadyTest {

        @Test
        @DisplayName("Payment로부터 READY 상태의 이벤트가 생성된다")
        void shouldCreateReadyEvent() {
            Payment payment = createPayment(1L, UUID.randomUUID(), 50000L);

            PspPaymentEvent event = PspPaymentEvent.createReady(payment, "T0000", "PACA");

            assertThat(event.getOrderId()).isEqualTo(1L);
            assertThat(event.getOrderKey()).isEqualTo(payment.getOrderKey().toString());
            assertThat(event.getPgCompanyKey()).isEqualTo("NHN_KCP");
            assertThat(event.getPgShopKey()).isEqualTo("T0000");
            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.READY);
            assertThat(event.getMethod()).isEqualTo("PACA");
            assertThat(event.getAmount()).isEqualTo(50000L);
        }
    }

    @Nested
    @DisplayName("startExecution 메서드")
    class StartExecutionTest {

        @Test
        @DisplayName("READY 상태에서 EXECUTING으로 전이된다")
        void shouldTransitionToExecuting() {
            PspPaymentEvent event = createReadyEvent();

            event.startExecution();

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.EXECUTING);
        }

        @Test
        @DisplayName("READY가 아닌 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotReady() {
            PspPaymentEvent event = createReadyEvent();
            event.startExecution();

            assertThatThrownBy(event::startExecution)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("completeWithApproval 메서드")
    class CompleteWithApprovalTest {

        @Test
        @DisplayName("EXECUTING 상태에서 승인 정보와 함께 COMPLETE로 전이된다")
        void shouldCompleteWithApprovalData() {
            PspPaymentEvent event = createExecutingEvent();

            event.completeWithApproval(createApprovalInfo("20260210153000"));

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.COMPLETE);
            assertThat(event.getPgPaymentKey()).isEqualTo("tno_123");
            assertThat(event.getMethod()).isEqualTo("PACA");
            assertThat(event.getCardNumber()).isEqualTo("1234-****-****-5678");
            assertThat(event.getApprovalNumber()).isEqualTo("12345678");
            assertThat(event.getInstallment()).isEqualTo((short) 0);
            assertThat(event.getIssueCompanyCode()).isEqualTo("CCLG");
            assertThat(event.getIssueCompanyName()).isEqualTo("신한카드");
            assertThat(event.getResultCode()).isEqualTo("0000");
            assertThat(event.getResultMessage()).isEqualTo("승인 성공");
            assertThat(event.getPgApprovalResult()).isEqualTo("{\"res_cd\":\"0000\"}");
            assertThat(event.getPaidAt()).isNotNull();
            assertThat(event.getPaidAt().getYear()).isEqualTo(2026);
        }

        @Test
        @DisplayName("appTime이 null이면 현재 시간을 사용한다")
        void shouldUseCurrentTimeWhenAppTimeIsNull() {
            PspPaymentEvent event = createExecutingEvent();

            event.completeWithApproval(createApprovalInfo(null));

            assertThat(event.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("EXECUTING이 아닌 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotExecuting() {
            PspPaymentEvent event = createReadyEvent();

            assertThatThrownBy(() -> event.completeWithApproval(createApprovalInfo("20260210153000")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("failExecution 메서드")
    class FailExecutionTest {

        @Test
        @DisplayName("EXECUTING 상태에서 실패 정보와 함께 FAILED로 전이된다")
        void shouldTransitionToFailedWithFailInfo() {
            PspPaymentEvent event = createExecutingEvent();

            event.failExecution("8001", "결제 실패");

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.FAILED);
            assertThat(event.getResultCode()).isEqualTo("8001");
            assertThat(event.getResultMessage()).isEqualTo("결제 실패");
        }

        @Test
        @DisplayName("EXECUTING이 아닌 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotExecuting() {
            PspPaymentEvent event = createReadyEvent();

            assertThatThrownBy(() -> event.failExecution("8001", "결제 실패"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("markUnknown 메서드")
    class MarkUnknownTest {

        @Test
        @DisplayName("EXECUTING 상태에서 UNKNOWN으로 전이되고 결과 정보가 저장된다")
        void shouldTransitionToUnknownWithResultInfo() {
            PspPaymentEvent event = createExecutingEvent();

            event.markUnknown("UNKNOWN", "KCP 통신 오류: Connection timeout");

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.UNKNOWN);
            assertThat(event.getResultCode()).isEqualTo("UNKNOWN");
            assertThat(event.getResultMessage()).isEqualTo("KCP 통신 오류: Connection timeout");
        }

        @Test
        @DisplayName("EXECUTING이 아닌 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotExecuting() {
            PspPaymentEvent event = createReadyEvent();

            assertThatThrownBy(() -> event.markUnknown("UNKNOWN", "타임아웃"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("resolveToComplete 메서드")
    class ResolveToCompleteTest {

        @Test
        @DisplayName("UNKNOWN 상태에서 승인 정보와 함께 COMPLETE로 전이된다")
        void shouldTransitionToCompleteFromUnknown() {
            PspPaymentEvent event = createUnknownEvent();

            event.resolveToComplete(createApprovalInfo("20260304120000"));

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.COMPLETE);
            assertThat(event.getPgPaymentKey()).isEqualTo("tno_123");
            assertThat(event.getApprovalNumber()).isEqualTo("12345678");
            assertThat(event.getResultCode()).isEqualTo("0000");
        }

        @Test
        @DisplayName("UNKNOWN이 아닌 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotUnknown() {
            PspPaymentEvent event = createExecutingEvent();

            assertThatThrownBy(() -> event.resolveToComplete(createApprovalInfo("20260304120000")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("resolveToFailed 메서드")
    class ResolveToFailedTest {

        @Test
        @DisplayName("UNKNOWN 상태에서 실패 정보와 함께 FAILED로 전이된다")
        void shouldTransitionToFailedFromUnknown() {
            PspPaymentEvent event = createUnknownEvent();

            event.resolveToFailed("8001", "KCP 가맹점 사이트 확인 결과 미승인");

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.FAILED);
            assertThat(event.getResultCode()).isEqualTo("8001");
            assertThat(event.getResultMessage()).isEqualTo("KCP 가맹점 사이트 확인 결과 미승인");
        }

        @Test
        @DisplayName("UNKNOWN이 아닌 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotUnknown() {
            PspPaymentEvent event = createReadyEvent();

            assertThatThrownBy(() -> event.resolveToFailed("8001", "미승인"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("isUnresolved 메서드")
    class IsUnresolvedTest {

        @Test
        @DisplayName("READY 상태는 미해결 상태이다")
        void shouldReturnTrueForReady() {
            PspPaymentEvent event = createReadyEvent();

            assertThat(event.isUnresolved()).isTrue();
        }

        @Test
        @DisplayName("EXECUTING 상태는 미해결 상태이다")
        void shouldReturnTrueForExecuting() {
            PspPaymentEvent event = createExecutingEvent();

            assertThat(event.isUnresolved()).isTrue();
        }

        @Test
        @DisplayName("UNKNOWN 상태는 미해결 상태이다")
        void shouldReturnTrueForUnknown() {
            PspPaymentEvent event = createUnknownEvent();

            assertThat(event.isUnresolved()).isTrue();
        }

        @Test
        @DisplayName("COMPLETE 상태는 해결 상태이다")
        void shouldReturnFalseForComplete() {
            PspPaymentEvent event = createCompletedEvent();

            assertThat(event.isUnresolved()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태는 해결 상태이다")
        void shouldReturnFalseForFailed() {
            PspPaymentEvent event = createExecutingEvent();
            event.failExecution("8001", "실패");

            assertThat(event.isUnresolved()).isFalse();
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class CancelTest {

        @Test
        @DisplayName("COMPLETE 상태에서 CANCELLED로 전이된다")
        void shouldTransitionToCancelled() {
            PspPaymentEvent event = createCompletedEvent();

            event.cancel("{\"mod_type\":\"STSC\"}");

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.CANCELLED);
            assertThat(event.getPgCancelApprovalResult()).isEqualTo("{\"mod_type\":\"STSC\"}");
        }

        @Test
        @DisplayName("COMPLETE가 아닌 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotComplete() {
            PspPaymentEvent event = createReadyEvent();

            assertThatThrownBy(() -> event.cancel("{}"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("partialRefund 메서드")
    class PartialRefundTest {

        @Test
        @DisplayName("COMPLETE 상태에서 PARTIALLY_REFUNDED로 전이된다")
        void shouldTransitionToPartiallyRefunded() {
            PspPaymentEvent event = createCompletedEvent();

            event.partialRefund("{\"mod_type\":\"STPC\"}");

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.PARTIALLY_REFUNDED);
            assertThat(event.getPgCancelApprovalResult()).isEqualTo("{\"mod_type\":\"STPC\"}");
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED 상태에서 다시 부분 환불이 가능하다")
        void shouldAllowMultiplePartialRefunds() {
            PspPaymentEvent event = createCompletedEvent();

            event.partialRefund("{\"first_partial\"}");
            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.PARTIALLY_REFUNDED);

            event.partialRefund("{\"second_partial\"}");
            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.PARTIALLY_REFUNDED);
            assertThat(event.getPgCancelApprovalResult()).isEqualTo("{\"second_partial\"}");
        }

        @Test
        @DisplayName("READY 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotRefundable() {
            PspPaymentEvent event = createReadyEvent();

            assertThatThrownBy(() -> event.partialRefund("{}"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("refund 메서드")
    class RefundTest {

        @Test
        @DisplayName("COMPLETE 상태에서 REFUNDED로 전이된다")
        void shouldTransitionToRefundedFromComplete() {
            PspPaymentEvent event = createCompletedEvent();

            event.refund("{\"mod_type\":\"STPC\"}");

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.REFUNDED);
            assertThat(event.getPgCancelApprovalResult()).isEqualTo("{\"mod_type\":\"STPC\"}");
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED 상태에서 REFUNDED로 전이된다")
        void shouldTransitionToRefundedFromPartiallyRefunded() {
            PspPaymentEvent event = createCompletedEvent();
            event.partialRefund("{\"partial\"}");

            event.refund("{\"full_refund\"}");

            assertThat(event.getPoStatus()).isEqualTo(PaymentEventStatus.REFUNDED);
        }

        @Test
        @DisplayName("READY 상태에서는 예외가 발생한다")
        void shouldThrowWhenNotRefundable() {
            PspPaymentEvent event = createReadyEvent();

            assertThatThrownBy(() -> event.refund("{}"))
                    .isInstanceOf(IllegalStateException.class);
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

    private PspPaymentEvent createReadyEvent() {
        Payment payment = createPayment(1L, UUID.randomUUID(), 50000L);
        return PspPaymentEvent.createReady(payment, "T0000", "PACA");
    }

    private PspPaymentEvent createExecutingEvent() {
        PspPaymentEvent event = createReadyEvent();
        event.startExecution();
        return event;
    }

    private PspPaymentEvent createCompletedEvent() {
        PspPaymentEvent event = createExecutingEvent();
        event.completeWithApproval(createApprovalInfo("20260210153000"));
        return event;
    }

    private PspPaymentEvent createUnknownEvent() {
        PspPaymentEvent event = createExecutingEvent();
        event.markUnknown("UNKNOWN", "KCP 통신 오류");
        return event;
    }

    private PaymentApprovalInfo createApprovalInfo(String appTime) {
        return PaymentApprovalInfo.builder()
                .pgPaymentKey("tno_123")
                .method("PACA")
                .cardNumber("1234-****-****-5678")
                .approvalNumber("12345678")
                .installment((short) 0)
                .issueCompanyCode("CCLG")
                .issueCompanyName("신한카드")
                .resultCode("0000")
                .resultMessage("승인 성공")
                .pgApprovalResult("{\"res_cd\":\"0000\"}")
                .appTime(appTime)
                .build();
    }
}
