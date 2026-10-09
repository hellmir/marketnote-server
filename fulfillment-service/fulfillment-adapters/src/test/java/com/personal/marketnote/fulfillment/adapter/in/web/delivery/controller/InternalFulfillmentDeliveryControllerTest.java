package com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.request.CancelInternalFulfillmentDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.request.RegisterInternalReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.CancelInternalFulfillmentDeliveryResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.GetFulfillmentWorkStatusResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.GetInternalReturnGodDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.GetShippingStatusResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.RegisterInternalReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.GetFulfillmentWorkStatusCommand;
import com.personal.marketnote.fulfillment.port.in.command.GetInternalReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.GetShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.GetFulfillmentWorkStatusResult;
import com.personal.marketnote.fulfillment.port.in.result.GetInternalReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.GetShippingStatusResult;
import com.personal.marketnote.fulfillment.port.in.result.RegisterInternalReturnDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.CancelInternalFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.GetFulfillmentWorkStatusUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.GetInternalReturnGodDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.GetShippingStatusUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.RegisterInternalReturnDeliveryUseCase;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalFulfillmentDeliveryController 풀필먼트 내부 배송 API")
class InternalFulfillmentDeliveryControllerTest {

    @InjectMocks
    private InternalFulfillmentDeliveryController controller;

    @Mock
    private GetFulfillmentWorkStatusUseCase getFulfillmentWorkStatusUseCase;
    @Mock
    private CancelInternalFulfillmentDeliveryUseCase cancelInternalFulfillmentDeliveryUseCase;
    @Mock
    private RegisterInternalReturnDeliveryUseCase registerInternalReturnDeliveryUseCase;
    @Mock
    private GetInternalReturnGodDetailUseCase getInternalReturnGodDetailUseCase;
    @Mock
    private GetShippingStatusUseCase getShippingStatusUseCase;

    @Nested
    @DisplayName("GET /api/v1/internal/fulfillment/deliveries/work-status - 작업 상태 조회")
    class GetWorkStatus {

        @Test
        @DisplayName("주문 ID를 Command로 전달하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentWorkStatusUseCase.getWorkStatus(new GetFulfillmentWorkStatusCommand(100L)))
                    .thenReturn(new GetFulfillmentWorkStatusResult(100L, "WORKING"));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentWorkStatusResponse>> response = controller.getWorkStatus(100L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentWorkStatusUseCase).getWorkStatus(new GetFulfillmentWorkStatusCommand(100L));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/internal/fulfillment/deliveries/shipping-status - 배송 상태 조회")
    class GetShippingStatus {

        @Test
        @DisplayName("주문 ID를 Command로 전달하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getShippingStatusUseCase.getShippingStatus(new GetShippingStatusCommand(200L)))
                    .thenReturn(new GetShippingStatusResult(200L, "SHIPPING", true, "T1", "CJ", null));

            // when
            ResponseEntity<BaseResponse<GetShippingStatusResponse>> response = controller.getShippingStatus(200L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getShippingStatusUseCase).getShippingStatus(new GetShippingStatusCommand(200L));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/internal/fulfillment/deliveries/cancel - 출고 취소")
    class CancelDelivery {

        @Test
        @DisplayName("요청 본문의 주문 ID로 취소 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            CancelInternalFulfillmentDeliveryRequest request = new CancelInternalFulfillmentDeliveryRequest(300L);
            when(cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(new CancelInternalFulfillmentDeliveryCommand(300L)))
                    .thenReturn(new CancelInternalFulfillmentDeliveryResult(300L, true, "cancelled"));

            // when
            ResponseEntity<BaseResponse<CancelInternalFulfillmentDeliveryResponse>> response = controller.cancelDelivery(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(new CancelInternalFulfillmentDeliveryCommand(300L));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/internal/fulfillment/deliveries/return - 반품 등록")
    class RegisterReturnDelivery {

        @Test
        @DisplayName("반품 요청 본문을 Command로 매핑하여 UseCase에 위임하고 Created를 반환한다")
        void returnsCreated() {
            // given
            RegisterInternalReturnDeliveryRequest request = new RegisterInternalReturnDeliveryRequest(
                    400L,
                    "2026-09-03",
                    "홍길동",
                    "010-0000-0000",
                    "서울특별시 강남구",
                    "홍회수",
                    "010-1111-1111",
                    "06000",
                    "서울특별시 서초구",
                    "101호",
                    "단순변심",
                    "색상 불만족",
                    "부재 시 문 앞",
                    List.of(new RegisterInternalReturnDeliveryRequest.ProductItem("GOD01", 2))
            );
            when(registerInternalReturnDeliveryUseCase.registerReturnDelivery(any(RegisterInternalReturnDeliveryCommand.class)))
                    .thenReturn(RegisterInternalReturnDeliveryResult.of(400L, "RTN-001", true, "registered"));

            // when
            ResponseEntity<BaseResponse<RegisterInternalReturnDeliveryResponse>> response =
                    controller.registerReturnDelivery(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ArgumentCaptor<RegisterInternalReturnDeliveryCommand> captor =
                    ArgumentCaptor.forClass(RegisterInternalReturnDeliveryCommand.class);
            verify(registerInternalReturnDeliveryUseCase).registerReturnDelivery(captor.capture());
            RegisterInternalReturnDeliveryCommand command = captor.getValue();
            assertThat(command.orderId()).isEqualTo(400L);
            assertThat(command.pickupZipCode()).isEqualTo("06000");
            assertThat(command.products()).hasSize(1);
            assertThat(command.products().get(0).productCode()).isEqualTo("GOD01");
            assertThat(command.products().get(0).quantity()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/internal/fulfillment/deliveries/return-god-detail - 반품 완료 상품 상세 조회")
    class GetReturnGodDetail {

        @Test
        @DisplayName("반품 요청번호를 Command로 전달하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getInternalReturnGodDetailUseCase.getReturnGodDetail(eq(GetInternalReturnGodDetailCommand.of("RTN1,RTN2"))))
                    .thenReturn(GetInternalReturnGodDetailResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetInternalReturnGodDetailResponse>> response = controller.getReturnGodDetail("RTN1,RTN2");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getInternalReturnGodDetailUseCase).getReturnGodDetail(eq(GetInternalReturnGodDetailCommand.of("RTN1,RTN2")));
        }
    }
}
