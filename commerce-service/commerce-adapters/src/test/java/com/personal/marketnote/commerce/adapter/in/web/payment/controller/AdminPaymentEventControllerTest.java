package com.personal.marketnote.commerce.adapter.in.web.payment.controller;

import com.personal.marketnote.commerce.adapter.in.web.payment.request.ResolveUnknownPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.GetUnknownPaymentEventsResponse;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.ResolveUnknownPaymentResponse;
import com.personal.marketnote.commerce.port.in.command.payment.ResolveUnknownPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.GetUnknownPaymentEventsResult;
import com.personal.marketnote.commerce.port.in.result.payment.ResolveUnknownPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.GetUnknownPaymentEventsUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.ResolveUnknownPaymentUseCase;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentEventController 테스트")
class AdminPaymentEventControllerTest {

    @InjectMocks
    private AdminPaymentEventController adminPaymentEventController;

    @Mock
    private GetUnknownPaymentEventsUseCase getUnknownPaymentEventsUseCase;

    @Mock
    private ResolveUnknownPaymentUseCase resolveUnknownPaymentUseCase;

    @Nested
    @DisplayName("getUnknownPaymentEvents")
    class GetUnknownPaymentEvents {

        @Test
        @DisplayName("UNKNOWN 결제 이벤트 조회가 성공하면 200 OK와 이벤트 목록을 반환한다")
        void shouldReturnUnknownPaymentEvents() {
            // given
            GetUnknownPaymentEventsResult result = GetUnknownPaymentEventsResult.builder()
                    .id(1L)
                    .orderId(10L)
                    .orderKey("ORDER_KEY_1")
                    .amount(10_000L)
                    .method("CARD")
                    .resultCode("9999")
                    .resultMessage("UNKNOWN")
                    .createdAt(LocalDateTime.of(2026, 1, 15, 0, 0))
                    .build();
            when(getUnknownPaymentEventsUseCase.getUnknownPaymentEvents()).thenReturn(List.of(result));

            // when
            ResponseEntity<BaseResponse<List<GetUnknownPaymentEventsResponse>>> response =
                    adminPaymentEventController.getUnknownPaymentEvents();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            verify(getUnknownPaymentEventsUseCase).getUnknownPaymentEvents();
        }
    }

    @Nested
    @DisplayName("resolveUnknownPaymentEvent")
    class ResolveUnknownPaymentEvent {

        @Test
        @DisplayName("UNKNOWN 결제 이벤트 해소가 성공하면 200 OK와 해소 응답을 반환한다")
        void shouldReturnOkWhenResolveSucceeds() {
            // given
            String orderKey = "ORDER_KEY_1";
            ResolveUnknownPaymentRequest request = new ResolveUnknownPaymentRequest(
                    "COMPLETE", "0000", "정상", "PG_KEY_1", "APPROVAL_1", "20260115120000"
            );
            ResolveUnknownPaymentResult result = ResolveUnknownPaymentResult.builder()
                    .orderKey(orderKey)
                    .resolvedStatus("COMPLETE")
                    .orderId(10L)
                    .build();
            when(resolveUnknownPaymentUseCase.resolve(any(ResolveUnknownPaymentCommand.class)))
                    .thenReturn(result);

            // when
            ResponseEntity<BaseResponse<ResolveUnknownPaymentResponse>> response =
                    adminPaymentEventController.resolveUnknownPaymentEvent(orderKey, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            ArgumentCaptor<ResolveUnknownPaymentCommand> captor =
                    ArgumentCaptor.forClass(ResolveUnknownPaymentCommand.class);
            verify(resolveUnknownPaymentUseCase).resolve(captor.capture());
            assertThat(captor.getValue().orderKey()).isEqualTo(orderKey);
            assertThat(captor.getValue().resolvedStatus()).isEqualTo("COMPLETE");
        }
    }
}
