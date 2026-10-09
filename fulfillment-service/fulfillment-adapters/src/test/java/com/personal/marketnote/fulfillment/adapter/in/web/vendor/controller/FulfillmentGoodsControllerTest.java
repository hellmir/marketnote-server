package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentGoodsElementsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsElementsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsElementsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentGoodsDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentGoodsElementsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentGoodsUseCase;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentGoodsController 풀필먼트 상품 관리")
class FulfillmentGoodsControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentGoodsController controller;

    @Mock
    private RegisterFulfillmentGoodsUseCase registerFulfillmentGoodsUseCase;
    @Mock
    private GetFulfillmentGoodsUseCase getFulfillmentGoodsUseCase;
    @Mock
    private GetFulfillmentGoodsDetailUseCase getFulfillmentGoodsDetailUseCase;
    @Mock
    private UpdateFulfillmentGoodsUseCase updateFulfillmentGoodsUseCase;
    @Mock
    private GetFulfillmentGoodsElementsUseCase getFulfillmentGoodsElementsUseCase;

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/goods/{customerCode} - 상품 등록")
    class RegisterGoods {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentGoodsUseCase.registerGoods(any(RegisterFulfillmentGoodsCommand.class)))
                    .thenReturn(RegisterFulfillmentGoodsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentGoodsResponse>> response =
                    controller.registerGoods(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ArgumentCaptor<RegisterFulfillmentGoodsCommand> captor =
                    ArgumentCaptor.forClass(RegisterFulfillmentGoodsCommand.class);
            verify(registerFulfillmentGoodsUseCase).registerGoods(captor.capture());
            assertThat(captor.getValue().customerCode()).isEqualTo(CUSTOMER_CODE);
            assertThat(captor.getValue().accessToken()).isEqualTo(ACCESS_TOKEN);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/goods/{customerCode} - 상품 목록 조회")
    class GetGoods {

        @Test
        @DisplayName("경로/헤더를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentGoodsUseCase.getGoods(any(GetFulfillmentGoodsCommand.class)))
                    .thenReturn(GetFulfillmentGoodsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentGoodsResponse>> response =
                    controller.getGoods(CUSTOMER_CODE, ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentGoodsUseCase).getGoods(any(GetFulfillmentGoodsCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/goods/detail/{customerCode} - 단일 상품 조회")
    class GetGoodsDetail {

        @Test
        @DisplayName("상품코드와 함께 상세 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentGoodsDetailUseCase.getGoodsDetail(any(GetFulfillmentGoodsDetailCommand.class)))
                    .thenReturn(GetFulfillmentGoodsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentGoodsResponse>> response =
                    controller.getGoodsDetail(CUSTOMER_CODE, ACCESS_TOKEN, "GOD_01");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentGoodsDetailUseCase).getGoodsDetail(any(GetFulfillmentGoodsDetailCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/goods/element/{customerCode} - 모음상품 상세 조회")
    class GetGoodsElements {

        @Test
        @DisplayName("Command로 매핑하여 모음상품 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentGoodsElementsUseCase.getGoodsElements(any(GetFulfillmentGoodsElementsCommand.class)))
                    .thenReturn(GetFulfillmentGoodsElementsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentGoodsElementsResponse>> response =
                    controller.getGoodsElements(CUSTOMER_CODE, ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentGoodsElementsUseCase).getGoodsElements(any(GetFulfillmentGoodsElementsCommand.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vendors/fassto/goods/{customerCode} - 상품 수정")
    class UpdateGoods {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 수정 UseCase에 위임한다")
        void returnsOk() {
            // given
            when(updateFulfillmentGoodsUseCase.updateGoods(any(UpdateFulfillmentGoodsCommand.class)))
                    .thenReturn(UpdateFulfillmentGoodsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<UpdateFulfillmentGoodsResponse>> response =
                    controller.updateGoods(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateFulfillmentGoodsUseCase).updateGoods(any(UpdateFulfillmentGoodsCommand.class));
        }
    }
}
