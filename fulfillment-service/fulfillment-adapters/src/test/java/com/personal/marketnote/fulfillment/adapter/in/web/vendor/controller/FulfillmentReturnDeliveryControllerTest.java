package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentReturnGodDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentDirectReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentReturnGodDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDirectReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentReturnDeliveryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentReturnDeliveryController 풀필먼트 반품 배송 관리")
class FulfillmentReturnDeliveryControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentReturnDeliveryController controller;

    @Mock
    private RegisterFulfillmentReturnDeliveryUseCase registerFulfillmentReturnDeliveryUseCase;
    @Mock
    private RegisterFulfillmentDirectReturnDeliveryUseCase registerFulfillmentDirectReturnDeliveryUseCase;
    @Mock
    private GetFulfillmentReturnGodDetailUseCase getFulfillmentReturnGodDetailUseCase;

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/return-deliveries/{customerCode} - 반품 예약 등록")
    class RegisterReturnDelivery {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentReturnDeliveryUseCase.registerReturnDelivery(any(RegisterFulfillmentReturnDeliveryCommand.class)))
                    .thenReturn(com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentReturnDeliveryResponse>> response =
                    controller.registerReturnDelivery(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerFulfillmentReturnDeliveryUseCase).registerReturnDelivery(any(RegisterFulfillmentReturnDeliveryCommand.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/return-deliveries/direct/{customerCode} - 반품 택배사 미지정 등록")
    class RegisterDirectReturnDelivery {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 미지정 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentDirectReturnDeliveryUseCase.registerDirectReturnDelivery(
                    any(RegisterFulfillmentDirectReturnDeliveryCommand.class)
            )).thenReturn(RegisterFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentDirectReturnDeliveryResponse>> response =
                    controller.registerDirectReturnDelivery(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerFulfillmentDirectReturnDeliveryUseCase).registerDirectReturnDelivery(any(RegisterFulfillmentDirectReturnDeliveryCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/return-deliveries/god-detail/{customerCode} - 반품 완료 상품 조회")
    class GetReturnGodDetail {

        @Test
        @DisplayName("쿼리 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentReturnGodDetailUseCase.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenReturn(GetFulfillmentReturnGodDetailResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentReturnGodDetailResponse>> response = controller.getReturnGodDetail(
                    CUSTOMER_CODE, ACCESS_TOKEN, "2026-04-01", "2026-10-07", null, "WH01"
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentReturnGodDetailUseCase).getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class));
        }
    }
}
