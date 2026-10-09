package com.personal.marketnote.commerce.adapter.in.web.payment.controller;

import com.personal.marketnote.commerce.adapter.in.web.payment.request.ApprovePaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.CancelPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.ReadyPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.ApprovePaymentResponse;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.GetPaymentResponse;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.ReadyPaymentResponse;
import com.personal.marketnote.commerce.adapter.in.web.refund.response.GetAdminRefundResponse;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.in.result.payment.GetPaymentResult;
import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;
import com.personal.marketnote.commerce.port.in.result.refund.GetAdminRefundResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.ApprovePaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.CancelPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.GetPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.ReadyPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.refund.GetAdminRefundsUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private ReadyPaymentUseCase readyPaymentUseCase;

    @Mock
    private ApprovePaymentUseCase approvePaymentUseCase;

    @Mock
    private CancelPaymentUseCase cancelPaymentUseCase;

    @Mock
    private GetPaymentUseCase getPaymentUseCase;

    @Mock
    private GetAdminRefundsUseCase getAdminRefundsUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Nested
    @DisplayName("readyPayment")
    class ReadyPayment {

        @Test
        @DisplayName("거래 등록이 성공하면 200 OK와 거래 등록 응답을 반환한다")
        void shouldReturnOkWhenReadyPaymentSucceeds() {
            // given
            ReadyPaymentRequest request = mock(ReadyPaymentRequest.class);
            when(request.getOrderKey()).thenReturn("ORDER_KEY_1");
            when(request.getPayMethod()).thenReturn("CARD");
            when(request.getGoodName()).thenReturn("테스트 상품");

            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            ReadyPaymentResult result = ReadyPaymentResult.builder()
                    .orderKey("ORDER_KEY_1")
                    .approvalKey("APPROVAL_KEY_1")
                    .payUrl("https://pg.test/pay")
                    .traceNo("TRACE_1")
                    .build();
            when(readyPaymentUseCase.ready(any(ReadyPaymentCommand.class))).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<ReadyPaymentResponse>> response =
                    paymentController.readyPayment(principal, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderKey()).isEqualTo("ORDER_KEY_1");
            assertThat(response.getBody().getContent().approvalKey()).isEqualTo("APPROVAL_KEY_1");
            assertThat(response.getBody().getContent().payUrl()).isEqualTo("https://pg.test/pay");
            assertThat(response.getBody().getContent().traceNo()).isEqualTo("TRACE_1");
            ArgumentCaptor<ReadyPaymentCommand> captor = ArgumentCaptor.forClass(ReadyPaymentCommand.class);
            verify(readyPaymentUseCase).ready(captor.capture());
            assertThat(captor.getValue().buyerId()).isEqualTo(200L);
            assertThat(captor.getValue().orderKey()).isEqualTo("ORDER_KEY_1");
        }
    }

    @Nested
    @DisplayName("approvePayment")
    class ApprovePayment {

        @Test
        @DisplayName("결제 승인이 성공하면 200 OK와 승인 응답을 반환한다")
        void shouldReturnOkWhenApprovePaymentSucceeds() {
            // given
            ApprovePaymentRequest request = mock(ApprovePaymentRequest.class);
            when(request.getOrderKey()).thenReturn("ORDER_KEY_1");
            when(request.getEncData()).thenReturn("ENC_DATA");
            when(request.getEncInfo()).thenReturn("ENC_INFO");
            when(request.getPayType()).thenReturn("CARD");

            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            ApprovePaymentResult result = ApprovePaymentResult.builder()
                    .orderId(1L)
                    .orderKey("ORDER_KEY_1")
                    .pgPaymentKey("PG_KEY_1")
                    .amount(10_000L)
                    .resultCode("0000")
                    .resultMessage("정상 승인")
                    .payMethod("CARD")
                    .build();
            when(approvePaymentUseCase.approve(any(ApprovePaymentCommand.class))).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<ApprovePaymentResponse>> response =
                    paymentController.approvePayment(principal, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderId()).isEqualTo(1L);
            assertThat(response.getBody().getContent().amount()).isEqualTo(10_000L);
            assertThat(response.getBody().getContent().resultCode()).isEqualTo("0000");
            verify(approvePaymentUseCase).approve(any(ApprovePaymentCommand.class));
        }
    }

    @Nested
    @DisplayName("getPayment")
    class GetPayment {

        @Test
        @DisplayName("주문 키로 결제 정보를 조회하면 200 OK를 반환한다")
        void shouldReturnOkWhenGetPaymentSucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            String orderKey = "ORDER_KEY_1";
            GetPaymentResult result = GetPaymentResult.builder()
                    .orderId(1L)
                    .orderKey(orderKey)
                    .paymentAmount(10_000L)
                    .successYn(true)
                    .refundedYn(false)
                    .refundAmount(0L)
                    .pgPaymentKey("PG_KEY_1")
                    .build();
            when(getPaymentUseCase.getPayment(200L, orderKey)).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetPaymentResponse>> response =
                    paymentController.getPayment(principal, orderKey);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderKey()).isEqualTo(orderKey);
            assertThat(response.getBody().getContent().paymentAmount()).isEqualTo(10_000L);
            assertThat(response.getBody().getContent().successYn()).isTrue();
            verify(getPaymentUseCase).getPayment(200L, orderKey);
        }
    }

    @Nested
    @DisplayName("cancelPayment")
    class CancelPayment {

        @Test
        @DisplayName("결제 취소가 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenCancelPaymentSucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            String orderKey = "ORDER_KEY_1";
            CancelPaymentRequest request = mock(CancelPaymentRequest.class);
            when(request.getCancelType()).thenReturn(CancelPaymentCommand.CancelType.FULL);
            when(request.getCancelAmount()).thenReturn(10_000L);
            when(request.getCancelReason()).thenReturn("단순 변심");
            when(request.getCancelProducts()).thenReturn(null);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    paymentController.cancelPayment(principal, orderKey, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<CancelPaymentCommand> captor = ArgumentCaptor.forClass(CancelPaymentCommand.class);
            verify(cancelPaymentUseCase).cancel(captor.capture());
            assertThat(captor.getValue().buyerId()).isEqualTo(200L);
            assertThat(captor.getValue().orderKey()).isEqualTo(orderKey);
            assertThat(captor.getValue().cancelAmount()).isEqualTo(10_000L);
        }
    }

    @Nested
    @DisplayName("getRefundsByOrderId")
    class GetRefundsByOrderId {

        @Test
        @DisplayName("주문 ID로 환불 목록을 조회하면 200 OK를 반환한다")
        void shouldReturnRefundListWhenGetRefundsSucceeds() {
            // given
            Long orderId = 1L;
            GetAdminRefundResult result = GetAdminRefundResult.builder()
                    .id(10L)
                    .paymentId(20L)
                    .orderId(orderId)
                    .refundType(RefundType.FULL_REFUND)
                    .refundAmount(10_000L)
                    .cancelReason("단순 변심")
                    .processedBy("ADMIN_1")
                    .pgRefundKey("PG_REFUND_1")
                    .pgRawResponse("{}")
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .build();
            when(getAdminRefundsUseCase.getRefundsByOrderId(orderId)).thenReturn(List.of(result));

            // when
            ResponseEntity<BaseResponse<List<GetAdminRefundResponse>>> response =
                    paymentController.getRefundsByOrderId(orderId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).id()).isEqualTo(10L);
            assertThat(response.getBody().getContent().get(0).refundAmount()).isEqualTo(10_000L);
            verify(getAdminRefundsUseCase).getRefundsByOrderId(orderId);
        }
    }
}
