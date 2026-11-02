package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentShopRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentShopRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentShopsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentShopResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFulfillmentShopResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentShopsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentShopUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentShopUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentShopController 풀필먼트 쇼핑몰 관리")
class FulfillmentShopControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentShopController controller;

    @Mock
    private RegisterFulfillmentShopUseCase registerFulfillmentShopUseCase;
    @Mock
    private GetFulfillmentShopsUseCase getFulfillmentShopsUseCase;
    @Mock
    private UpdateFulfillmentShopUseCase updateFulfillmentShopUseCase;

    private RegisterFulfillmentShopRequest registerRequest() {
        RegisterFulfillmentShopRequest request = new RegisterFulfillmentShopRequest();
        ReflectionTestUtils.setField(request, "shopName", "쇼핑몰1");
        return request;
    }

    private UpdateFulfillmentShopRequest updateRequest() {
        UpdateFulfillmentShopRequest request = new UpdateFulfillmentShopRequest();
        ReflectionTestUtils.setField(request, "shopCode", "SHOP-01");
        return request;
    }

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/shops/{customerCode} - 출고처 등록")
    class RegisterShop {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentShopUseCase.registerShop(any(RegisterFulfillmentShopCommand.class)))
                    .thenReturn(RegisterFulfillmentShopResult.of("ok", "SUC", "SHOP-01"));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentShopResponse>> response =
                    controller.registerShop(CUSTOMER_CODE, ACCESS_TOKEN, registerRequest());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerFulfillmentShopUseCase).registerShop(any(RegisterFulfillmentShopCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/shops/{customerCode} - 출고처 목록 조회")
    class GetShops {

        @Test
        @DisplayName("경로/헤더를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentShopsUseCase.getShops(any(GetFulfillmentShopsCommand.class)))
                    .thenReturn(GetFulfillmentShopsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentShopsResponse>> response =
                    controller.getShops(CUSTOMER_CODE, ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentShopsUseCase).getShops(any(GetFulfillmentShopsCommand.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vendors/fassto/shops/{customerCode} - 출고처 수정")
    class UpdateShop {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 수정 UseCase에 위임한다")
        void returnsOk() {
            // given
            when(updateFulfillmentShopUseCase.updateShop(any(UpdateFulfillmentShopCommand.class)))
                    .thenReturn(UpdateFulfillmentShopResult.of("ok", "SUC", "SHOP-01"));

            // when
            ResponseEntity<BaseResponse<UpdateFulfillmentShopResponse>> response =
                    controller.updateShop(CUSTOMER_CODE, ACCESS_TOKEN, updateRequest());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateFulfillmentShopUseCase).updateShop(any(UpdateFulfillmentShopCommand.class));
        }
    }
}
