package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentWarehousingAbnormalImageResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentWarehousingAbnormalResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentWarehousingDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentWarehousingInspecDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentWarehousingResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentWarehousingResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFulfillmentWarehousingResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalImageCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingInspecDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalImageResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingInspecDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingAbnormalImageUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingAbnormalUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingInspecDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentWarehousingUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentWarehousingUseCase;
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
@DisplayName("FulfillmentWarehousingController 풀필먼트 입고 관리")
class FulfillmentWarehousingControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentWarehousingController controller;

    @Mock
    private RegisterFulfillmentWarehousingUseCase registerFulfillmentWarehousingUseCase;
    @Mock
    private GetFulfillmentWarehousingUseCase getFulfillmentWarehousingUseCase;
    @Mock
    private GetFulfillmentWarehousingDetailUseCase getFulfillmentWarehousingDetailUseCase;
    @Mock
    private GetFulfillmentWarehousingInspecDetailUseCase getFulfillmentWarehousingInspecDetailUseCase;
    @Mock
    private GetFulfillmentWarehousingAbnormalUseCase getFulfillmentWarehousingAbnormalUseCase;
    @Mock
    private GetFulfillmentWarehousingAbnormalImageUseCase getFulfillmentWarehousingAbnormalImageUseCase;
    @Mock
    private UpdateFulfillmentWarehousingUseCase updateFulfillmentWarehousingUseCase;

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/warehousing/{customerCode} - 입고 등록")
    class RegisterWarehousing {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentWarehousingUseCase.registerWarehousing(any(RegisterFulfillmentWarehousingCommand.class)))
                    .thenReturn(RegisterFulfillmentWarehousingResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentWarehousingResponse>> response =
                    controller.registerWarehousing(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerFulfillmentWarehousingUseCase).registerWarehousing(any(RegisterFulfillmentWarehousingCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/warehousing/{customerCode}/{startDate}/{endDate} - 입고 목록 조회")
    class GetWarehousing {

        @Test
        @DisplayName("경로/쿼리 파라미터를 Query로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentWarehousingUseCase.getWarehousing(any(GetFulfillmentWarehousingCommand.class)))
                    .thenReturn(GetFulfillmentWarehousingResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentWarehousingResponse>> response = controller.getWarehousing(
                    CUSTOMER_CODE, "20260401", "20260415", ACCESS_TOKEN, "01", "ORD-1", "1"
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentWarehousingUseCase).getWarehousing(any(GetFulfillmentWarehousingCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/warehousing/detail/{customerCode}/{slipNo} - 입고 상세 조회")
    class GetWarehousingDetail {

        @Test
        @DisplayName("슬립번호와 함께 상세 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentWarehousingDetailUseCase.getWarehousingDetail(any(GetFulfillmentWarehousingDetailCommand.class)))
                    .thenReturn(GetFulfillmentWarehousingDetailResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentWarehousingDetailResponse>> response =
                    controller.getWarehousingDetail(CUSTOMER_CODE, "SLIP-1", ACCESS_TOKEN, "ORD-1");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentWarehousingDetailUseCase).getWarehousingDetail(any(GetFulfillmentWarehousingDetailCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/warehousing/inspec/{customerCode}/{slipNo}/{whCd} - 검수 상세 조회")
    class GetWarehousingInspecDetail {

        @Test
        @DisplayName("슬립번호/센터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentWarehousingInspecDetailUseCase.getWarehousingInspecDetail(
                    any(GetFulfillmentWarehousingInspecDetailCommand.class)
            )).thenReturn(GetFulfillmentWarehousingInspecDetailResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentWarehousingInspecDetailResponse>> response =
                    controller.getWarehousingInspecDetail(CUSTOMER_CODE, "SLIP-1", "WH01", ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentWarehousingInspecDetailUseCase).getWarehousingInspecDetail(any(GetFulfillmentWarehousingInspecDetailCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/warehousing/abnormal/{customerCode}/{whCd}/{slipNo} - 비정상 입고 조회")
    class GetWarehousingAbnormal {

        @Test
        @DisplayName("센터/슬립번호를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentWarehousingAbnormalUseCase.getWarehousingAbnormal(any(GetFulfillmentWarehousingAbnormalCommand.class)))
                    .thenReturn(GetFulfillmentWarehousingAbnormalResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentWarehousingAbnormalResponse>> response =
                    controller.getWarehousingAbnormal(CUSTOMER_CODE, "WH01", "SLIP-1", ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentWarehousingAbnormalUseCase).getWarehousingAbnormal(any(GetFulfillmentWarehousingAbnormalCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/warehousing/abnormal/image/... - 비정상 입고 이미지 조회")
    class GetWarehousingAbnormalImage {

        @Test
        @DisplayName("이미지 식별 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentWarehousingAbnormalImageUseCase.getWarehousingAbnormalImage(
                    any(GetFulfillmentWarehousingAbnormalImageCommand.class)
            )).thenReturn(GetFulfillmentWarehousingAbnormalImageResult.of(0, null));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentWarehousingAbnormalImageResponse>> response =
                    controller.getWarehousingAbnormalImage("SLIP-1", "GOD01", "SN01", "1", "1", ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentWarehousingAbnormalImageUseCase).getWarehousingAbnormalImage(any(GetFulfillmentWarehousingAbnormalImageCommand.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vendors/fassto/warehousing/{customerCode} - 입고 수정")
    class UpdateWarehousing {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 수정 UseCase에 위임한다")
        void returnsOk() {
            // given
            when(updateFulfillmentWarehousingUseCase.updateWarehousing(any(UpdateFulfillmentWarehousingCommand.class)))
                    .thenReturn(UpdateFulfillmentWarehousingResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<UpdateFulfillmentWarehousingResponse>> response =
                    controller.updateWarehousing(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateFulfillmentWarehousingUseCase).updateWarehousing(any(UpdateFulfillmentWarehousingCommand.class));
        }
    }
}
