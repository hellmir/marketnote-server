package com.personal.marketnote.commerce.adapter.in.web.settlement.controller;

import com.personal.marketnote.commerce.adapter.in.web.settlement.request.ExecuteSettlementRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSellerSettlementsResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementDetailResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementsResponse;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementDetailResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.CancelSettlementUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ExecuteSettlementUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetFailedSettlementsUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSellerSettlementsUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementDetailUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ReExecuteSettlementUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.RetryFailedSettlementUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("SettlementController 테스트")
class SettlementControllerTest {

    @InjectMocks
    private SettlementController settlementController;

    @Mock
    private ExecuteSettlementUseCase executeSettlementUseCase;

    @Mock
    private RetryFailedSettlementUseCase retryFailedSettlementUseCase;

    @Mock
    private CancelSettlementUseCase cancelSettlementUseCase;

    @Mock
    private ReExecuteSettlementUseCase reExecuteSettlementUseCase;

    @Mock
    private GetFailedSettlementsUseCase getFailedSettlementsUseCase;

    @Mock
    private GetSettlementUseCase getSettlementUseCase;

    @Mock
    private GetSettlementDetailUseCase getSettlementDetailUseCase;

    @Mock
    private GetSellerSettlementsUseCase getSellerSettlementsUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    private GetSettlementResult sampleSettlementResult() {
        return GetSettlementResult.builder()
                .id(1L)
                .sellerId(100L)
                .year(2026)
                .month(1)
                .totalAllocatedAmount(100_000L)
                .shippingFee(3_000L)
                .pgFeeAmount(2_000L)
                .platformFeeAmount(5_000L)
                .sellerPayoutAmount(90_000L)
                .status(SettlementStatus.PENDING)
                .createdAt(LocalDateTime.of(2026, 2, 1, 0, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 1, 0, 0))
                .build();
    }

    @Nested
    @DisplayName("executeSettlement")
    class ExecuteSettlement {

        @Test
        @DisplayName("정산 실행 요청이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenExecuteSettlementSucceeds() {
            // given
            ExecuteSettlementRequest request = mock(ExecuteSettlementRequest.class);
            when(request.getYear()).thenReturn(2026);
            when(request.getMonth()).thenReturn(1);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    settlementController.executeSettlement(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(executeSettlementUseCase).executeSettlement(any(ExecuteSettlementCommand.class));
        }
    }

    @Nested
    @DisplayName("retrySettlement")
    class RetrySettlement {

        @Test
        @DisplayName("정산 재시도가 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenRetrySettlementSucceeds() {
            // when
            ResponseEntity<BaseResponse<Void>> response = settlementController.retrySettlement(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(retryFailedSettlementUseCase).retrySettlement(1L);
        }
    }

    @Nested
    @DisplayName("cancelSettlement")
    class CancelSettlement {

        @Test
        @DisplayName("정산 취소가 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenCancelSettlementSucceeds() {
            // when
            ResponseEntity<BaseResponse<Void>> response = settlementController.cancelSettlement(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cancelSettlementUseCase).cancelSettlement(1L);
        }
    }

    @Nested
    @DisplayName("reExecuteSettlement")
    class ReExecuteSettlement {

        @Test
        @DisplayName("정산 재실행이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenReExecuteSettlementSucceeds() {
            // when
            ResponseEntity<BaseResponse<Void>> response = settlementController.reExecuteSettlement(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(reExecuteSettlementUseCase).reExecuteSettlement(1L);
        }
    }

    @Nested
    @DisplayName("getFailedSettlements")
    class GetFailedSettlements {

        @Test
        @DisplayName("실패한 정산 목록 조회가 성공하면 200 OK를 반환한다")
        void shouldReturnFailedSettlements() {
            // given
            GetSettlementsResult result = new GetSettlementsResult(List.of(sampleSettlementResult()));
            when(getFailedSettlementsUseCase.getFailedSettlements()).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetSettlementsResponse>> response =
                    settlementController.getFailedSettlements();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().settlements()).hasSize(1);
            verify(getFailedSettlementsUseCase).getFailedSettlements();
        }
    }

    @Nested
    @DisplayName("getSettlements")
    class GetSettlements {

        @Test
        @DisplayName("연월 기준 정산 목록 조회가 성공하면 200 OK를 반환한다")
        void shouldReturnSettlements() {
            // given
            GetSettlementsResult result = new GetSettlementsResult(List.of(sampleSettlementResult()));
            when(getSettlementUseCase.getSettlements(any())).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetSettlementsResponse>> response =
                    settlementController.getSettlements(2026, 1);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().settlements()).hasSize(1);
            verify(getSettlementUseCase).getSettlements(any());
        }
    }

    @Nested
    @DisplayName("getSettlement")
    class GetSettlement {

        @Test
        @DisplayName("정산 단건 조회가 성공하면 200 OK와 정산 정보를 반환한다")
        void shouldReturnSettlement() {
            // given
            when(getSettlementUseCase.getSettlement(1L)).thenReturn(sampleSettlementResult());

            // when
            ResponseEntity<BaseResponse<GetSettlementResponse>> response =
                    settlementController.getSettlement(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
            verify(getSettlementUseCase).getSettlement(1L);
        }
    }

    @Nested
    @DisplayName("getSettlementAllocations")
    class GetSettlementAllocations {

        @Test
        @DisplayName("정산 내역 상세 조회가 성공하면 200 OK와 배분 목록을 반환한다")
        void shouldReturnAllocations() {
            // given
            GetSettlementDetailResult detail = GetSettlementDetailResult.builder()
                    .id(10L)
                    .orderId(20L)
                    .sellerId(100L)
                    .allocatedAmount(50_000L)
                    .shippingFee(3_000L)
                    .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                    .targetType(PaymentAllocationTargetType.ORDER)
                    .createdAt(LocalDateTime.of(2026, 1, 15, 0, 0))
                    .build();
            when(getSettlementDetailUseCase.getSettlementAllocations(1L))
                    .thenReturn(List.of(detail));

            // when
            ResponseEntity<BaseResponse<List<GetSettlementDetailResponse>>> response =
                    settlementController.getSettlementAllocations(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).id()).isEqualTo(10L);
            verify(getSettlementDetailUseCase).getSettlementAllocations(1L);
        }
    }

    @Nested
    @DisplayName("getMySettlements")
    class GetMySettlements {

        @Test
        @DisplayName("판매자 본인의 정산 내역 조회가 성공하면 200 OK를 반환한다")
        void shouldReturnMySellerSettlements() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            GetSettlementsResult result = new GetSettlementsResult(List.of(sampleSettlementResult()));
            when(getSellerSettlementsUseCase.getSellerSettlements(any())).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetSellerSettlementsResponse>> response =
                    settlementController.getMySettlements(2026, 1, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().settlements()).hasSize(1);
            verify(getSellerSettlementsUseCase).getSellerSettlements(any());
        }
    }
}
