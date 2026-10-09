package com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller;

import com.personal.marketnote.commerce.adapter.in.web.quickpayment.request.ApproveQuickPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.request.IssueBatchKeyRequest;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.response.ApproveQuickPaymentResponse;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.response.IssueBatchKeyResponse;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.response.RegisterQuickPaymentTransactionResponse;
import com.personal.marketnote.commerce.port.in.command.quickpayment.ApproveQuickPaymentCommand;
import com.personal.marketnote.commerce.port.in.command.quickpayment.DeleteQuickPaymentCardCommand;
import com.personal.marketnote.commerce.port.in.command.quickpayment.IssueBatchKeyCommand;
import com.personal.marketnote.commerce.port.in.command.quickpayment.RegisterQuickPaymentTransactionCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.ApproveQuickPaymentResult;
import com.personal.marketnote.commerce.port.in.result.quickpayment.IssueBatchKeyResult;
import com.personal.marketnote.commerce.port.in.result.quickpayment.RegisterQuickPaymentTransactionResult;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.ApproveQuickPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.DeleteQuickPaymentCardUseCase;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.IssueBatchKeyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.RegisterQuickPaymentTransactionUseCase;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuickPaymentController 테스트")
class QuickPaymentControllerTest {

    @InjectMocks
    private QuickPaymentController quickPaymentController;

    @Mock
    private RegisterQuickPaymentTransactionUseCase registerQuickPaymentTransactionUseCase;

    @Mock
    private IssueBatchKeyUseCase issueBatchKeyUseCase;

    @Mock
    private ApproveQuickPaymentUseCase approveQuickPaymentUseCase;

    @Mock
    private DeleteQuickPaymentCardUseCase deleteQuickPaymentCardUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Nested
    @DisplayName("registerTransaction")
    class RegisterTransaction {

        @Test
        @DisplayName("빠른결제 거래 등록이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenRegisterSucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            RegisterQuickPaymentTransactionResult result = RegisterQuickPaymentTransactionResult.builder()
                    .transactionId("TX_1")
                    .approvalKey("APPROVAL_1")
                    .payUrl("https://pg/pay")
                    .traceNo("TRACE_1")
                    .build();
            when(registerQuickPaymentTransactionUseCase.register(any(RegisterQuickPaymentTransactionCommand.class)))
                    .thenReturn(result);

            // when
            ResponseEntity<BaseResponse<RegisterQuickPaymentTransactionResponse>> response =
                    quickPaymentController.registerTransaction(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            ArgumentCaptor<RegisterQuickPaymentTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterQuickPaymentTransactionCommand.class);
            verify(registerQuickPaymentTransactionUseCase).register(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo(200L);
        }
    }

    @Nested
    @DisplayName("issueBatchKey")
    class IssueBatchKey {

        @Test
        @DisplayName("카드 배치키 발급이 성공하면 200 OK와 배치키 응답을 반환한다")
        void shouldReturnOkWhenIssueBatchKeySucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            IssueBatchKeyRequest request = mock(IssueBatchKeyRequest.class);
            when(request.getEncData()).thenReturn("ENC_DATA");
            when(request.getEncInfo()).thenReturn("ENC_INFO");
            when(request.getCardMaskNo()).thenReturn("1234-****-****-5678");

            IssueBatchKeyResult result = IssueBatchKeyResult.builder()
                    .quickPaymentCardId(1L)
                    .cardCode("01")
                    .cardName("테스트카드")
                    .maskedCardNumber("1234-****-****-5678")
                    .cardBinType01("TYPE1")
                    .cardBinType02("TYPE2")
                    .build();
            when(issueBatchKeyUseCase.issueBatchKey(any(IssueBatchKeyCommand.class))).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<IssueBatchKeyResponse>> response =
                    quickPaymentController.issueBatchKey(principal, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(issueBatchKeyUseCase).issueBatchKey(any(IssueBatchKeyCommand.class));
        }
    }

    @Nested
    @DisplayName("approveQuickPayment")
    class ApproveQuickPayment {

        @Test
        @DisplayName("빠른결제 승인이 성공하면 200 OK와 승인 응답을 반환한다")
        void shouldReturnOkWhenApproveSucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            ApproveQuickPaymentRequest request = mock(ApproveQuickPaymentRequest.class);
            when(request.getOrderKey()).thenReturn("ORDER_KEY_1");
            when(request.getQuickPaymentCardId()).thenReturn(1L);
            when(request.getGoodName()).thenReturn("테스트 상품");

            ApproveQuickPaymentResult result = ApproveQuickPaymentResult.builder()
                    .orderId(10L)
                    .orderKey("ORDER_KEY_1")
                    .pgPaymentKey("PG_1")
                    .amount(10_000L)
                    .resultCode("0000")
                    .resultMessage("정상 승인")
                    .payMethod("CARD")
                    .build();
            when(approveQuickPaymentUseCase.approve(any(ApproveQuickPaymentCommand.class))).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<ApproveQuickPaymentResponse>> response =
                    quickPaymentController.approveQuickPayment(principal, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderId()).isEqualTo(10L);
            assertThat(response.getBody().getContent().amount()).isEqualTo(10_000L);
            verify(approveQuickPaymentUseCase).approve(any(ApproveQuickPaymentCommand.class));
        }
    }

    @Nested
    @DisplayName("deleteQuickPaymentCard")
    class DeleteQuickPaymentCard {

        @Test
        @DisplayName("빠른결제 카드 삭제가 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenDeleteSucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    quickPaymentController.deleteQuickPaymentCard(principal, 1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<DeleteQuickPaymentCardCommand> captor =
                    ArgumentCaptor.forClass(DeleteQuickPaymentCardCommand.class);
            verify(deleteQuickPaymentCardUseCase).delete(captor.capture());
            assertThat(captor.getValue().quickPaymentCardId()).isEqualTo(1L);
            assertThat(captor.getValue().userId()).isEqualTo(200L);
        }
    }
}
